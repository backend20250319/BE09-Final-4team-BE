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

}
