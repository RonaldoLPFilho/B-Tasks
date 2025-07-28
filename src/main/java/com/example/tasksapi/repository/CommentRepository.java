package com.example.tasksapi.repository;

import com.example.tasksapi.domain.task.Comment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface CommentRepository extends JpaRepository<Comment, UUID> {
}
