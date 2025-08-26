package com.hermes.communicationservice.announcement.dto;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnnouncementSummaryDto {

  private Long id;
  private String title;
  private String displayAuthor;
  private int views;
  private int commentCount;
  private LocalDateTime createdAt;

}
