package com.example.tasksapi.controller;

import com.example.tasksapi.domain.Comment;
import com.example.tasksapi.dto.ApiResponseDTO;
import com.example.tasksapi.dto.CreateCommentRequestDTO;
import com.example.tasksapi.service.task.CommentService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/comments")
public class CommentController {
    private final CommentService commentService;

    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    @PostMapping
    public ResponseEntity<ApiResponseDTO<Comment>> createComment(@RequestBody CreateCommentRequestDTO comment) {
        Comment data = commentService.createComment(comment);
        return ResponseEntity.ok(ApiResponseDTO.success(HttpStatus.CREATED, "Comment created", data));
    }

    @DeleteMapping("/{commentId}")
    public ResponseEntity<ApiResponseDTO<Void>> deleteComment(@PathVariable UUID commentId) {
        commentService.deleteById(commentId);
        return ResponseEntity.ok(ApiResponseDTO.success(HttpStatus.OK, "Comment deleted", null));
    }
}
