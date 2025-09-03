package com.hermes.communicationservice.archive.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "사내 문서 목록 응답")
public class ArchiveSummaryDto {

  @Schema(description = "문서 ID", example = "1")
  private Long id;

  @Schema(description = "문서 제목", example = "2024년 사내 규정 업데이트")
  private String title;

}