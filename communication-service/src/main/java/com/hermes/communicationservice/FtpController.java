package com.hermes.communicationservice;



import com.hermes.ftpstarter.common.ApiResponse;
import com.hermes.ftpstarter.dto.FtpResponseDto;
import com.hermes.ftpstarter.service.FtpService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/ftp")
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "ftp", name = "enabled", havingValue = "true")
public class FtpController {

  @Autowired
  private final FtpService ftpService;

  // 파일 업로드
  @PostMapping("/upload")
  public ApiResponse<FtpResponseDto> upload(@RequestParam("file") MultipartFile file) {
    FtpResponseDto response = ftpService.uploadFile(file);
    return ApiResponse.success("업로드 성공", response);
  }

  // 파일 삭제
  @DeleteMapping("/delete")
  public ApiResponse<Object> delete(@RequestParam("filename") String filename) {
    ftpService.deleteFile(filename);
    return ApiResponse.success("삭제 성공");
  }

  // 다운로드 주소 반환
  @GetMapping("/file-url")
  public ApiResponse<FtpResponseDto> getFileUrl(@RequestParam String fileName) {
    FtpResponseDto response = FtpResponseDto.builder()
        .storedFileName(fileName)
        .build();
    return ApiResponse.success("다운로드 주소 반환 성공", response);
  }

}
