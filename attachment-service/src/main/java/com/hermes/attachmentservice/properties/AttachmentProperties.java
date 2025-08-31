package com.hermes.attachmentservice.properties;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;


@Component
@Getter
@Setter
@ConfigurationProperties(prefix = "ftp")
public class AttachmentProperties {

  private String host;           // 호스트
  private int uploadPort;        // 업로드 포트
  private int downloadPort;      // 다운로드 포트

  private String user;
  private String password;
  private String baseDir;

}

