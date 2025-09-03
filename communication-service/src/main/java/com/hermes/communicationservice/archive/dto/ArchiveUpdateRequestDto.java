package com.hermes.communicationservice.archive.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
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
@Schema(description = "사내 문서 수정 요청")
public class ArchiveUpdateRequestDto {

  @Schema(description = "문서 제목", example = "2024년 사내 규정 업데이트 (수정)")
  private String title;

  @Schema(description = "문서 설명", example = "2024년 개정된 사내 규정에 대한 수정된 안내 문서입니다")
  private String description;

  @Schema(description = "첨부파일 ID 목록", example = "[\"file1\", \"file2\", \"file3\"]")
  private List<String> fileIds;

}