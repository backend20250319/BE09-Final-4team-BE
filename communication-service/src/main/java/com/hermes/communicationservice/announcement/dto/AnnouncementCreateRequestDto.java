package com.hermes.communicationservice.announcement.dto;


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
  private String content;
  private List<String> fileIds = new ArrayList<>();

}
