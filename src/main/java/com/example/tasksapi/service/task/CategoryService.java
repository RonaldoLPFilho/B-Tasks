package com.example.tasksapi.service.task;

import com.example.tasksapi.domain.task.Category;
import com.example.tasksapi.domain.task.Task;
import com.example.tasksapi.domain.User;
import com.example.tasksapi.dto.CategoryDTO;
import com.example.tasksapi.dto.DeleteCategoryRequestDTO;
import com.example.tasksapi.exception.InvalidDataException;
import com.example.tasksapi.exception.NotFoundException;
import com.example.tasksapi.repository.CategoryRepository;
import com.example.tasksapi.repository.TaskRepository;
import com.example.tasksapi.service.user.AuthenticatedUserService;
import com.example.tasksapi.service.user.UserService;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class CategoryService {
    public static final String DEFAULT_CATEGORY_NAME = "Padrão";
    public static final String DEFAULT_CATEGORY_COLOR = "#9333EA";

    private final CategoryRepository categoryRepository;
    private final TaskRepository taskRepository;
    private final UserService userService;
    private final AuthenticatedUserService authenticatedUserService;

    public CategoryService(CategoryRepository categoryRepository, TaskRepository taskRepository, UserService userService,
                           AuthenticatedUserService authenticatedUserService) {
        this.categoryRepository = categoryRepository;
        this.taskRepository = taskRepository;
        this.userService = userService;
        this.authenticatedUserService = authenticatedUserService;
    }

    public List<Category> findAllForCurrentUser() {
        User user = authenticatedUserService.getCurrentUser();
        ensureDefaultCategory(user);
        return categoryRepository.findByUserIdOrderByDefaultCategoryDescNameAsc(user.getId());
    }

    public List<Category> findAllByToken(String token) {
        User user = userService.extractEmailFromTokenAndReturnUser(token)
                .orElseThrow(() -> new NotFoundException("User not found"));

        ensureDefaultCategory(user);
        return categoryRepository.findByUserIdOrderByDefaultCategoryDescNameAsc(user.getId());
    }

    public Category findByIdAndValidateOwnership(UUID id, UUID userId) {
        return categoryRepository.findByUserIdAndId(userId, id)
                .orElseThrow(() -> new NotFoundException("Category not found"));
    }

    @Transactional
    public Category createCategory(CategoryDTO dto) {
        return createCategoryForUser(dto, authenticatedUserService.getCurrentUser());
    }

    @Transactional
    public Category createCategory(CategoryDTO dto, String token) {
        User user = userService.extractEmailFromTokenAndReturnUser(token)
                .orElseThrow(() -> new NotFoundException("User not found"));
        return createCategoryForUser(dto, user);
    }

    private Category createCategoryForUser(CategoryDTO dto, User user) {
        validateCategory(dto, user.getId(), null);

        Category category = new Category();
        category.setUser(user);
        category.setColor(dto.color().trim());
        category.setName(dto.name().trim());
        category.setDefaultCategory(false);

        return categoryRepository.save(category);
    }

    @Transactional
    public Category updateCategory(UUID categoryId, CategoryDTO dto) {
        return updateCategoryForUser(categoryId, dto, authenticatedUserService.getCurrentUser());
    }

    @Transactional
    public Category updateCategory(UUID categoryId, CategoryDTO dto, String token) {
        User user = userService.extractEmailFromTokenAndReturnUser(token)
                .orElseThrow(() -> new NotFoundException("User not found"));
        return updateCategoryForUser(categoryId, dto, user);
    }

    private Category updateCategoryForUser(UUID categoryId, CategoryDTO dto, User user) {
        Category category = findByIdAndValidateOwnership(categoryId, user.getId());

        validateCategory(dto, user.getId(), categoryId);

        category.setName(dto.name().trim());
        category.setColor(dto.color().trim());
        return categoryRepository.save(category);
    }

    @Transactional
    public void deleteCategory(UUID categoryId, DeleteCategoryRequestDTO request) {
        deleteCategoryForUser(categoryId, request, authenticatedUserService.getCurrentUser());
    }

    @Transactional
    public void deleteCategory(UUID categoryId, DeleteCategoryRequestDTO request, String token) {
        User user = userService.extractEmailFromTokenAndReturnUser(token)
                .orElseThrow(() -> new NotFoundException("User not found"));
        deleteCategoryForUser(categoryId, request, user);
    }

    private void deleteCategoryForUser(UUID categoryId, DeleteCategoryRequestDTO request, User user) {
        Category categoryToDelete = findByIdAndValidateOwnership(categoryId, user.getId());
        Category fallbackCategory = resolveFallbackCategory(user, categoryToDelete, request);

        List<Task> tasks = taskRepository.findByCategoryId(categoryToDelete.getId());
        for (Task task : tasks) {
            task.setCategory(fallbackCategory);
        }
        taskRepository.saveAll(tasks);

        categoryRepository.delete(categoryToDelete);
    }

    @Transactional
    public Category ensureDefaultCategory(User user) {
        return categoryRepository.findByUserIdAndDefaultCategoryTrue(user.getId())
                .orElseGet(() -> createOrPromoteDefaultCategory(user));
    }

    @Transactional
    public Category createDefaultCategory(User user) {
        return ensureDefaultCategory(user);
    }

    private Category createOrPromoteDefaultCategory(User user) {
        Category existingDefaultByName = categoryRepository
                .findByUserIdAndNameIgnoreCase(user.getId(), DEFAULT_CATEGORY_NAME)
                .orElse(null);

        if (existingDefaultByName != null) {
            existingDefaultByName.setDefaultCategory(true);
            if (existingDefaultByName.getColor() == null || existingDefaultByName.getColor().isBlank()) {
                existingDefaultByName.setColor(DEFAULT_CATEGORY_COLOR);
            }
            return categoryRepository.save(existingDefaultByName);
        }

        Category category = new Category();
        category.setUser(user);
        category.setName(DEFAULT_CATEGORY_NAME);
        category.setColor(DEFAULT_CATEGORY_COLOR);
        category.setDefaultCategory(true);
        return categoryRepository.save(category);
    }

    private Category resolveFallbackCategory(User user, Category categoryToDelete, DeleteCategoryRequestDTO request) {
        if (!categoryToDelete.isDefaultCategory()) {
            Category fallbackCategory = ensureDefaultCategory(user);
            if (fallbackCategory.getId().equals(categoryToDelete.getId())) {
                throw new InvalidDataException("Default category cannot be removed without a replacement category");
            }
            return fallbackCategory;
        }

        UUID replacementCategoryId = request != null ? request.replacementCategoryId() : null;
        if (replacementCategoryId == null) {
            throw new InvalidDataException("Replacement category is required to remove the default category");
        }

        if (replacementCategoryId.equals(categoryToDelete.getId())) {
            throw new InvalidDataException("Replacement category must be different from the category being removed");
        }

        Category replacementCategory = findByIdAndValidateOwnership(replacementCategoryId, user.getId());
        replacementCategory.setDefaultCategory(true);
        categoryRepository.save(replacementCategory);
        return replacementCategory;
    }

    private void validateCategory(CategoryDTO dto, UUID userId, UUID currentCategoryId) {
        if (dto == null) {
            throw new InvalidDataException("Invalid category");
        }

        if (dto.name() == null || dto.name().isBlank()) {
            throw new InvalidDataException("Category name is required");
        }

        if (dto.color() == null || dto.color().isBlank()) {
            throw new InvalidDataException("Category color is required");
        }

        Category existingCategory = categoryRepository.findByUserIdAndNameIgnoreCase(userId, dto.name().trim())
                .orElse(null);

        if (existingCategory != null && !existingCategory.getId().equals(currentCategoryId)) {
            throw new InvalidDataException("Category name already exists");
        }
    }
}
