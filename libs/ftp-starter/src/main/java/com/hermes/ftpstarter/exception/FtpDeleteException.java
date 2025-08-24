package com.hermes.ftpstarter.exception;

public class FtpDeleteException extends RuntimeException {
  public FtpDeleteException(String fileName) {
    super("FTP 삭제 실패: " + fileName);
  }

  public FtpDeleteException(String message, Throwable cause) {
    super(message, cause);
  }
}