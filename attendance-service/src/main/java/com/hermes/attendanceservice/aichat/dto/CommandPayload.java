package com.hermes.attendanceservice.aichat.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommandPayload {
    private CommandType type;
    private Map<String, Object> params;
} 