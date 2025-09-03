package com.hermes.communicationservice.announcement.dto;

import com.fasterxml.jackson.databind.JsonNode;
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
    private JsonNode content;
    private LocalDateTime createdAt;
    private int views;
    private List<String> fileIds;

}
