package com.hermes.ftpspringbootstarter.service;

import com.hermes.ftpspringbootstarter.dto.FtpResponseDto;
import com.hermes.ftpspringbootstarter.properties.FtpProperties;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

@Service
public class FtpService {

  private final FtpProperties ftpProperties;

  public FtpService(FtpProperties ftpProperties) {
    this.ftpProperties = ftpProperties;
  }

  public FtpResponseDto uploadFile(MultipartFile file) {
    FTPClient ftpClient = new FTPClient();
    String originalName = file.getOriginalFilename();
    String uniqueName = UUID.randomUUID() + "_" + originalName; // UUID 붙여서 고유 이름 생성

    try (InputStream inputStream = file.getInputStream()) {
      ftpClient.connect(ftpProperties.getHost(), ftpProperties.getUploadPort());
      ftpClient.login(ftpProperties.getUser(), ftpProperties.getPassword());
      ftpClient.enterLocalPassiveMode();
      ftpClient.setFileType(FTP.BINARY_FILE_TYPE);

      if (!ftpClient.changeWorkingDirectory(ftpProperties.getBaseDir())) {
        throw new RuntimeException("FTP 디렉토리 변경 실패: " + ftpProperties.getBaseDir());
      }

      boolean stored = ftpClient.storeFile(uniqueName, inputStream);
        if (!stored) {
            throw new RuntimeException("FTP 업로드 실패: " + uniqueName);
        }

      return FtpResponseDto.builder().storedFileName(uniqueName).build();

    } catch (IOException e) {
      throw new RuntimeException("FTP 업로드 중 오류 발생", e);
    } finally {
      try {
          if (ftpClient.isConnected()) {
              ftpClient.logout();
          }
        ftpClient.disconnect();
      } catch (IOException ignored) {
      }
    }
  }

  public String getFileUrl(String fileName) {
    return "http://" + ftpProperties.getHost() + ":" + ftpProperties.getDownloadPort()
        + ftpProperties.getBaseDir() + "/" + fileName;
  }

  public void deleteFile(String filename) {
    if (!existsFileByname(filename)) {
      throw new RuntimeException("존재하지 않는 파일명: " + filename);
    }

    FTPClient ftpClient = new FTPClient();
    try {
      ftpClient.connect(ftpProperties.getHost(), ftpProperties.getUploadPort());
      ftpClient.login(ftpProperties.getUser(), ftpProperties.getPassword());
      ftpClient.enterLocalPassiveMode();

      if (!ftpClient.changeWorkingDirectory(ftpProperties.getBaseDir())) {
        throw new RuntimeException("FTP 디렉토리 변경 실패: " + ftpProperties.getBaseDir());
      }

      boolean deleted = ftpClient.deleteFile(filename);
        if (!deleted) {
            throw new RuntimeException("FTP 파일 삭제 실패: " + filename);
        }

    } catch (IOException e) {
      throw new RuntimeException("FTP 삭제 중 오류 발생", e);
    } finally {
      try {
          if (ftpClient.isConnected()) {
              ftpClient.logout();
          }
        ftpClient.disconnect();
      } catch (IOException ignored) {
      }
    }
  }

  // 존재하는 파일명(UUID포함)인지 확인
  public boolean existsFileByname(String filename) {
    FTPClient ftpClient = new FTPClient();
    try {
      ftpClient.connect(ftpProperties.getHost(), ftpProperties.getUploadPort());
      ftpClient.login(ftpProperties.getUser(), ftpProperties.getPassword());
      ftpClient.enterLocalPassiveMode();

      if (!ftpClient.changeWorkingDirectory(ftpProperties.getBaseDir())) {
        throw new RuntimeException("FTP 디렉토리 변경 실패: " + ftpProperties.getBaseDir());
      }

      return ftpClient.listFiles(filename).length > 0;

    } catch (IOException e) {
      throw new RuntimeException("FTP 파일 존재 확인 중 오류 발생", e);
    } finally {
      try {
        if (ftpClient.isConnected()) ftpClient.logout();
        ftpClient.disconnect();
      } catch (IOException ignored) {}
    }
  }

}
