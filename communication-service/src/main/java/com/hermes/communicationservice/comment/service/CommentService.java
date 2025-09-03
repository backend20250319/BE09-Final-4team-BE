package com.hermes.communicationservice.comment.service;

import com.hermes.communicationservice.client.UserServiceClient;
import com.hermes.communicationservice.client.dto.MainProfileResponseDto;
import com.hermes.communicationservice.comment.dto.CommentResponseDto;
import com.hermes.communicationservice.comment.dto.UserBasicInfo;
import com.hermes.communicationservice.comment.entity.Comment;
import com.hermes.communicationservice.comment.mapper.CommentMapper;
import com.hermes.communicationservice.comment.repository.CommentRepository;
import com.hermes.communicationservice.announcement.entity.Announcement;
import com.hermes.communicationservice.announcement.repository.AnnouncementRepository;
import com.hermes.api.common.ApiResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import com.hermes.auth.principal.UserPrincipal;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommentService {

  private final CommentRepository commentRepository;
  private final AnnouncementRepository announcementRepository;
  private final UserServiceClient userServiceClient;
  private final CommentMapper commentMapper;

  // 댓글 생성
  @Transactional
  public CommentResponseDto createComment(Long announcementId, String content, Long authorId) {
    log.info("댓글 생성 요청 - announcementId={}, authorId={}", announcementId, authorId);

    Announcement announcement = findAnnouncementById(announcementId);

    Comment comment = Comment.builder()
        .announcement(announcement)
        .content(content)
        .authorId(authorId)
        .build();

    Comment savedComment = commentRepository.save(comment);
    UserBasicInfo userInfo = fetchUserBasicInfo(authorId);

    return commentMapper.toCommentResponseDtoWithUser(savedComment, userInfo, true); // 생성자는 항상 삭제 가능
  }

  // 댓글 삭제
  @Transactional
  public void deleteComment(Long commentId, UserPrincipal user) {
    log.info("댓글 삭제 요청 - commentId={}", commentId);

    Comment comment = commentRepository.findById(commentId)
        .orElseThrow(() -> new IllegalArgumentException("댓글을 찾을 수 없습니다: " + commentId));
        
    // 본인 또는 ADMIN만 삭제 가능
    if (!user.isAdmin() && !comment.getAuthorId().equals(user.getId())) {
      throw new AccessDeniedException("댓글 삭제 권한이 없습니다");
    }

    commentRepository.delete(comment);
  }

  // 공지사항 ID로 댓글 목록 조회
  public List<CommentResponseDto> getCommentsByAnnouncementId(Long announcementId, UserPrincipal user) {
    log.info("공지사항 댓글 목록 조회 요청 - announcementId={}", announcementId);

    return commentRepository.findByAnnouncement_IdOrderById(announcementId)
        .stream()
        .map(
            comment
                -> {
                  UserBasicInfo userInfo = fetchUserBasicInfo(comment.getAuthorId());
                  boolean canDelete = user.isAdmin() || comment.getAuthorId().equals(user.getId());
                  return commentMapper.toCommentResponseDtoWithUser(comment, userInfo, canDelete);
                })
        .collect(Collectors.toList());
  }

  private Announcement findAnnouncementById(Long announcementId) {
    return announcementRepository.findById(announcementId)
        .orElseThrow(() -> new IllegalArgumentException("공지사항을 찾을 수 없습니다: " + announcementId));
  }

  private UserBasicInfo fetchUserBasicInfo(Long userId) {
    try {
      String token = getCurrentJwtToken();
      if (token == null) {
        log.warn("JWT 토큰을 찾을 수 없어 기본 사용자 정보를 반환합니다. - userId={}", userId);
        return createDefaultUserBasicInfo(userId);
      }
      
      ApiResult<MainProfileResponseDto> response = userServiceClient.getMainProfile(userId, token);
      if (response != null && response.getData() != null && response.getData().getId() != null) {
        return commentMapper.toUserBasicInfo(response.getData());
      }
    } catch (Exception e) {
      log.warn("사용자 정보 조회 실패 - userId={}, reason={}", userId, e.getMessage());
    }

    return createDefaultUserBasicInfo(userId);
  }
  
  private UserBasicInfo createDefaultUserBasicInfo(Long userId) {
    return UserBasicInfo.builder()
        .id(userId)
        .name("알 수 없음")
        .profileImageUrl("")
        .build();
  }
  
  /**
   * 현재 JWT 토큰을 "Bearer " 형태로 반환합니다.
   */
  private String getCurrentJwtToken() {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    if (auth instanceof JwtAuthenticationToken jwtAuth) {
      return "Bearer " + jwtAuth.getToken().getTokenValue();
    }
    return null;
  }
}
