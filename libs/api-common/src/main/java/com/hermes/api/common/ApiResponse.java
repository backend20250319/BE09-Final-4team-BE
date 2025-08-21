package com.hermes.api.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse<T> {
    private String status; // SUCCESS, REJECTED, FAILURE
    private String message;
    private T data;
    
    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>("SUCCESS", message, data);
    }
    
    public static <T> ApiResponse<T> success(String message) {
        return new ApiResponse<>("SUCCESS", message, null);
    }
    
    public static <T> ApiResponse<T> rejected(String message) {
        return new ApiResponse<>("REJECTED", message, null);
    }
    
    public static <T> ApiResponse<T> failure(String message) {
        return new ApiResponse<>("FAILURE", message, null);
    }
} 