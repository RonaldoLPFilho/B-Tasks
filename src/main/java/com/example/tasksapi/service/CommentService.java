package com.example.tasksapi.service;

import com.example.tasksapi.domain.Comment;
import com.example.tasksapi.domain.Task;
import com.example.tasksapi.dto.CreateCommentRequestDTO;
import com.example.tasksapi.exception.InvalidDataException;
import com.example.tasksapi.repository.CommentRepository;
import org.springframework.stereotype.Service;

@Service
public class CommentService {

    private final CommentRepository commentRepository;
    private final UserService userService;
    private final TaskService taskService;

    public CommentService(CommentRepository commentRepository, UserService userService, TaskService taskService) {
        this.commentRepository = commentRepository;
        this.userService = userService;
        this.taskService = taskService;
    }


    public Comment createComment(CreateCommentRequestDTO dto) {
        if(isValid(dto)){

            Task task = taskService.findById(dto.taskId());
            Comment comment = new Comment(
                    dto.description(),
                    dto.username(),
                    task
            );

            return commentRepository.save(comment);
        }else {
            throw new InvalidDataException("Invalid comment data");
        }
    }

    private boolean isValid(CreateCommentRequestDTO createCommentRequestDTO) {
        if(createCommentRequestDTO.description().isBlank() || createCommentRequestDTO.taskId() == null || createCommentRequestDTO.username().isBlank())
            return false;


        if(!userService.userExists(createCommentRequestDTO.username()))
            return false;


        if(!taskService.existsById(createCommentRequestDTO.taskId()))
            return false;


        return true;
    }
}
