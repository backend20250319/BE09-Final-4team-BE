package com.hermes.ftpstarter.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FtpResponseDto {
  private String originalName;
  private String storedName;
  private String url;
}
