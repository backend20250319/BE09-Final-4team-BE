package com.hermes.communicationservice.ftp.controller;

import com.hermes.api.common.ApiResult;
import com.hermes.communicationservice.ftp.dto.FileMappingDto;
import com.hermes.communicationservice.ftp.service.FileMappingService;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/files")
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "ftp", name = "enabled", havingValue = "true")
public class FileMappingController {

  private final FileMappingService fileMappingService;

  // 파일 업로드
  @PostMapping("/upload")
  public ResponseEntity<ApiResult<List<FileMappingDto>>> uploadMultiple(
      @RequestParam("files") List<MultipartFile> files) {

    List<FileMappingDto> responses = fileMappingService.uploadFiles(files);

    ApiResult<List<FileMappingDto>> apiResult = ApiResult.success("업로드 성공", responses);
    return ResponseEntity.status(HttpStatus.CREATED).body(apiResult);
  }


  // 파일 삭제
  @DeleteMapping("/delete")
  public ResponseEntity<ApiResult<Object>> delete(@RequestParam("id") Long id) {
    fileMappingService.delete(id);
    ApiResult<Object> apiResult = ApiResult.success("삭제 성공");
    return ResponseEntity.status(HttpStatus.NO_CONTENT).body(apiResult);
  }

  // id로 파일 정보 조회
  @GetMapping("/id/{id}")
  public ResponseEntity<FileMappingDto> getFileByOriginalName(@PathVariable Long id) {
    FileMappingDto response = fileMappingService.getFileById(id);
    return ResponseEntity.ok(response);
  }

  // 저장된 이름으로 파일 정보 조회
  @GetMapping("/storedName/{storedName}")
  public ResponseEntity<FileMappingDto> getFileByStoredName(@PathVariable String storedName) {
    FileMappingDto response = fileMappingService.getFileByStoredName(storedName);
    return ResponseEntity.ok(response);
  }

}
