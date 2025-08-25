package com.hermes.api.common;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApiResult<T> {

  private String status; // SUCCESS, REJECTED, FAILURE
  private String message;
  private T data;

  public static <T> ApiResult<T> success() {
    return ApiResult.<T>builder()
        .status("SUCCESS")
        .message("성공")
        .build();
  }

  public static <T> ApiResult<T> success(String message) {
    return ApiResult.<T>builder()
        .status("SUCCESS")
        .message(message)
        .build();
  }

  public static <T> ApiResult<T> success(T data) {
    return ApiResult.<T>builder()
        .status("SUCCESS")
        .message("성공")
        .data(data)
        .build();
  }

  public static <T> ApiResult<T> success(String message, T data) {
    return ApiResult.<T>builder()
        .status("SUCCESS")
        .message(message)
        .data(data)
        .build();
  }

  
  public static <T> ApiResult<T> failure() {
    return ApiResult.<T>builder()
        .status("FAILURE")
        .message("실패")
        .build();
  }

  public static <T> ApiResult<T> failure(String message) {
    return ApiResult.<T>builder()
        .status("FAILURE")
        .message(message)
        .build();
  }

  public static <T> ApiResult<T> failure(String message, T data) {
    return ApiResult.<T>builder()
        .status("FAILURE")
        .message(message)
        .data(data)
        .build();
  }

  
  public static <T> ApiResult<T> rejected() {
    return ApiResult.<T>builder()
        .status("REJECTED")
        .message("거절됨")
        .build();
  }

  public static <T> ApiResult<T> rejected(String message) {
    return ApiResult.<T>builder()
        .status("REJECTED")
        .message(message)
        .build();
  }

  public static <T> ApiResult<T> rejected(String message, T data) {
    return ApiResult.<T>builder()
        .status("REJECTED")
        .message(message)
        .data(data)
        .build();
  }
  
}