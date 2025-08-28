package com.hermes.communicationservice.comment.mapper;

import com.hermes.communicationservice.comment.dto.CommentResponseDto;
import com.hermes.communicationservice.comment.dto.UserBasicInfo;
import com.hermes.communicationservice.comment.entity.Comment;
import com.hermes.userserviceclient.dto.UserDetailResponseDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CommentMapper {
    
    @Mapping(target = "userInfo", ignore = true)
    CommentResponseDto toCommentResponseDto(Comment comment);
    
    UserBasicInfo toUserBasicInfo(UserDetailResponseDto.UserResponseDto userResponseDto);
    
    default CommentResponseDto toCommentResponseDtoWithUser(Comment comment, UserBasicInfo userInfo) {
        CommentResponseDto dto = toCommentResponseDto(comment);
        dto.setUserInfo(userInfo);
        return dto;
    }
}
