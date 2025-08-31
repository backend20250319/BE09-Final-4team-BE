package com.hermes.attachmentservice.service;


import com.hermes.attachmentservice.dto.UploadResponse;
import com.hermes.attachmentservice.entity.AttachmentFile;
import com.hermes.attachmentservice.properties.AttachmentProperties;
import com.hermes.attachmentservice.repository.AttachmentFileRepository;
import com.hermes.attachmentservice.exception.FileNotFoundException;
import com.hermes.attachmentservice.exception.FileUploadException;
import com.hermes.attachmentservice.exception.FileStorageException;
import com.hermes.attachment.dto.AttachmentMetadata;
import java.io.Closeable;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
public class AttachmentService {

  private final AttachmentFileRepository attachmentFileRepository;
  private final AttachmentProperties attachmentProperties;
  private final String downloadBaseUrl;

  public AttachmentService(AttachmentFileRepository attachmentFileRepository,
      AttachmentProperties attachmentProperties) {
    this.attachmentFileRepository = attachmentFileRepository;
    this.attachmentProperties = attachmentProperties;
    this.downloadBaseUrl =
        "http://" + attachmentProperties.getHost() + ":" + attachmentProperties.getDownloadPort()
            + attachmentProperties.getBaseDir();
  }


  @Transactional
  public List<UploadResponse> uploadFiles(List<MultipartFile> files, Long uploadedBy) throws IOException {
    if (files == null || files.isEmpty()) {
      throw new FileUploadException("업로드할 파일이 없습니다");
    }

    List<UploadResponse> uploadedFileIds = new ArrayList<>();
    List<String> uploadedStoredNames = new ArrayList<>();

    // FTP 연결 재사용으로 효율성 향상
    try (CloseableFTPClient ftpClient = new CloseableFTPClient()) {
      // FTP 연결 및 초기 설정
      connectAndLogin(ftpClient);
      ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
      
      if (!ftpClient.changeWorkingDirectory(attachmentProperties.getBaseDir())) {
        throw new FileUploadException("FTP 디렉토리 변경 실패: " + attachmentProperties.getBaseDir());
      }

      // 각 파일 업로드 처리
      for (MultipartFile file : files) {
        if (file.isEmpty()) {
          throw new FileUploadException("빈 파일은 업로드할 수 없습니다: " + file.getOriginalFilename());
        }

        try (InputStream inputStream = file.getInputStream()) {
          String fileId = UUID.randomUUID().toString();
          String storedUUID = UUID.randomUUID().toString();
          String extension = getFileExtension(file.getOriginalFilename());
          String storedName = storedUUID + extension;

          // FTP 업로드
          boolean stored = ftpClient.storeFile(storedName, inputStream);
          
          if (!stored) {
            throw new FileUploadException("FTP 업로드 실패: " + file.getOriginalFilename());
          }

          // 성공 후에만 리스트에 추가 (데이터 일관성 보장)
          uploadedFileIds.add(UploadResponse.builder().fileId(fileId).build());
          uploadedStoredNames.add(storedName);

          // DB 저장
          AttachmentFile attachmentFile = AttachmentFile.builder()
              .fileId(fileId)
              .storedName(storedName)
              .fileSize(file.getSize())
              .contentType(file.getContentType())
              .originalFileName(file.getOriginalFilename())
              .createdAt(LocalDateTime.now())
              .uploadedBy(uploadedBy)
              .build();

          attachmentFileRepository.save(attachmentFile);

        } catch (Exception e) {
          // 실패 시 이미 업로드된 파일들 롤백
          rollbackUploadedFiles(ftpClient, uploadedStoredNames);
          throw new FileUploadException("파일 업로드 중 오류 발생, 롤백 처리됨: " + file.getOriginalFilename(), e);
        }
      }

    } catch (IOException e) {
      throw new FileUploadException("FTP 연결 오류", e);
    }

    return uploadedFileIds;
  }

