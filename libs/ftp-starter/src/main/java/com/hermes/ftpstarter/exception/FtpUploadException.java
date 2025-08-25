package com.hermes.ftpstarter.exception;

public class FtpUploadException extends FtpException {
  public FtpUploadException(String fileName) {
    super("FTP 업로드 실패: " + fileName);
  }

  public FtpUploadException(String message, Throwable cause) {
    super(message, cause);
  }
}
