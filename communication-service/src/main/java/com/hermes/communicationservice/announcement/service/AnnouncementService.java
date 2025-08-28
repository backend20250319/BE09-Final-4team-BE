package com.hermes.communicationservice.announcement.service;


import com.hermes.communicationservice.announcement.dto.AnnouncementCreateRequestDto;
import com.hermes.communicationservice.announcement.dto.AnnouncementResponseDto;
import com.hermes.communicationservice.announcement.dto.AnnouncementSummaryDto;
import com.hermes.communicationservice.announcement.dto.AnnouncementUpdateRequestDto;
import com.hermes.communicationservice.announcement.entity.Announcement;
import com.hermes.communicationservice.announcement.repository.AnnouncementRepository;
import com.hermes.communicationservice.file.exception.FileMappingSaveException;
import com.hermes.communicationservice.file.repository.FileMappingRepository;
import com.hermes.communicationservice.file.service.FileMappingService;
import com.hermes.ftpstarter.dto.FtpResponseDto;
import com.hermes.ftpstarter.service.FtpService;
import java.util.ArrayList;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import com.hermes.communicationservice.file.dto.FileMappingDto;
import com.hermes.communicationservice.file.entity.FileMapping;
import org.springframework.web.multipart.MultipartFile;
import com.hermes.communicationservice.announcement.dto.FileResponseDto;
import com.hermes.communicationservice.announcement.exception.AnnouncementNotFoundException;
import com.hermes.communicationservice.file.exception.FileMappingNotFoundException;
import com.hermes.communicationservice.file.enums.OwnerType;
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class AnnouncementService {

  private final AnnouncementRepository announcementRepository;
  private final FileMappingService fileMappingService;
  private final FtpService ftpService;
  private final FileMappingRepository fileMappingRepository;


  // 생성
  @Transactional
  public AnnouncementResponseDto createAnnouncement(AnnouncementCreateRequestDto request) {
    // 1. 공지사항 저장
    Announcement announcement = Announcement.builder()
        .title(request.getTitle())
        .authorId(request.getAuthorId())
        .displayAuthor(request.getDisplayAuthor())
        .content(request.getContent())
        .build();
    Announcement saved = announcementRepository.save(announcement);

    // 2. 파일 저장 및 FileMapping 생성 (FTP 업로드 보상 로직 포함)
    List<FileMapping> fileMappings = new ArrayList<>();
    List<String> uploadedStoredNames = new ArrayList<>();
    System.out.println("before");
    try {
    System.out.println("start");
      for (MultipartFile file : request.getMultipartFiles()) {
        if (file == null || file.isEmpty()) {
        System.out.println("skip");
          log.warn("skip empty file");
          continue;
        }
        if (file != null && !file.isEmpty()) {
          FtpResponseDto ftpResponse = ftpService.uploadFile(file);
          uploadedStoredNames.add(ftpResponse.getStoredName());
          FileMapping fileMapping = FileMapping.builder()
              .originalName(file.getOriginalFilename())
              .storedName(ftpResponse.getStoredName())
              .ownerType(OwnerType.ANNOUNCEMENT)
              .ownerId(saved.getId())
              .build();
          fileMappings.add(fileMapping);
          System.out.println(fileMapping);
        }
      }
      if (!fileMappings.isEmpty()) {
        log.info("saving fileMappings - count={}", fileMappings.size());
        fileMappingRepository.saveAll(fileMappings);
        log.info("saved fileMappings");
      }
    } catch (Exception e) {
      // FTP 업로드 실패 시 보상: 이미 업로드된 파일들 삭제 시도
      for (String storedName : uploadedStoredNames) {
        try {
          ftpService.deleteFile(storedName);
        } catch (Exception ignore) {
        }
      }
      throw new FileMappingSaveException("공지사항 생성 실패 - 파일 업로드 중 오류", e);
    }

    // 3. 파일 응답 DTO 변환
    List<FileResponseDto> fileDtos = fileMappings.stream()
        .map(f -> FileResponseDto.builder()
            .id(f.getId())
            .originalName(f.getOriginalName())
            .storedName(f.getStoredName())
            .url(ftpService.getFileUrl(f.getStoredName()))
            .build())
        .collect(Collectors.toList());

    // 4. 공지사항 응답 DTO 반환
    return AnnouncementResponseDto.builder()
        .id(saved.getId())
        .title(saved.getTitle())
        .displayAuthor(saved.getDisplayAuthor())
        .content(saved.getContent())
        .createdAt(saved.getCreatedAt())
        .files(fileDtos)
        .build();
  }

  // 단건 조회
  @Transactional
  public AnnouncementResponseDto getAnnouncement(Long id) {
    // 1. 조회수 원자적 증가
    announcementRepository.increaseViews(id);
    
    // 2. 공지사항 엔터티 조회
    Announcement announcement = announcementRepository.findById(id)
        .orElseThrow(() -> new AnnouncementNotFoundException(id));
    
    // 3. 파일 리스트 조회 (ownerType/ownerId로)
    List<FileMapping> fileMappings = fileMappingRepository.findByOwnerTypeAndOwnerId(OwnerType.ANNOUNCEMENT, id);
    
    // 4. 파일 리스트를 FileResponseDto로 변환
    List<FileResponseDto> fileDtos = fileMappings.stream()
        .map(f -> FileResponseDto.builder()
            .id(f.getId())
            .originalName(f.getOriginalName())
            .storedName(f.getStoredName())
            .url(ftpService.getFileUrl(f.getStoredName()))
            .build())
        .collect(Collectors.toList());
    
    announcementRepository.increaseViews(id);

    // 5. 응답 DTO 조립
    return AnnouncementResponseDto.builder()
        .id(announcement.getId())
        .title(announcement.getTitle())
        .displayAuthor(announcement.getDisplayAuthor())
        .content(announcement.getContent())
        .createdAt(announcement.getCreatedAt())
        .files(fileDtos)
        .build();
  }

  // 전체 조회
  @Transactional(readOnly = true)
  public List<AnnouncementSummaryDto> getAllAnnouncementSummary() {
    return announcementRepository.findAllAnnouncementSummary();
  }


  // PATCH 수정 ->
  @Transactional
  public AnnouncementResponseDto updateAnnouncement(AnnouncementUpdateRequestDto request) {
    // 1) 공지 로드
    Announcement announcement = announcementRepository.findById(request.getId())
        .orElseThrow(() -> new AnnouncementNotFoundException(request.getId()));

    // 2) 공지 필드 부분 수정
    if (request.getTitle() != null) {
      announcement.setTitle(request.getTitle());
    }
    if (request.getContent() != null) {
      announcement.setContent(request.getContent());
    }
    if (request.getDisplayAuthor() != null) {
      announcement.setDisplayAuthor(request.getDisplayAuthor());
    }
    if (request.getAuthorId() != null) {
      announcement.setAuthorId(request.getAuthorId());
    }
    announcementRepository.save(announcement);

    // 3) 파일 삭제 (FTP best-effort + DB 삭제)
    if (request.getFilesToDelete() != null && !request.getFilesToDelete().isEmpty()) {
      for (Long fileId : request.getFilesToDelete()) {
        FileMapping fm = fileMappingRepository.findById(fileId)
            .orElseThrow(() -> new FileMappingNotFoundException(fileId));
        try {
          ftpService.deleteFile(fm.getStoredName()); // 실패해도 계속 진행
        } catch (Exception ex) {
          log.error("FTP 파일 삭제 실패 storedName={}", fm.getStoredName(), ex);
        }
        fileMappingRepository.delete(fm);
      }
    }

    // 4) 파일 업로드 (보상 로직 포함) + DB 저장
    List<String> uploadedStoredNames = new ArrayList<>();
    List<FileMapping> newFileMappings = new ArrayList<>();
    try {
      if (request.getFilesToUpload() != null && !request.getFilesToUpload().isEmpty()) {
        for (MultipartFile file : request.getFilesToUpload()) {
          if (file == null || file.isEmpty()) continue;
          FtpResponseDto uploaded = ftpService.uploadFile(file); // 실패 시 예외
          uploadedStoredNames.add(uploaded.getStoredName());

          FileMapping fm = FileMapping.builder()
              .originalName(file.getOriginalFilename())
              .storedName(uploaded.getStoredName())
              .ownerType(OwnerType.ANNOUNCEMENT)
              .ownerId(announcement.getId())
              .build();
          newFileMappings.add(fm);
        }
        if (!newFileMappings.isEmpty()) {
          fileMappingRepository.saveAll(newFileMappings);
        }
      }
    } catch (Exception e) {
      // 보상: 이미 업로드된 파일들 삭제
      for (String storedName : uploadedStoredNames) {
        try {
          ftpService.deleteFile(storedName);
        } catch (Exception ignore) { }
      }
      throw new FileMappingSaveException("파일 업로드 중 오류로 수정 중단", e);
    }

    // 5) 최신 파일 목록 조회 → DTO 변환
    List<FileMapping> fileMappings =
        fileMappingRepository.findByOwnerTypeAndOwnerId(OwnerType.ANNOUNCEMENT, announcement.getId());

    List<FileResponseDto> files = fileMappings.stream()
        .map(f -> FileResponseDto.builder()
            .id(f.getId())
            .originalName(f.getOriginalName())
            .storedName(f.getStoredName())
            .url(ftpService.getFileUrl(f.getStoredName()))
            .build())
        .collect(Collectors.toList());

    // 6) 응답 DTO 조립
    return AnnouncementResponseDto.builder()
        .id(announcement.getId())
        .title(announcement.getTitle())
        .displayAuthor(announcement.getDisplayAuthor())
        .content(announcement.getContent())
        .createdAt(announcement.getCreatedAt())
        .files(files)
        .build();
  }

  // 삭제
  @Transactional
  public void deleteAnnouncement(Long id) {
    Announcement announcement = announcementRepository.findById(id)
        .orElseThrow(() -> new AnnouncementNotFoundException(id));

    // 1) 파일 목록 조회 (ownerType/ownerId)
    List<FileMapping> files = fileMappingRepository.findByOwnerTypeAndOwnerId(OwnerType.ANNOUNCEMENT, id);

    // 2) DB 삭제 (파일 → 공지 순서 권장)
    if (!files.isEmpty()) {
      fileMappingRepository.deleteAll(files);
    }
    announcementRepository.delete(announcement);

    // 3) FTP 삭제 (best-effort)
    for (FileMapping f : files) {
      try {
        ftpService.deleteFile(f.getStoredName());
      } catch (Exception e) {
        log.error("FTP 파일 삭제 실패 - id: {}", f.getId(), e);
      }
    }

    log.info("공지사항 삭제 완료 - id: {}, files: {}", id, files.size());
  }

}

