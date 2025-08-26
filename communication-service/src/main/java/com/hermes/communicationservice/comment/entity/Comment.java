package com.hermes.communicationservice.comment.entity;

import com.hermes.communicationservice.announcement.entity.Announcement;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
// Auditing 사용 (createdAt, updatedAt, createdBy, updatedBy 자동으로 채워준다.)
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Comment {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne
  @JoinColumn(name = "announcement_id", nullable = false)
  private Announcement targetAnnouncement;

  private Long authorId;

  @Lob
  private String content;

  @CreatedDate
  @Column(updatable = false, nullable = false)
  private LocalDateTime createdAt;
}
