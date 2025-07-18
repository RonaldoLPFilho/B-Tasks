package com.example.tasksapi.service.task;

import com.example.tasksapi.domain.task.Category;
import com.example.tasksapi.domain.User;
import com.example.tasksapi.dto.CategoryDTO;
import com.example.tasksapi.exception.InvalidDataException;
import com.example.tasksapi.exception.NotFoundException;
import com.example.tasksapi.repository.CategoryRepository;
import com.example.tasksapi.service.user.UserService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class CategoryService {
    private final CategoryRepository categoryRepository;
    private final UserService userService;

    public CategoryService(CategoryRepository categoryRepository, UserService userService) {
        this.categoryRepository = categoryRepository;
        this.userService = userService;
    }

    public List<Category> findAllByToken(String token) {
        Optional<User> user = userService.extractEmailFromTokenAndReturnUser(token);

        if(user.isEmpty()){
            throw new NotFoundException("User not found");
        }

        return categoryRepository.findByUserId(user.get().getId());
    }

    public Category findById(UUID id) {
        Optional<Category> category = categoryRepository.findById(id);
        if(category.isEmpty()){
            throw new NotFoundException("Category not found");
        }
        return category.get();
    }

    public void createCategory(CategoryDTO dto, String token) {
        User user = userService.extractEmailFromTokenAndReturnUser(token)
                .orElseThrow(() -> new NotFoundException("User not found"));


        if(isValidCategory(dto, user.getId())){
            Category category = new Category();
            category.setUser(user);
            category.setColor(dto.color());
            category.setName(dto.name());

            categoryRepository.save(category);
        }else{
            throw new InvalidDataException("Invalid category");
        }

    }

    private boolean isValidCategory(CategoryDTO dto, UUID userId) {
        return !dto.name().isBlank()  && !categoryRepository.existsByNameAndUserId(dto.name(), userId);
    }
}
