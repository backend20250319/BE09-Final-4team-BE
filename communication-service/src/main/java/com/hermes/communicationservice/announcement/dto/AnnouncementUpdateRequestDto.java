package com.hermes.communicationservice.announcement.dto;

import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;
import jakarta.validation.constraints.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnnouncementUpdateRequestDto {
  @NotNull
  private Long id;
  @Size(max = 200)
  private String title;
  private Long authorId;
  @Size(max = 100)
  private String displayAuthor;
  private String content;
  @Size(max = 50)
  private List<Long> filesToDelete = new ArrayList<>();
  @Size(max = 10)
  private List<MultipartFile> filesToUpload = new ArrayList<>();
}
