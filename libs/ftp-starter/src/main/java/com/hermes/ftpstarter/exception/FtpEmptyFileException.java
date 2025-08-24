package com.hermes.ftpstarter.exception;

public class FtpEmptyFileException extends FtpUploadException {
  public FtpEmptyFileException() {
    super("업로드할 파일이 비어있습니다.");
  }
}
