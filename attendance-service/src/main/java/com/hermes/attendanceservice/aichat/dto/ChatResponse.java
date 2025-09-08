package com.hermes.attendanceservice.aichat.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatResponse {
    /** reply 또는 command */
    private String type;
    private String reply; // type==reply 인 경우
    private CommandPayload command; // type==command 인 경우
} 