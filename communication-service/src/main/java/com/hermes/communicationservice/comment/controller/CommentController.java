package com.hermes.communicationservice.comment.controller;

import com.hermes.auth.principal.UserPrincipal;
import com.hermes.communicationservice.comment.dto.CommentCreateDto;
import com.hermes.communicationservice.comment.dto.CommentResponseDto;
import com.hermes.communicationservice.comment.service.CommentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import jakarta.validation.Valid;
import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
@Tag(name = "댓글 관리", description = "공지사항 댓글 생성, 조회, 삭제 API")
public class CommentController {

    private final CommentService commentService;

    @Operation(summary = "댓글 생성", description = "특정 공지사항에 새로운 댓글을 생성합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "댓글 생성 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터"),
        @ApiResponse(responseCode = "401", description = "인증 실패"),
        @ApiResponse(responseCode = "404", description = "공지사항을 찾을 수 없음"),
        @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    @PostMapping("/announcements/{announcementId}/comments")
    public ResponseEntity<CommentResponseDto> createComment(
            @Parameter(description = "공지사항 ID", required = true, example = "1") @PathVariable Long announcementId,
            @Parameter(description = "댓글 생성 요청 정보", required = true) @Valid @RequestBody CommentCreateDto request,
            @AuthenticationPrincipal UserPrincipal user,
            @RequestHeader("Authorization") String authorization) {
        log.info("댓글 생성 요청: announcementId={}, content={}, authorId={}", announcementId, request.getContent(), user.getId());

        CommentResponseDto response = commentService.createComment(announcementId, request.getContent(), user.getId(), authorization);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "공지사항별 댓글 목록 조회", description = "특정 공지사항의 모든 댓글을 조회합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "댓글 목록 조회 성공"),
        @ApiResponse(responseCode = "401", description = "인증 실패"),
        @ApiResponse(responseCode = "404", description = "공지사항을 찾을 수 없음"),
        @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    @GetMapping("/announcements/{announcementId}/comments")
    public ResponseEntity<List<CommentResponseDto>> getCommentsByAnnouncementId(
            @Parameter(description = "공지사항 ID", required = true, example = "1") @PathVariable Long announcementId,
            @AuthenticationPrincipal UserPrincipal user,
            @RequestHeader("Authorization") String authorization) {
        log.info("공지사항 댓글 목록 조회 요청: announcementId={}", announcementId);

        List<CommentResponseDto> comments = commentService.getCommentsByAnnouncementId(announcementId, user, authorization);

        return ResponseEntity.ok(comments);
    }

    @Operation(summary = "댓글 삭제", description = "특정 댓글을 삭제합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "댓글 삭제 성공"),
        @ApiResponse(responseCode = "401", description = "인증 실패"),
        @ApiResponse(responseCode = "403", description = "삭제 권한 없음"),
        @ApiResponse(responseCode = "404", description = "댓글을 찾을 수 없음"),
        @ApiResponse(responseCode = "500", description = "서버 내부 오류")
    })
    @DeleteMapping("/comments/{commentId}")
    public ResponseEntity<Void> deleteComment(
            @Parameter(description = "댓글 ID", required = true, example = "1") @PathVariable Long commentId,
            @AuthenticationPrincipal UserPrincipal user) {
        log.info("댓글 삭제 요청: commentId={}", commentId);
        
        commentService.deleteComment(commentId, user);
        
        return ResponseEntity.noContent().build();
    }
}
