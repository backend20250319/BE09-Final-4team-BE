package com.hermes.communicationservice.ftp.dto;

import com.hermes.communicationservice.ftp.entity.FileMapping;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
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
}