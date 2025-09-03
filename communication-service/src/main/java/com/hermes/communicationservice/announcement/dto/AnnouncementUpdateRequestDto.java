package com.hermes.communicationservice.announcement.dto;

import com.fasterxml.jackson.databind.JsonNode;
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
public class AnnouncementUpdateRequestDto {

  @Size(max = 200)
  private String title;
  @Size(max = 100)
  private String displayAuthor;
  private JsonNode content;
  private List<String> fileIds;

}
