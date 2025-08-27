package com.hermes.communicationservice.announcement.dto;

import com.hermes.communicationservice.announcement.entity.Announcement;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;
import com.hermes.communicationservice.file.entity.FileMapping;
import jakarta.validation.constraints.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnnouncementCreateRequestDto {

  @NotBlank(message = "제목은 필수입니다.")
  @Size(max = 200)
  private String title;
  @NotNull
  private Long authorId;
  @Size(max = 100)
  private String displayAuthor;
  private String content;
  @Size(max = 10, message = "파일은 최대 10개까지 업로드 가능합니다.")
  private List<MultipartFile> multipartFiles = new ArrayList<>();
}
