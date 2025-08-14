# News Crawler Service

HERMES 프로젝트의 뉴스 크롤링 마이크로서비스입니다.

## 기능

- 네이버 뉴스 자동 크롤링
- 카테고리별 뉴스 수집 (정치, 경제, 사회, 생활/문화, 세계, IT/과학)
- 뉴스 기사 조회 및 검색 API
- CSV 파일로 크롤링 결과 저장

## 기술 스택

- Spring Boot 3.5.4
- Spring Cloud (Eureka Client)
- Spring Data JPA
- MySQL
- Jsoup (웹 크롤링)
- Selenium (동적 콘텐츠 크롤링)
- Lombok

## API 엔드포인트

### 뉴스 조회
- `GET /api/news` - 모든 뉴스 기사 조회
- `GET /api/news/recent` - 최근 뉴스 기사 10개 조회
- `GET /api/news/{id}` - ID로 뉴스 기사 조회
- `GET /api/news/category/{categoryId}` - 카테고리별 뉴스 기사 조회
- `GET /api/news/press/{press}` - 언론사별 뉴스 기사 조회
- `GET /api/news/search?title={title}` - 제목으로 뉴스 기사 검색
- `GET /api/news/count` - 저장된 뉴스 기사 수 조회

## 실행 방법

1. MySQL 데이터베이스 설정
   - 데이터베이스: `hermes_news`
   - 사용자: `root`
   - 비밀번호: `1234`

2. 서비스 실행
   ```bash
   ./gradlew bootRun
   ```

3. 서비스 포트: 8083

## 크롤링 설정

- 자동 크롤링은 서비스 시작 시 실행됩니다
- 각 카테고리별로 최대 2개씩, 총 최대 10개 기사를 수집합니다
- 허용된 언론사: 연합뉴스, 동아일보, 중앙일보, 한겨레, 경향신문, MBC, 파이낸셜뉴스, 국민일보, 서울경제, 한국일보, 헤럴드경제, YTN, 문화일보, 오마이뉴스, SBS, KBS

## 데이터베이스 스키마

### news_articles 테이블
- `id`: 기본키
- `category_id`: 카테고리 ID
- `category_name`: 카테고리명
- `press`: 언론사
- `title`: 제목
- `content`: 내용
- `reporter`: 기자
- `date`: 날짜
- `link`: 링크
- `created_at`: 생성일시
