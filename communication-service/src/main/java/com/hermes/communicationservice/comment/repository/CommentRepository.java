package com.hermes.communicationservice.comment.repository;

import com.hermes.communicationservice.comment.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommentRepository extends JpaRepository<Comment, Long> {

}
