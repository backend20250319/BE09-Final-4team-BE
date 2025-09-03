package com.hermes.api.common;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * @deprecated ResponseEntity에는 HTTP 상태 코드가 포함되어 있어, 이미 성공/실패를 충분히 표현할 수 있습니다.
 *             ApiResult로 한번 더 감싸면 응답 구조를 복잡하게 만들 뿐이므로 더 이상 사용하지 않습니다.
 *             대신 적절한 예외를 던지고, Swagger로 상태 코드를 정확하게 문서화 해주세요.
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Deprecated
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