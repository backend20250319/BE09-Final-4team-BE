package com.hermes.communicationservice.announcement.dto;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnnouncementCreateRequestDto {

  @NotBlank(message = "제목은 필수입니다.")
  @Size(max = 200)
  private String title;
  @Size(max = 100)
  private String displayAuthor;
  @NotNull(message = "내용은 필수입니다.")
  private JsonNode content;
  @Builder.Default
  private List<String> fileIds = new ArrayList<>();

}
