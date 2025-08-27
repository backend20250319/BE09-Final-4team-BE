package com.hermes.newscrawler.util;

import com.hermes.newscrawler.dto.NewsDetail;
import com.hermes.newscrawler.service.NewsArticleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
@RequiredArgsConstructor
@Slf4j
public class DatabaseCrawler implements CommandLineRunner {

    private final NewsArticleService newsArticleService;

    private final Map<String, List<NewsDetail>> sessionNewsMap = new ConcurrentHashMap<>();

    @Override
    public void run(String... args) throws Exception {
        try {
            newsArticleService.deleteAllNews();
            String initialSessionId = "initial-" + System.currentTimeMillis();
            performCrawlingForLogin(initialSessionId);
        } catch (Exception e) {
            log.error("초기 크롤링 실패: {}", e.getMessage(), e);
        }
    }

    private static final Map<Integer, String> CATEGORIES = Map.of(
            105, "IT/과학",
            100, "정치",
            101, "경제",
            102, "사회",
            103, "생활/문화",
            104, "세계"
    );

    public List<NewsDetail> performCrawlingForLogin(String sessionId) throws Exception {
        ChromeOptions options = createChromeOptions();
        WebDriver driver = new ChromeDriver(options);
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(3));

        int targetCountPerCategory = 2;
        int maxTotalArticles = 12;
        List<NewsDetail> completeNewsList = new ArrayList<>();

