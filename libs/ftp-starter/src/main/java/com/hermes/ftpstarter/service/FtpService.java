package com.hermes.ftpstarter.service;

import com.hermes.ftpstarter.dto.FtpResponseDto;
import com.hermes.ftpstarter.exception.*;
import com.hermes.ftpstarter.properties.FtpProperties;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

@Service
public class FtpService {

  private final FtpProperties ftpProperties;

  public FtpService(FtpProperties ftpProperties) {
    this.ftpProperties = ftpProperties;
  }

  // 업로드
  public FtpResponseDto uploadFile(MultipartFile file) {
    if (file.isEmpty()) {
      throw new FtpEmptyFileException();
    }

    String originalName = file.getOriginalFilename();
    String storedName = UUID.randomUUID().toString();

    try (CloseableFTPClient ftpClient = new CloseableFTPClient();
        InputStream inputStream = file.getInputStream()) {

      connectAndLogin(ftpClient);
      ftpClient.setFileType(FTP.BINARY_FILE_TYPE);

      if (!ftpClient.changeWorkingDirectory(ftpProperties.getBaseDir())) {
        throw new FtpUploadException("FTP 디렉토리 변경 실패: " + ftpProperties.getBaseDir());
      }

      boolean stored = ftpClient.storeFile(storedName, inputStream);
      if (!stored) {
        throw new FtpUploadException(originalName);
      }

      return FtpResponseDto.builder().storedName(storedName).build();

    } catch (IOException e) {
      throw new FtpUploadException("FTP 업로드 중 오류 발생: " + originalName, e);
    }
  }

  // 삭제
  public void deleteFile(String storedName) {
    if (!existsFileByname(storedName)) {
      throw new FtpFileNotFoundException(storedName);
    }

    try (CloseableFTPClient ftpClient = new CloseableFTPClient()) {
      connectAndLogin(ftpClient);

      if (!ftpClient.changeWorkingDirectory(ftpProperties.getBaseDir())) {
        throw new FtpDeleteException("FTP 디렉토리 변경 실패: " + ftpProperties.getBaseDir());
      }

      boolean deleted = ftpClient.deleteFile(storedName);
      if (!deleted) {
        throw new FtpDeleteException(storedName);
      }

    } catch (IOException e) {
      throw new FtpDeleteException("FTP 삭제 중 오류 발생: " + storedName, e);
    }
  }

  // URL 조회
  public String getFileUrl(String storedName) {
    return "http://" + ftpProperties.getHost() + ":" + ftpProperties.getDownloadPort()
        + ftpProperties.getBaseDir() + "/" + storedName;
  }

  // 파일 존재 여부 확인
  public boolean existsFileByname(String filename) {
    try (CloseableFTPClient ftpClient = new CloseableFTPClient()) {
      connectAndLogin(ftpClient);

      if (!ftpClient.changeWorkingDirectory(ftpProperties.getBaseDir())) {
        throw new RuntimeException("FTP 디렉토리 변경 실패: " + ftpProperties.getBaseDir());
      }

      return ftpClient.listFiles(filename).length > 0;

    } catch (IOException e) {
      throw new RuntimeException("FTP 파일 존재 확인 중 오류 발생", e);
    }
  }

  // 공통 연결 및 로그인
  private void connectAndLogin(FTPClient ftpClient) throws IOException {
    ftpClient.connect(ftpProperties.getHost(), ftpProperties.getUploadPort());
    ftpClient.login(ftpProperties.getUser(), ftpProperties.getPassword());
    ftpClient.enterLocalPassiveMode();
  }

  // Closeable FTPClient
  private static class CloseableFTPClient extends FTPClient implements Closeable {
    @Override
    public void close() throws IOException {
      if (isConnected()) {
        logout();
        disconnect();
      }
    }
  }
}
