package com.hermes.communicationservice.announcement.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnnouncementResponseDto {
    private Long id;
    private String title;
    private String displayAuthor;
    private String content;
    private LocalDateTime createdAt;
    private List<FileResponseDto> files;
}
