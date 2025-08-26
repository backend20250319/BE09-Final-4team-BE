package com.hermes.communicationservice.announcement.dto;

import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnnouncementUpdateRequestDto {
  private Long id;
  private String title;
  private Long authorId;
  private String displayAuthor;
  private String content;
  private List<Long> filesToDelete = new ArrayList<>();
  private List<MultipartFile> filesToUpload = new ArrayList<>();
}
