package com.hermes.communicationservice.announcement.dto;


import com.hermes.communicationservice.announcement.entity.Announcement;
import com.hermes.communicationservice.file.dto.FileMappingDto;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


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
  private int views;
  private List<FileMappingDto> fileMappingDtos = new ArrayList<>();
//   private List<CommentDto> comments; // 필요시 주석 해제

  public static AnnouncementResponseDto fromEntity(Announcement announcement,
      List<FileMappingDto> attachments) {
    return AnnouncementResponseDto.builder()
        .id(announcement.getId())
        .title(announcement.getTitle())
        .displayAuthor(announcement.getDisplayAuthor())
        .content(announcement.getContent())
        .createdAt(announcement.getCreatedAt())
        .views(announcement.getViews())
        .fileMappingDtos(attachments)
        .build();
  }

}
