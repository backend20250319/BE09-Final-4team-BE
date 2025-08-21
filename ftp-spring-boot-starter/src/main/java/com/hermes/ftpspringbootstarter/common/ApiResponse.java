package com.hermes.ftpspringbootstarter.common;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApiResponse<T> {

  private String status; // SUCCESS, REJECTED, FAILURE
  private String message;
  private T data;

  public static <T> ApiResponse<T> success() {
    return ApiResponse.<T>builder()
        .status("SUCCESS")
        .message("성공")
        .build();
  }

  public static <T> ApiResponse<T> success(String message) {
    return ApiResponse.<T>builder()
        .status("SUCCESS")
        .message(message)
        .build();
  }

  public static <T> ApiResponse<T> success(T data) {
    return ApiResponse.<T>builder()
        .status("SUCCESS")
        .message("성공")
        .data(data)
        .build();
  }

  public static <T> ApiResponse<T> success(String message, T data) {
    return ApiResponse.<T>builder()
        .status("SUCCESS")
        .message(message)
        .data(data)
        .build();
  }


  public static <T> ApiResponse<T> failure() {
    return ApiResponse.<T>builder()
        .status("FAILURE")
        .message("실패")
        .build();
  }

  public static <T> ApiResponse<T> failure(String message) {
    return ApiResponse.<T>builder()
        .status("FAILURE")
        .message(message)
        .build();
  }

  public static <T> ApiResponse<T> failure(String message, T data) {
    return ApiResponse.<T>builder()
        .status("FAILURE")
        .message(message)
        .data(data)
        .build();
  }


  public static <T> ApiResponse<T> rejected() {
    return ApiResponse.<T>builder()
        .status("REJECTED")
        .message("거절됨")
        .build();
  }

  public static <T> ApiResponse<T> rejected(String message) {
    return ApiResponse.<T>builder()
        .status("REJECTED")
        .message(message)
        .build();
  }

  public static <T> ApiResponse<T> rejected(String message, T data) {
    return ApiResponse.<T>builder()
        .status("REJECTED")
        .message(message)
        .data(data)
        .build();
  }

}
