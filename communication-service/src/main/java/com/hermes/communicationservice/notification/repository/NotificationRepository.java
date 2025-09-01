package com.hermes.communicationservice.notification.repository;

import com.hermes.communicationservice.notification.entity.Notification;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

  List<Notification> findByUserIdOrderByCreatedAtDescIdDesc(Long userId, Pageable pageable);
  
  List<Notification> findByUserIdAndIdLessThanOrderByCreatedAtDescIdDesc(Long userId, Long lastId, Pageable pageable);

  boolean existsByUserIdAndIsRead(Long userId, boolean isRead);

  @Modifying
  @Query("UPDATE Notification n SET n.isRead = true WHERE n.id = :id")
  int markAsRead(@Param("id") Long id);

}