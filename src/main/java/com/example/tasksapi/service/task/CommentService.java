package com.example.tasksapi.service.task;

import com.example.tasksapi.domain.Comment;
import com.example.tasksapi.domain.Task;
import com.example.tasksapi.dto.CreateCommentRequestDTO;
import com.example.tasksapi.exception.InvalidDataException;
import com.example.tasksapi.repository.CommentRepository;
import com.example.tasksapi.utils.UserUtils;
import org.springframework.stereotype.Service;

import java.util.UUID;


@Service
public class CommentService {

    private final CommentRepository commentRepository;
    private final TaskService taskService;

    public CommentService(CommentRepository commentRepository, TaskService taskService) {
        this.commentRepository = commentRepository;
        this.taskService = taskService;
    }


    public Comment createComment(CreateCommentRequestDTO dto) {
        if(isValid(dto)){

            Task task = taskService.findById(dto.taskId());
            Comment comment = new Comment(
                    dto.description(),
                    UserUtils.getCurrentUsername(),
                    task
            );

            return commentRepository.save(comment);
        }else {
            throw new InvalidDataException("Invalid comment data");
        }
    }

    public void deleteById(UUID id) {
        commentRepository.deleteById(id);
    }

    private boolean isValid(CreateCommentRequestDTO createCommentRequestDTO) {
        if(createCommentRequestDTO.description().isBlank() || createCommentRequestDTO.taskId() == null)
            return false;


        if(!taskService.existsById(createCommentRequestDTO.taskId()))
            return false;

        return true;
    }

//    private String getCurrentUsername() {
//        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
//        if(authentication != null && authentication.isAuthenticated()){
//            User user = (User) authentication.getPrincipal();
//            return user.getUsername();
//        }
//        return null;
//    }
}
