package com.hermes.communicationservice.ftp.exception;

public class FileMappingNotFoundException extends RuntimeException {
  public enum Key { ORIGINAL_NAME, STORED_NAME, ID }

  public FileMappingNotFoundException(Key key, String value) {
    super("매핑되는 파일을 찾을 수 없습니다 (" + key + "): " + value);
  }
}

