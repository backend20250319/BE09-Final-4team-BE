package com.hermes.communicationservice.ftp.service;

import com.hermes.communicationservice.ftp.entity.FileMapping;
import com.hermes.communicationservice.ftp.exception.FileMappingNotFoundException;
import com.hermes.communicationservice.ftp.exception.FileMappingNotFoundException.Key;
import com.hermes.communicationservice.ftp.exception.FileMappingSaveException;
import com.hermes.communicationservice.ftp.repository.FileMappingRepository;
import com.hermes.communicationservice.ftp.dto.FileMappingDto;
import com.hermes.ftpstarter.dto.FtpResponseDto;
import com.hermes.ftpstarter.exception.FtpException;
import com.hermes.ftpstarter.service.FtpService;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileMappingService {

  private final FtpService ftpService;
  private final FileMappingRepository fileMappingRepository;

  // 파일 업로드 + 매핑 테이블 저장
  @Transactional
  public List<FileMappingDto> uploadFiles(List<MultipartFile> files) {
    List<FileMappingDto> uploadedFiles = new ArrayList<>();

    for (MultipartFile file : files) {
      try {
        FtpResponseDto ftpResponse = ftpService.uploadFile(file);

        FileMapping fileMapping = FileMapping.builder()
            .originalName(file.getOriginalFilename())
            .storedName(ftpResponse.getStoredName())
            .build();

        FileMapping saved = fileMappingRepository.save(fileMapping);
        FileMappingDto fileResponse = FileMappingDto.fromEntity(saved, ftpResponse.getUrl());

        uploadedFiles.add(fileResponse);

      } catch (Exception e) {
        // DB 롤백 + 이미 업로드한 FTP 파일 삭제
        for (FileMappingDto uploaded : uploadedFiles) {
          try {
            ftpService.deleteFile(uploaded.getStoredName());
          } catch (Exception ignored) {
            log.error("FTP 파일 롤백 실패: {}", uploaded.getStoredName());
          }
        }
        if (e instanceof FtpException) {
          throw (FtpException) e;
        }
        throw new FileMappingSaveException("파일 매핑 실패", e);

      }
    }

    return uploadedFiles;
  }

  // FTP 파일 삭제 -> DB 삭제
  @Transactional
  public void delete(Long id) {
    FileMapping fileMapping = fileMappingRepository.findById(id)
        .orElseThrow(() -> new FileMappingNotFoundException(Key.ID, String.valueOf(id)));

    // FTP에서 파일 삭제
    ftpService.deleteFile(fileMapping.getStoredName());

    // DB에서 매핑 정보 삭제
    fileMappingRepository.delete(fileMapping);
  }

  // 파일 아이디로 조회
  @Transactional(readOnly = true)
  public FileMappingDto getFileById(Long id) {
    FileMapping fileMapping = fileMappingRepository.findById(id)
        .orElseThrow(() -> new FileMappingNotFoundException(Key.ID, String.valueOf(id)));

    String url = ftpService.getFileUrl(fileMapping.getStoredName());
    return FileMappingDto.fromEntity(fileMapping, url);
  }

  // 저장된 파일명(unique함)으로 파일 정보 조회
  @Transactional(readOnly = true)
  public FileMappingDto getFileByStoredName(String storedName) {
    FileMapping fileMapping = fileMappingRepository.findByStoredName(storedName)
        .orElseThrow(() -> new FileMappingNotFoundException(Key.STORED_NAME, storedName));

    String url = ftpService.getFileUrl(fileMapping.getStoredName());
    return FileMappingDto.fromEntity(fileMapping, url);
  }

}