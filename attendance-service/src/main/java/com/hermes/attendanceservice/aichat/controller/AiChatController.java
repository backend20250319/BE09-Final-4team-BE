package com.hermes.attendanceservice.aichat.controller;

import com.hermes.api.common.ApiResult;
import com.hermes.attendanceservice.aichat.dto.ChatRequest;
import com.hermes.attendanceservice.aichat.dto.ChatResponse;
import com.hermes.attendanceservice.aichat.service.AiChatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/aichat")
@RequiredArgsConstructor
public class AiChatController {

    private final AiChatService aiChatService;

    @PostMapping("/chat")
    public ResponseEntity<ApiResult<ChatResponse>> chat(@RequestBody ChatRequest req) {
        try {
            ChatResponse res = aiChatService.chat(req);
            return ResponseEntity.ok(ApiResult.success("AI 응답", res));
        } catch (Exception e) {
            log.error("AI chat error", e);
            return ResponseEntity.ok(ApiResult.failure("AI 요청 처리 중 오류가 발생했습니다."));
        }
    }
} 