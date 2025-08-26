package com.hermes.communicationservice.file.dto;

import com.hermes.communicationservice.file.entity.FileMapping;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileMappingDto {
  private Long id;
  private String originalName;
  private String storedName;
  private String url;

  public static FileMappingDto fromEntity(FileMapping fileMapping, String url) {
    return FileMappingDto.builder()
        .id(fileMapping.getId())
        .originalName(fileMapping.getOriginalName())
        .storedName(fileMapping.getStoredName())
        .url(url)
        .build();
  }

  public static FileMapping toEntity(FileMappingDto dto) {
    return FileMapping.builder()
        .originalName(dto.getOriginalName())
        .storedName(dto.getStoredName())
        .build();
  }
}