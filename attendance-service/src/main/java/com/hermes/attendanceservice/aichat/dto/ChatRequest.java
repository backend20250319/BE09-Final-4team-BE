package com.hermes.attendanceservice.aichat.dto;

import lombok.Data;
import java.util.List;

@Data
public class ChatRequest {
    private List<ChatMessage> messages;
    /** 명령 실행 허용 여부 (기본 true) */
    private Boolean allowCommands = true;
} 