        try {
            for (Map.Entry<Integer, String> category : CATEGORIES.entrySet()) {
                int categoryId = category.getKey();
                String categoryName = category.getValue();

                if (completeNewsList.size() >= maxTotalArticles) {
                    break;
                }

                List<NewsDetail> categoryNews = crawlCategory(driver, wait, categoryId, categoryName, targetCountPerCategory);
                completeNewsList.addAll(categoryNews);
            }

            if (completeNewsList.size() > maxTotalArticles) {
                completeNewsList = completeNewsList.subList(0, maxTotalArticles);
            }

            sessionNewsMap.put(sessionId, completeNewsList);

            try {
                newsArticleService.saveNewsArticles(completeNewsList);
            } catch (Exception e) {
                log.error("데이터베이스 저장 실패: {}", e.getMessage(), e);
            }

            return completeNewsList;

        } catch (Exception e) {
            log.error("크롤링 중 오류 발생", e);
            throw e;
        } finally {
            if (driver != null) {
                driver.quit();
            }
        }
    }

    public List<NewsDetail> getNewsForSession(String sessionId) {
        return sessionNewsMap.getOrDefault(sessionId, new ArrayList<>());
    }

    public void clearSessionData(String sessionId) {
        sessionNewsMap.remove(sessionId);
    }

    public void clearAllSessionData() {
        sessionNewsMap.clear();
    }

    private ChromeOptions createChromeOptions() {
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--disable-gpu");
        options.addArguments("--window-size=1920,1080");
        options.addArguments("--disable-extensions");
        options.addArguments("--disable-plugins");
        options.addArguments("--disable-images");
        options.addArguments("--disable-javascript");
        options.addArguments("--disable-css");
        return options;
    }

    private List<NewsDetail> crawlCategory(WebDriver driver, WebDriverWait wait, int categoryId, String categoryName, int targetCount) {
        List<NewsDetail> categoryNews = new ArrayList<>();
        Set<String> collectedLinks = new HashSet<>();

        try {
            String categoryUrl = "https://news.naver.com/section/" + categoryId;
            driver.get(categoryUrl);
            Thread.sleep(1000);

            while (collectedLinks.size() < targetCount) {
                List<WebElement> articles = driver.findElements(
                        By.cssSelector("#newsct > div.section_latest > div > div.section_latest_article._CONTENT_LIST._PERSIST_META > div > ul > li")
                );

                for (WebElement article : articles) {
                    if (collectedLinks.size() >= targetCount) break;

                    try {
                        NewsDetail detail = extractArticleDetail(article, categoryId, categoryName);
                        if (detail != null && !collectedLinks.contains(detail.getLink())) {
                            collectedLinks.add(detail.getLink());
                            categoryNews.add(detail);
                        }
                    } catch (Exception e) {
                    }
                }

                if (!clickMoreButton(driver, wait)) {
                    break;
                }
            }

        } catch (Exception e) {
        }

        return categoryNews;
    }

    private NewsDetail extractArticleDetail(WebElement article, int categoryId, String categoryName) {
        try {
            WebElement titleElement = article.findElement(By.cssSelector("div.sa_text > a"));
            WebElement pressElement = article.findElement(By.cssSelector("div.sa_text_info > div.sa_text_info_left > div.sa_text_press"));

            String title = titleElement.getText();
            String link = titleElement.getAttribute("href");
            String press = pressElement.getText();

            if (!isAllowedPress(press)) {
                return null;
            }

            return crawlNewsDetailFast(link, title, press, categoryName, categoryId);

        } catch (Exception e) {
            return null;
        }
    }

    private boolean isAllowedPress(String press) {
        Set<String> allowedPresses = Set.of(
                "연합뉴스", "동아일보", "중앙일보", "한겨레", "경향신문",
                "MBC", "파이낸셜뉴스", "국민일보", "서울경제", "한국일보",
                "헤럴드경제", "YTN", "문화일보", "오마이뉴스", "SBS", "KBS"
        );
        return allowedPresses.stream().anyMatch(p -> p.equalsIgnoreCase(press.trim()));
    }

    private boolean clickMoreButton(WebDriver driver, WebDriverWait wait) {
        try {
            WebElement moreBtn = wait.until(ExpectedConditions.elementToBeClickable(
                    By.cssSelector("#newsct > div.section_latest > div > div.section_more > a")
            ));
            moreBtn.click();
            Thread.sleep(500);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private NewsDetail crawlNewsDetailFast(String url, String title, String press, String categoryName, int categoryId) {
        try {
            Document doc = Jsoup.connect(url)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36")
                    .timeout(5000)
                    .get();

            String reporter = "";
            Element reporterElement = doc.selectFirst("#ct > div.media_end_head.go_trans > div.media_end_head_info.nv_notrans > div.media_end_head_journalist > a > em");
            if (reporterElement != null) {
                reporter = reporterElement.text();
            }

            String content = "";
            Element contentElement = doc.selectFirst("#dic_area");
            if (contentElement != null) {
                content = contentElement.text();
            }

            if (reporter.isEmpty() && !content.isEmpty()) {
                int searchWindowSize = 100;
                int startIndex = Math.max(0, content.length() - searchWindowSize);
                String searchArea = content.substring(startIndex);

                Pattern pattern = Pattern.compile("([가-힣]{2,5}\\s*(기자|특파원|객원기자|통신원))");
                Matcher matcher = pattern.matcher(searchArea);

                String foundReporter = "";
                int matchPosInSearchArea = -1;

                while (matcher.find()) {
                    foundReporter = matcher.group(1).trim();
                    matchPosInSearchArea = matcher.start();
                }

                if (!foundReporter.isEmpty()) {
                    reporter = foundReporter;
                    int originalIndex = startIndex + matchPosInSearchArea;
                    content = content.substring(0, originalIndex).trim();
                }
            }

            String date = "";
            Element dateElement = doc.selectFirst("span.media_end_head_info_datestamp_time._ARTICLE_DATE_TIME");
            if (dateElement != null) {
                date = dateElement.attr("data-date-time");
            }

            return new NewsDetail(title, content, reporter, date, url, press, categoryId, categoryName);

        } catch (Exception e) {
            return null;
        }
    }

    public void logMemoryUsage() {
        Runtime runtime = Runtime.getRuntime();
        long totalMemory = runtime.totalMemory();
        long freeMemory = runtime.freeMemory();
        long usedMemory = totalMemory - freeMemory;
        long maxMemory = runtime.maxMemory();

        log.info("메모리 사용량 - 사용: {}MB, 여유: {}MB, 최대: {}MB, 세션수: {}",
                usedMemory / 1024 / 1024,
                freeMemory / 1024 / 1024,
                maxMemory / 1024 / 1024,
                sessionNewsMap.size());
    }

    public void cleanupExpiredSessions() {
        long currentTime = System.currentTimeMillis();
        long sessionTimeout = 24 * 60 * 60 * 1000;

        List<String> expiredSessions = sessionNewsMap.keySet().stream()
                .filter(sessionId -> {
                    try {
                        long sessionTime = Long.parseLong(sessionId.split("-")[1]);
                        return currentTime - sessionTime > sessionTimeout;
                    } catch (Exception e) {
                        return true;
                    }
                })
                .toList();

        expiredSessions.forEach(sessionNewsMap::remove);
    }
}