package com.hermes.attendanceservice.aichat.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hermes.attendanceservice.aichat.config.AiChatProperties;
import com.hermes.attendanceservice.aichat.dto.ChatMessage;
import com.hermes.attendanceservice.aichat.dto.ChatRequest;
import com.hermes.attendanceservice.aichat.dto.ChatResponse;
import com.hermes.attendanceservice.aichat.dto.CommandPayload;
import com.hermes.attendanceservice.aichat.dto.CommandType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiChatService {

    private final AiChatProperties props;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public ChatResponse chat(ChatRequest req) {
        try {
            Map<String, Object> body = new HashMap<>();
            body.put("model", props.getModel());
            body.put("temperature", props.getTemperature());
            body.put("max_tokens", props.getMaxTokens());

            List<Map<String, String>> messages = new ArrayList<>();
            messages.add(Map.of(
                    "role", "system",
                    "content", "반드시 엄격한 JSON만 출력하세요. 추가 설명이나 코드블록 없이 하나의 JSON 객체로만 답하십시오. 예: {\"type\":\"reply\",\"reply\":\"...\"} 또는 {\"type\":\"command\",\"command\":{\"type\":\"FILL_VACATION_FORM\",\"params\":{\"startDate\":\"2025-01-03\",\"endDate\":\"2025-01-03\",\"type\":\"연차\",\"reason\":\"가족여행\"}}}"
            ));
            if (req.getMessages() != null) {
                for (ChatMessage m : req.getMessages()) {
                    messages.add(Map.of("role", m.getRole(), "content", m.getContent()));
                }
            }
            body.put("messages", messages);

            String json = objectMapper.writeValueAsString(body);

            HttpClient client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();
            HttpRequest httpRequest = HttpRequest.newBuilder()
                    .uri(URI.create(props.getBaseUrl() + "/chat/completions"))
                    .timeout(Duration.ofSeconds(60))
                    .header("Authorization", "Bearer " + props.getApiKey())
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json, StandardCharsets.UTF_8))
                    .build();

            HttpResponse<String> response = client.send(httpRequest, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

            if (response.statusCode() >= 400) {
                log.warn("OpenAI error: status={} body={}", response.statusCode(), response.body());
                return ChatResponse.builder().type("reply").reply("죄송해요, 지금은 요청을 처리할 수 없어요. 잠시 후 다시 시도해 주세요.").build();
            }

            JsonNode root = objectMapper.readTree(response.body());
            String content = root.path("choices").path(0).path("message").path("content").asText("");
            if (content == null || content.isBlank()) {
                return ChatResponse.builder().type("reply").reply("응답이 비어 있습니다. 다시 시도해 주세요.").build();
            }

            // First try strict JSON
            try {
                return parseAssistantJson(content, req);
            } catch (Exception e) {
                // Try repair if got JS-like object
                String repaired = repairJson(content);
                if (repaired != null) {
                    try {
                        return parseAssistantJson(repaired, req);
                    } catch (Exception ignore) {}
                }
                // Fallback to plain text
                return ChatResponse.builder().type("reply").reply(content).build();
            }
        } catch (Exception e) {
            log.error("AI chat failed", e);
            return ChatResponse.builder().type("reply").reply("오류가 발생했어요. 잠시 후 다시 시도해 주세요.").build();
        }
    }

    private ChatResponse parseAssistantJson(String jsonContent, ChatRequest req) throws Exception {
        JsonNode node = objectMapper.readTree(jsonContent);
        String type = node.path("type").asText("");
        if ("command".equalsIgnoreCase(type) && Boolean.TRUE.equals(req.getAllowCommands())) {
            String cmdTypeStr = node.path("command").path("type").asText("");
            CommandType cmdType = safeCommandType(cmdTypeStr);
            if (cmdType == null) {
                return ChatResponse.builder().type("reply").reply("허용되지 않은 명령이에요. 다른 방식으로 요청해 주세요.").build();
            }
            Map<String, Object> rawParams = objectMapper.convertValue(node.path("command").path("params"), Map.class);
            Map<String, Object> params = normalizeParams(cmdType, rawParams);
            if (!validateParams(cmdType, params)) {
                return ChatResponse.builder().type("reply").reply("명령 파라미터가 올바르지 않습니다. 필요한 정보를 더 알려 주세요.").build();
            }
            return ChatResponse.builder().type("command").command(CommandPayload.builder().type(cmdType).params(params).build()).build();
        } else {
            String reply = node.path("reply").asText("");
            if (reply.isBlank()) reply = jsonContent;
            return ChatResponse.builder().type("reply").reply(reply).build();
        }
    }

    private String repairJson(String raw) {
        if (raw == null) return null;
        String s = raw.trim();
        // If already looks like strict JSON, skip
        if (s.startsWith("{") && s.contains("\"type\"")) return null;
        // Replace single quotes with double
        s = s.replace('\'', '"');
        // Quote common keys if unquoted
        String[] keys = {"type", "command", "params", "startDate", "endDate", "start_date", "end_date", "date", "reason"};
        for (String k : keys) {
            s = s.replaceAll("(?<!\\\\)\\b" + k + ":", '"' + k + '"' + ":");
        }
        // Ensure outer braces
        if (!s.trim().startsWith("{")) {
            int first = s.indexOf('{');
            if (first >= 0) s = s.substring(first);
        }
        return s;
    }

    private CommandType safeCommandType(String value) {
        try { return CommandType.valueOf(Optional.ofNullable(value).orElse("")); }
        catch (Exception e) { return null; }
    }

    private Map<String, Object> normalizeParams(CommandType type, Map<String, Object> params) {
        if (params == null) params = new HashMap<>();
        Map<String, Object> out = new HashMap<>(params);
        switch (type) {
            case FILL_VACATION_FORM: {
                String start = firstString(params, "startDate", "from", "fromDate", "date", "start_date");
                String end = firstString(params, "endDate", "to", "toDate", "end_date");
                if (start != null) out.put("startDate", start);
                if (end == null && start != null) end = start; // single day
                if (end != null) out.put("endDate", end);
                return out;
            }
            default:
                return out;
        }
    }

    private String firstString(Map<String, Object> map, String... keys) {
        for (String k : keys) {
            Object v = map.get(k);
            if (v instanceof String && !((String) v).isBlank()) return (String) v;
        }
        return null;
    }

    private boolean validateParams(CommandType type, Map<String, Object> params) {
        if (params == null) params = Collections.emptyMap();
        switch (type) {
            case NAVIGATE:
                return params.containsKey("path") && params.get("path") instanceof String;
            case FILL_VACATION_FORM:
                return params.containsKey("startDate") && params.containsKey("endDate");
            case SUBMIT_VACATION_FORM:
                return params.containsKey("draftId");
            case OPEN_APPROVAL:
                return params.containsKey("approvalId");
            default:
                return false;
        }
    }
} 