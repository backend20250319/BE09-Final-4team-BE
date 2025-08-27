package com.hermes.communicationservice.comment.service;

import com.hermes.communicationservice.comment.dto.CommentCreateDto;
import com.hermes.communicationservice.comment.dto.CommentResponseDto;
import com.hermes.communicationservice.comment.dto.UserBasicInfo;
import com.hermes.communicationservice.comment.entity.Comment;
import com.hermes.communicationservice.comment.mapper.CommentMapper;
import com.hermes.communicationservice.comment.repository.CommentRepository;
import com.hermes.communicationservice.announcement.entity.Announcement;
import com.hermes.communicationservice.announcement.repository.AnnouncementRepository;
import com.hermes.userserviceclient.client.UserServiceClient;
import com.hermes.userserviceclient.dto.UserDetailResponseDto;
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

    /**
     * 댓글 생성
     */
    @Transactional
    public CommentResponseDto createComment(Long announcementId, CommentCreateDto createDto) {
        log.info("댓글 생성 요청: announcementId={}, authorId={}", announcementId, createDto.getAuthorId());
        
        Announcement announcement = announcementRepository.findById(announcementId)
                .orElseThrow(() -> new IllegalArgumentException("공지사항을 찾을 수 없습니다: " + announcementId));

        Comment comment = Comment.builder()
                .announcement(announcement)
                .content(createDto.getContent())
                .authorId(createDto.getAuthorId())
                .build();
        
        Comment savedComment = commentRepository.save(comment);
        
        UserBasicInfo userInfo = getUserBasicInfo(createDto.getAuthorId());
        
        return commentMapper.toCommentResponseDtoWithUser(savedComment, userInfo);
    }

    /**
     * 댓글 삭제
     */
    @Transactional
    public void deleteComment(Long commentId) {
        log.info("댓글 삭제 요청: commentId={}", commentId);
        
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("댓글을 찾을 수 없습니다: " + commentId));
        
        commentRepository.delete(comment);
    }

    /**
     * 공지사항 ID로 댓글 목록 조회
     */
    public List<CommentResponseDto> getCommentsByAnnouncementId(Long announcementId) {
        log.info("공지사항 댓글 목록 조회 요청: announcementId={}", announcementId);
        
        List<Comment> comments = commentRepository.findByAnnouncement_IdOrderById(announcementId);
        
        return comments.stream()
                .map(comment -> {
                    UserBasicInfo userInfo = getUserBasicInfo(comment.getAuthorId());
                    return commentMapper.toCommentResponseDtoWithUser(comment, userInfo);
                })
                .collect(Collectors.toList());
    }

    private UserBasicInfo getUserBasicInfo(Long userId) {
        try {
            var response = userServiceClient.getUserDetail(userId);
            ApiResult<UserDetailResponseDto> body = response.getBody();
            if (body != null && body.getData() != null && body.getData().getUser() != null) {
                UserDetailResponseDto.UserResponseDto user = body.getData().getUser();
                return commentMapper.toUserBasicInfo(user);
            }
        } catch (Exception e) {
            log.warn("사용자 정보 조회 실패: userId={}, reason={}", userId, e.getMessage());
        }
        return UserBasicInfo.builder()
                .id(userId)
                .name("알 수 없음")
                .profileUrl("")
                .build();
    }
}