  @Transactional(readOnly = true)
  public AttachmentMetadata getFileMetadata(String fileId) {
    AttachmentFile file = attachmentFileRepository.findById(fileId)
        .orElseThrow(() -> new FileNotFoundException(fileId));
    
    return AttachmentMetadata.builder()
        .fileId(file.getFileId())
        .originalFileName(file.getOriginalFileName())
        .fileSize(file.getFileSize())
        .contentType(file.getContentType())
        .filePath(downloadBaseUrl + "/" + file.getStoredName())
        .build();
  }

  @Transactional(readOnly = true)
  public Resource getFileResource(String fileId) {
    AttachmentFile file = attachmentFileRepository.findById(fileId)
        .orElseThrow(() -> new FileNotFoundException(fileId));

    try {
      CloseableFTPClient ftpClient = new CloseableFTPClient();
      
      // FTP 연결
      connectAndLogin(ftpClient);
      ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
      
      if (!ftpClient.changeWorkingDirectory(attachmentProperties.getBaseDir())) {
        throw new FileStorageException("FTP 디렉토리 변경 실패: " + attachmentProperties.getBaseDir());
      }

      // FTP에서 파일 InputStream 가져오기
      InputStream inputStream = ftpClient.retrieveFileStream(file.getStoredName());
      
      if (inputStream == null) {
        throw new FileStorageException("FTP 파일 스트림 생성 실패: " + fileId);
      }

      // FTP 연결을 유지하면서 InputStreamResource 반환
      return new FTPInputStreamResource(inputStream, ftpClient, file.getFileSize());
      
    } catch (IOException e) {
      throw new FileStorageException("FTP 파일 리소스 생성 중 오류: " + fileId, e);
    }
  }

  @Transactional
  public void deleteFile(String fileId) {
    AttachmentFile file = attachmentFileRepository.findById(fileId)
        .orElseThrow(() -> new FileNotFoundException(fileId));

      // DB에서 메타데이터 삭제
      attachmentFileRepository.deleteById(fileId);
 }

  private String getFileExtension(String originalFileName) {
    if (originalFileName == null || !originalFileName.contains(".")) {
      return "";
    }
    return originalFileName.substring(originalFileName.lastIndexOf("."));
  }

  // 공통 연결 및 로그인
  private void connectAndLogin(FTPClient ftpClient) throws IOException {
    ftpClient.connect(attachmentProperties.getHost(), attachmentProperties.getUploadPort());
    ftpClient.login(attachmentProperties.getUser(), attachmentProperties.getPassword());
    ftpClient.enterLocalPassiveMode();
  }

  // 롤백 전용 메서드
  private void rollbackUploadedFiles(CloseableFTPClient ftpClient,
      List<String> uploadedStoredNames) {
    for (String storedName : uploadedStoredNames) {
      try {
        ftpClient.deleteFile(storedName);
      } catch (IOException ex) {
        // 롤백 실패 시 로그만 남김
        log.error("FTP 롤백 실패, 파일 삭제 못함: {}", storedName, ex);
      }
    }
  }

  private static class CloseableFTPClient extends FTPClient implements Closeable {

    @Override
    public void close() throws IOException {
      if (isConnected()) {
        logout();
        disconnect();
      }
    }
  }

  // FTP InputStream과 연결을 함께 관리하는 Resource
  private static class FTPInputStreamResource extends InputStreamResource {
    private final CloseableFTPClient ftpClient;

    public FTPInputStreamResource(InputStream inputStream, CloseableFTPClient ftpClient, long contentLength) {
      super(inputStream);
      this.ftpClient = ftpClient;
    }

    @Override
    public InputStream getInputStream() throws IOException {
      return new InputStream() {
        private final InputStream delegate = FTPInputStreamResource.super.getInputStream();

        @Override
        public int read() throws IOException {
          return delegate.read();
        }

        @Override
        public int read(byte[] b) throws IOException {
          return delegate.read(b);
        }

        @Override
        public int read(byte[] b, int off, int len) throws IOException {
          return delegate.read(b, off, len);
        }

        @Override
        public void close() throws IOException {
          try {
            delegate.close();
          } finally {
            // InputStream이 닫히면 FTP 연결도 함께 정리
            if (ftpClient != null) {
              ftpClient.close();
            }
          }
        }
      };
    }
  }
}