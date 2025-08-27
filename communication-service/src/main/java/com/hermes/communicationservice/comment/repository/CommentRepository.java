package com.hermes.communicationservice.comment.repository;

import com.hermes.communicationservice.comment.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    
    /**
     * 공지사항 ID로 댓글 목록을 날짜순으로 조회
     */
    List<Comment> findByAnnouncement_IdOrderById(Long announcementId);
}
