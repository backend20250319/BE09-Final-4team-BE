package com.hermes.communicationservice.announcement.entity;

import com.hermes.communicationservice.comment.entity.Comment;
import jakarta.persistence.CascadeType;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Announcement {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  String title;

  private Long authorId; // 공지 발행자

  private String displayAuthor; // 화면에 공지 작성자로 표시될 이름

  private String content;

  @CreatedDate
  private LocalDateTime createdAt; // 공지사항 발행 시간

  private int views; // 조회수

  @OneToMany(mappedBy = "announcement", cascade = CascadeType.ALL, orphanRemoval = true)
  @Builder.Default
  private List<Comment> comments = new ArrayList<>();

  // 첨부파일 fileId 리스트
  @ElementCollection
  @Builder.Default
  List<String> fileIds = new ArrayList<>();

}
