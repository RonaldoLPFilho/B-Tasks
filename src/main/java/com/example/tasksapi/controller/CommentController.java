package com.example.tasksapi.controller;

import com.example.tasksapi.dto.ApiResponseDTO;
import com.example.tasksapi.dto.CommentResponseDTO;
import com.example.tasksapi.dto.CreateCommentRequestDTO;
import com.example.tasksapi.dto.TaskResponseMapper;
import com.example.tasksapi.service.task.CommentService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/comments")
public class CommentController {
    private final CommentService commentService;
    private final TaskResponseMapper taskResponseMapper;

    public CommentController(CommentService commentService, TaskResponseMapper taskResponseMapper) {
        this.commentService = commentService;
        this.taskResponseMapper = taskResponseMapper;
    }

    @PostMapping
    public ResponseEntity<ApiResponseDTO<CommentResponseDTO>> createComment(@RequestBody CreateCommentRequestDTO comment) {
        CommentResponseDTO data = taskResponseMapper.toCommentResponse(commentService.createComment(comment));
        return ResponseEntity.ok(ApiResponseDTO.success(HttpStatus.CREATED, "Comment created", data));
    }

    @DeleteMapping("/{commentId}")
    public ResponseEntity<ApiResponseDTO<Void>> deleteComment(@PathVariable UUID commentId) {
        commentService.deleteById(commentId);
        return ResponseEntity.ok(ApiResponseDTO.success(HttpStatus.OK, "Comment deleted", null));
    }
}
