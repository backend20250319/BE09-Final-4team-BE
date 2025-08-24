package com.hermes.ftpstarter.exception;

public class FtpFileNotFoundException extends RuntimeException {
  public FtpFileNotFoundException(String fileName) {
    super("존재하지 않는 파일명: " + fileName);
  }
}
