package com.hermes.communicationservice.comment.service;

import com.hermes.communicationservice.client.UserServiceClient;
import com.hermes.communicationservice.client.dto.MainProfileResponseDto;
import com.hermes.communicationservice.comment.dto.CommentCreateDto;
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
  public CommentResponseDto createComment(Long announcementId, CommentCreateDto createDto) {
    log.info("댓글 생성 요청 - announcementId={}, authorId={}", announcementId, createDto.getAuthorId());

    Announcement announcement = findAnnouncementById(announcementId);

    Comment comment = Comment.builder()
        .announcement(announcement)
        .content(createDto.getContent())
        .authorId(createDto.getAuthorId())
        .build();

    Comment savedComment = commentRepository.save(comment);
    UserBasicInfo userInfo = fetchUserBasicInfo(createDto.getAuthorId());

    return commentMapper.toCommentResponseDtoWithUser(savedComment, userInfo);
  }

  // 댓글 삭제
  @Transactional
  public void deleteComment(Long commentId) {
    log.info("댓글 삭제 요청 - commentId={}", commentId);

    Comment comment = commentRepository.findById(commentId)
        .orElseThrow(() -> new IllegalArgumentException("댓글을 찾을 수 없습니다: " + commentId));

    commentRepository.delete(comment);
  }

  // 공지사항 ID로 댓글 목록 조회
  public List<CommentResponseDto> getCommentsByAnnouncementId(Long announcementId) {
    log.info("공지사항 댓글 목록 조회 요청 - announcementId={}", announcementId);

    return commentRepository.findByAnnouncement_IdOrderById(announcementId)
        .stream()
        .map(
            comment
                -> commentMapper.toCommentResponseDtoWithUser
                (comment, fetchUserBasicInfo(comment.getAuthorId())))
        .collect(Collectors.toList());
  }

  private Announcement findAnnouncementById(Long announcementId) {
    return announcementRepository.findById(announcementId)
        .orElseThrow(() -> new IllegalArgumentException("공지사항을 찾을 수 없습니다: " + announcementId));
  }

  private UserBasicInfo fetchUserBasicInfo(Long userId) {
    try {
      ApiResult<MainProfileResponseDto> response = userServiceClient.getMainProfile(userId);
      if (response != null && response.getData() != null && response.getData().getId() != null) {
        return commentMapper.toUserBasicInfo(response.getData());
      }
    } catch (Exception e) {
      log.warn("사용자 정보 조회 실패 - userId={}, reason={}", userId, e.getMessage());
    }

    return UserBasicInfo.builder()
        .id(userId)
        .name("알 수 없음")
        .profileUrl("")
        .build();
  }
}
