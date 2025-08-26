package com.hermes.communicationservice.file.exception;


public class FileMappingSaveException extends RuntimeException {
  public FileMappingSaveException(String message) {
    super(message);
  }

  public FileMappingSaveException(String message, Throwable cause) {
    super(message, cause);
  }
}
