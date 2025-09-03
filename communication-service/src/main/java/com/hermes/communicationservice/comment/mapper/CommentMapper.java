package com.hermes.communicationservice.comment.mapper;

import com.hermes.communicationservice.client.dto.MainProfileResponseDto;
import com.hermes.communicationservice.comment.dto.CommentResponseDto;
import com.hermes.communicationservice.comment.dto.UserBasicInfo;
import com.hermes.communicationservice.comment.entity.Comment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CommentMapper {
    
    @Mapping(target = "userInfo", ignore = true)
    CommentResponseDto toCommentResponseDto(Comment comment);
    
    UserBasicInfo toUserBasicInfo(MainProfileResponseDto mainProfileResponseDto);
    
    default CommentResponseDto toCommentResponseDtoWithUser(Comment comment, UserBasicInfo userInfo, boolean canDelete) {
        CommentResponseDto dto = toCommentResponseDto(comment);
        dto.setUserInfo(userInfo);
        dto.setCanDelete(canDelete);
        return dto;
    }
}
