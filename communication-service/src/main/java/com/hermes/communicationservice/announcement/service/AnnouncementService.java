package com.hermes.communicationservice.announcement.service;


import com.hermes.communicationservice.announcement.dto.AnnouncementCreateRequestDto;
import com.hermes.communicationservice.announcement.dto.AnnouncementResponseDto;
import com.hermes.communicationservice.announcement.dto.AnnouncementSummaryDto;
import com.hermes.communicationservice.announcement.dto.AnnouncementUpdateRequestDto;
import com.hermes.communicationservice.announcement.entity.Announcement;
import com.hermes.communicationservice.announcement.repository.AnnouncementRepository;
import java.util.ArrayList;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import com.hermes.communicationservice.announcement.exception.AnnouncementNotFoundException;
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class AnnouncementService {

  private final AnnouncementRepository announcementRepository;


  // 생성
  @Transactional
  public AnnouncementResponseDto createAnnouncement(AnnouncementCreateRequestDto request, Long authorId) {

    Announcement announcement = Announcement.builder()
        .title(request.getTitle())
        .authorId(authorId)
        .displayAuthor(request.getDisplayAuthor())
        .content(request.getContent())
        .fileIds(request.getFileIds())
        .build();
    Announcement saved = announcementRepository.save(announcement);


    return AnnouncementResponseDto.builder()
        .id(saved.getId())
        .title(saved.getTitle())
        .displayAuthor(saved.getDisplayAuthor())
        .content(saved.getContent())
        .createdAt(saved.getCreatedAt())
        .fileIds(new ArrayList<>(saved.getFileIds()))
        .build();
  }

  // 단건 조회
  @Transactional
  public AnnouncementResponseDto getAnnouncement(Long id) {

    // 1. 공지사항 엔터티 조회
    Announcement announcement = announcementRepository.findByIdWithFileIds(id)
        .orElseThrow(() -> new AnnouncementNotFoundException(id));

    // 2. 조회수 증가
    announcementRepository.increaseViews(id);

    // 3. 응답 DTO 조립
    return AnnouncementResponseDto.builder()
        .id(announcement.getId())
        .title(announcement.getTitle())
        .displayAuthor(announcement.getDisplayAuthor())
        .content(announcement.getContent())
        .createdAt(announcement.getCreatedAt())
        .fileIds(new ArrayList<>(announcement.getFileIds()))
        .build();

  }

  // 전체 조회
  @Transactional(readOnly = true)
  public List<AnnouncementSummaryDto> getAllAnnouncementSummary() {
    return announcementRepository.findAllAnnouncementSummary();
  }


  // PATCH 수정
  @Transactional
  public AnnouncementResponseDto updateAnnouncement(AnnouncementUpdateRequestDto request, Long id, Long authorId) {
    // 1. 공지 로드
    Announcement announcement = announcementRepository.findById(id)
        .orElseThrow(() -> new AnnouncementNotFoundException(id));

    // 2. 공지 필드 부분 수정
    if (request.getTitle() != null) {
      announcement.setTitle(request.getTitle());
    }
    if (request.getContent() != null) {
      announcement.setContent(request.getContent());
    }
    if (request.getDisplayAuthor() != null) {
      announcement.setDisplayAuthor(request.getDisplayAuthor());
    }
    if (authorId != null) {
      announcement.setAuthorId(authorId);
    }
    if (request.getFileIds() != null) {
      announcement.setFileIds(request.getFileIds());
    }
    announcementRepository.save(announcement);

    return AnnouncementResponseDto.builder()
        .id(announcement.getId())
        .title(announcement.getTitle())
        .displayAuthor(announcement.getDisplayAuthor())
        .content(announcement.getContent())
        .createdAt(announcement.getCreatedAt())
        .fileIds(new ArrayList<>(announcement.getFileIds()))
        .build();

  }

  // 삭제
  @Transactional
  public void deleteAnnouncement(Long id) {
    Announcement announcement = announcementRepository.findById(id)
        .orElseThrow(() -> new AnnouncementNotFoundException(id));

    announcementRepository.delete(announcement);

    log.info("공지사항 삭제 완료 - id: {}", id);

  }

}

