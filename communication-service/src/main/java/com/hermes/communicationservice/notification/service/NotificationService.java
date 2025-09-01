package com.hermes.communicationservice.notification.service;

import com.hermes.communicationservice.notification.dto.CreateNotificationRequestDto;
import com.hermes.communicationservice.notification.dto.NotificationResponseDto;
import com.hermes.communicationservice.notification.entity.Notification;
import com.hermes.communicationservice.notification.exception.NotificationAccessDeniedException;
import com.hermes.communicationservice.notification.exception.NotificationNotFoundException;
import com.hermes.communicationservice.notification.repository.NotificationRepository;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NotificationService {

  private final NotificationRepository notificationRepository;

  @Transactional
  public NotificationResponseDto createNotification(CreateNotificationRequestDto requestDto) {
    Notification notification = requestDto.toEntity();
    Notification savedNotification = notificationRepository.save(notification);
    return NotificationResponseDto.fromEntity(savedNotification);
  }

  @Transactional(readOnly = true)
  public List<NotificationResponseDto> getNotifications(Long userId, Long lastId, int size) {
    Pageable pageable = PageRequest.of(0, size);

    List<Notification> notifications;
    if (lastId == null) {
      notifications = notificationRepository.findByUserIdOrderByCreatedAtDescIdDesc(userId, pageable);
    } else {
      notifications = notificationRepository.findByUserIdAndIdLessThanOrderByCreatedAtDescIdDesc(userId, lastId, pageable);
    }

    return notifications.stream()
            .map(NotificationResponseDto::fromEntity)
            .collect(Collectors.toList());
  }

  @Transactional(readOnly = true)
  public boolean hasUnreadNotifications(Long userId) {
    return notificationRepository.existsByUserIdAndIsRead(userId, false);
  }

  @Transactional
  public boolean markAsRead(Long notificationId, Long userId) {
    Notification notification = notificationRepository.findById(notificationId)
        .orElseThrow(() -> new NotificationNotFoundException("알림을 찾을 수 없습니다: " + notificationId));
    
    if (!notification.getUserId().equals(userId)) {
      throw new NotificationAccessDeniedException("해당 알림에 접근할 수 없습니다");
    }
    
    int updated = notificationRepository.markAsRead(notificationId);
    return updated > 0;
  }

}