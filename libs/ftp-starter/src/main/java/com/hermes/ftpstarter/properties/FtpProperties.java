package com.hermes.ftpstarter.properties;


import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;


@ConfigurationProperties(prefix = "ftp") // .yml에서 자동매핑
@Data
public class FtpProperties {

  private String host;           // 호스트
  private int uploadPort;        // 업로드 포트
  private int downloadPort;      // 다운로드 포트

  private String user;
  private String password;
  private String baseDir;

}

