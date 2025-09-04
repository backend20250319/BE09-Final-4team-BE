package com.hermes.communicationservice.announcement.service;


import com.hermes.communicationservice.announcement.dto.AnnouncementCreateRequestDto;
import com.hermes.communicationservice.announcement.dto.AnnouncementResponseDto;
import com.hermes.communicationservice.announcement.dto.AnnouncementSummaryDto;
import com.hermes.communicationservice.announcement.dto.AnnouncementUpdateRequestDto;
import com.hermes.communicationservice.announcement.entity.Announcement;
import com.hermes.communicationservice.announcement.repository.AnnouncementRepository;
import com.hermes.communicationservice.client.UserServiceClient;
import com.hermes.communicationservice.notification.service.NotificationService;
import com.hermes.notification.dto.NotificationRequest;
import com.hermes.notification.enums.NotificationType;
import com.hermes.notification.publisher.NotificationPublisher;
import java.time.LocalDateTime;
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
  private final NotificationPublisher notificationPublisher;
  private final UserServiceClient userServiceClient;
  private final NotificationService notificationService;


  // 생성
  @Transactional
  public AnnouncementResponseDto createAnnouncement(AnnouncementCreateRequestDto request, Long authorId, String authorization) {

    Announcement announcement = Announcement.builder()
        .title(request.getTitle())
        .authorId(authorId)
        .displayAuthor(request.getDisplayAuthor())
        .content(request.getContent())
        .fileIds(request.getFileIds())
        .build();
    Announcement saved = announcementRepository.save(announcement);

    // 전사원에게 알림 발송
    try {
      if (authorization != null && !authorization.isEmpty()) {
        List<Long> userIds = userServiceClient.getAllUserIds(authorization).getData();
        if (userIds != null && !userIds.isEmpty()) {
          NotificationRequest notificationRequest = NotificationRequest.builder()
              .userIds(userIds)
              .type(NotificationType.ANNOUNCEMENT)
              .content("[공지사항] " + saved.getTitle())
              .referenceId(saved.getId())
              .createdAt(LocalDateTime.now())
              .build();

          notificationPublisher.publish(notificationRequest);
          log.info("공지사항 생성 알림 발송 완료 - 공지 ID: {}, 대상자 수: {}", saved.getId(), userIds.size());
        }
      } else {
        log.warn("Authorization 토큰이 없어 알림 발송을 건너뜁니다. - 공지 ID: {}", saved.getId());
      }
    } catch (Exception e) {
      log.error("공지사항 알림 발송 실패 - 공지 ID: {}, 오류: {}", saved.getId(), e.getMessage());
    }

    return AnnouncementResponseDto.builder()
        .id(saved.getId())
        .title(saved.getTitle())
        .displayAuthor(saved.getDisplayAuthor())
        .content(saved.getContent())
        .createdAt(saved.getCreatedAt())
        .views(saved.getViews())
        .fileIds(new ArrayList<>(saved.getFileIds()))
        .build();
  }

  // 단건 조회
  @Transactional
  public AnnouncementResponseDto getAnnouncement(Long id) {

    // 1. 공지사항 엔터티 조회 (fileIds 포함)
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
        .views(announcement.getViews() + 1)
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
        .views(announcement.getViews())
        .fileIds(new ArrayList<>(announcement.getFileIds()))
        .build();

  }

  // 삭제
  @Transactional
  public void deleteAnnouncement(Long id) {
    Announcement announcement = announcementRepository.findById(id)
        .orElseThrow(() -> new AnnouncementNotFoundException(id));

    // 공지사항 관련 알림 삭제
    notificationService.deleteNotificationsByReferenceId(id, NotificationType.ANNOUNCEMENT);

    // 공지사항 삭제
    announcementRepository.delete(announcement);

    log.info("공지사항 삭제 완료 - id: {}", id);
  }

  // 공지 제목으로 검색
  @Transactional(readOnly = true)
  public List<AnnouncementSummaryDto> searchAnnouncement(String keyword) {
    return announcementRepository.findByTitleContaining(keyword);
  }


}

