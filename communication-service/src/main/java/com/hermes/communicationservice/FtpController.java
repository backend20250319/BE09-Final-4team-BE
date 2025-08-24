package com.hermes.communicationservice;

import com.hermes.api.common.ApiResponse;
import com.hermes.ftpstarter.dto.FtpResponseDto;
import com.hermes.ftpstarter.service.FtpService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/ftp")
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "ftp", name = "enabled", havingValue = "true")
public class FtpController {

  private final FtpService ftpService;

  // 파일 업로드
  @PostMapping("/upload")
  public ResponseEntity<ApiResponse<FtpResponseDto>> upload(@RequestParam("file") MultipartFile file) {
    FtpResponseDto response = ftpService.uploadFile(file);
    ApiResponse<FtpResponseDto> apiResponse = ApiResponse.success("업로드 성공", response);
    return ResponseEntity.status(HttpStatus.CREATED).body(apiResponse);
  }

  // 파일 삭제
  @DeleteMapping("/delete")
  public ResponseEntity<ApiResponse<Object>> delete(@RequestParam("storedName") String storedName) {
    ftpService.deleteFile(storedName);
    ApiResponse<Object> apiResponse = ApiResponse.success("삭제 성공");
    return ResponseEntity.status(HttpStatus.NO_CONTENT).body(apiResponse);
  }

  // 다운로드 주소 반환
  @GetMapping("/file-url")
  public ResponseEntity<ApiResponse<String>> getFileUrl(@RequestParam String storedName) {
    String url = ftpService.getFileUrl(storedName);
    ApiResponse<String> apiResponse = ApiResponse.success("다운로드 주소 반환 성공", url);
    return ResponseEntity.ok(apiResponse);
  }
}
