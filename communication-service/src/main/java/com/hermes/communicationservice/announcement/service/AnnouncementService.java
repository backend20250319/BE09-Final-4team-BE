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
import com.hermes.ftpstarter.exception.FtpException;
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
    List<FileMapping> fileMappings = new ArrayList<>();
    List<FileMappingDto> fileMappingDtos = new ArrayList<>();

    try {
      // 1. 파일 업로드 (빈 파일 제외)
      List<MultipartFile> files = request.getMultipartFiles() != null
          ? request.getMultipartFiles().stream()
          .filter(f -> f != null && !f.isEmpty())
          .toList()
          : List.of();

      for (MultipartFile file : files) {
        FtpResponseDto ftpResponse = ftpService.uploadFile(file);
        FileMapping fileMapping = FileMapping.builder()
            .originalName(file.getOriginalFilename())
            .storedName(ftpResponse.getStoredName())
            .build();
        fileMappings.add(fileMapping);
      }

      // 2. Announcement 엔터티 생성 (파일 매핑 포함)
      Announcement announcement = request.toEntity(fileMappings);
      Announcement saved = announcementRepository.save(announcement);

      // 3. DTO 변환
      for (FileMapping fileMapping : fileMappings) {
        FileMappingDto dto = FileMappingDto.fromEntity(fileMapping, ftpService.getFileUrl(fileMapping.getStoredName()));
        fileMappingDtos.add(dto);
      }

      log.info("공지사항 생성 완료 - id: {}", saved.getId());
      return AnnouncementResponseDto.fromEntity(saved, fileMappingDtos);

    } catch (Exception e) {
      // FTP 업로드 롤백
      for (FileMapping f : fileMappings) {
        try {
          ftpService.deleteFile(f.getStoredName());
        } catch (Exception ignored) {}
      }
      throw new FileMappingSaveException("공지사항 생성 실패 - 파일 롤백", e);
    }
  }





  // 단건 조회
  @Transactional
  public AnnouncementResponseDto getAnnouncement(Long id, List<FileMappingDto> attachments) {
    Announcement announcement = announcementRepository.findById(id)
        .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 공지입니다."));
    // 조회수 증가
    announcement.setViews(announcement.getViews() + 1);

    return AnnouncementResponseDto.fromEntity(announcement, attachments);
  }

  // 전체 조회
  @Transactional(readOnly = true)
  public List<AnnouncementSummaryDto> getAllAnnouncementSummary() {
    return announcementRepository.findAllAnnouncementSummary();
  }


  // PATCH 수정
  @Transactional
  public AnnouncementResponseDto updateAnnouncement(AnnouncementUpdateRequestDto request,
      List<FileMappingDto> uploadedFiles) {

    Announcement announcement = announcementRepository.findById(request.getId())
        .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 공지입니다."));

    boolean updated = false;

    if (request.getTitle() != null) {
      announcement.setTitle(request.getTitle());
      updated = true;
    }
    if (request.getDisplayAuthor() != null) {
      announcement.setDisplayAuthor(request.getDisplayAuthor());
      updated = true;
    }
    if (request.getAuthorId() != null) {
      announcement.setAuthorId(request.getAuthorId());
      updated = true;
    }
    if (request.getContent() != null) {
      announcement.setContent(request.getContent());
      updated = true;
    }
    if (uploadedFiles != null && !uploadedFiles.isEmpty()) {
      announcement.setAttachments(
          uploadedFiles.stream()
              .map(FileMappingDto::toEntity)
              .collect(Collectors.toList())
      );
      updated = true;
    }

    if (updated) {
      Announcement saved = announcementRepository.save(announcement);

      log.info(
          "공지사항 수정 - id: {}, title: {}, displayAuthor: {}, content: {}, 첨부파일: {}, 작성자ID: {}",
          saved.getId(),
          saved.getTitle(),
          saved.getDisplayAuthor(),
          saved.getContent(),
          uploadedFiles,
          request.getAuthorId()
      );

      return AnnouncementResponseDto.fromEntity(saved, uploadedFiles);
    } else {
      log.info("공지사항 변경 사항 없음 - id: {}", announcement.getId());
      return AnnouncementResponseDto.fromEntity(announcement, uploadedFiles);
    }
  }

  // 삭제
  @Transactional
  public void deleteAnnouncement(Long id) {
    Announcement announcement = announcementRepository.findById(id)
        .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 공지입니다."));

    // 1. FTP 파일 삭제 -> DB 삭제
    announcement.getAttachments().forEach(file -> {
      try {
        fileMappingService.delete(file.getId());
      } catch (Exception e) {
        log.error("FTP 파일 삭제 실패 - id: {}", file.getId(), e);
      }
    });

    // 2. DB에서 공지 삭제 (첨부파일 매핑도 cascade 삭제)
    announcementRepository.delete(announcement);

    log.info("공지사항 삭제 완료 - id: {}", id);

  }

}

