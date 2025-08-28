package com.hermes.communicationservice.announcement.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileResponseDto {
    private Long id;
    private String originalName;
    private String storedName;
    private String url;
}
