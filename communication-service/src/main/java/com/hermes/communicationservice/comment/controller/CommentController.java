package com.hermes.communicationservice.comment.controller;

import com.hermes.communicationservice.comment.dto.CommentCreateDto;
import com.hermes.communicationservice.comment.dto.CommentResponseDto;
import com.hermes.communicationservice.comment.service.CommentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    // 댓글 생성
    @PostMapping("/announcements/{announcementId}/comments")
    public ResponseEntity<CommentResponseDto> createComment(
            @PathVariable Long announcementId,
            @Valid @RequestBody CommentCreateDto createDto) {
        log.info("댓글 생성 요청: announcementId={}, createDto={}", announcementId, createDto);

        CommentResponseDto response = commentService.createComment(announcementId, createDto);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // 공지사항별 댓글 목록 조회
    @GetMapping("/announcements/{announcementId}/comments")
    public ResponseEntity<List<CommentResponseDto>> getCommentsByAnnouncementId(@PathVariable Long announcementId) {
        log.info("공지사항 댓글 목록 조회 요청: announcementId={}", announcementId);

        List<CommentResponseDto> comments = commentService.getCommentsByAnnouncementId(announcementId);

        return ResponseEntity.ok(comments);
    }

    // 댓글 삭제
    @DeleteMapping("/comments/{commentId}")
    public ResponseEntity<Void> deleteComment(@PathVariable Long commentId) {
        log.info("댓글 삭제 요청: commentId={}", commentId);
        
        commentService.deleteComment(commentId);
        
        return ResponseEntity.noContent().build();
    }
}
