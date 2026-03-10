package com.example.tasksapi.service.task;

import com.example.tasksapi.domain.task.Comment;
import com.example.tasksapi.domain.task.Task;
import com.example.tasksapi.domain.User;
import com.example.tasksapi.dto.CreateCommentRequestDTO;
import com.example.tasksapi.exception.InvalidDataException;
import com.example.tasksapi.exception.NotFoundException;
import com.example.tasksapi.repository.CommentRepository;
import com.example.tasksapi.service.user.AuthenticatedUserService;
import org.springframework.stereotype.Service;

import java.util.UUID;


@Service
public class CommentService {

    private final CommentRepository commentRepository;
    private final TaskService taskService;
    private final AuthenticatedUserService authenticatedUserService;

    public CommentService(CommentRepository commentRepository, TaskService taskService,
                          AuthenticatedUserService authenticatedUserService) {
        this.commentRepository = commentRepository;
        this.taskService = taskService;
        this.authenticatedUserService = authenticatedUserService;
    }


    public Comment createComment(CreateCommentRequestDTO dto) {
        if(isValid(dto)){
            User user = authenticatedUserService.getCurrentUser();
            Task task = taskService.findByIdAndValidateOwnership(dto.taskId(), user.getId());
            Comment comment = new Comment(
                    dto.description(),
                    user.getUsername(),
                    task
            );

            return commentRepository.save(comment);
        }else {
            throw new InvalidDataException("Invalid comment data");
        }
    }

    public void deleteById(UUID id) {
        User user = authenticatedUserService.getCurrentUser();
        Comment comment = commentRepository.findByIdAndTaskUserId(id, user.getId())
                .orElseThrow(() -> new NotFoundException("Comment not found with id " + id));
        commentRepository.delete(comment);
    }

    private boolean isValid(CreateCommentRequestDTO createCommentRequestDTO) {
        if(createCommentRequestDTO.description().isBlank() || createCommentRequestDTO.taskId() == null)
            return false;

        return true;
    }
}
