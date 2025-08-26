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

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnnouncementCreateRequestDto {

  private String title;
  private Long authorId;
  private String displayAuthor;
  private String content;
  private List<MultipartFile> multipartFiles = new ArrayList<>();

  public Announcement toEntity(List<FileMapping> fileMappings) {
      return Announcement.builder()
          .title(title)
          .authorId(authorId)
          .displayAuthor(displayAuthor)
          .content(content)
          .attachments(fileMappings)
          .build();
  }
}
