package com.example.tasksapi.controller.task;

import com.example.tasksapi.dto.ApiResponseDTO;
import com.example.tasksapi.dto.CategoryDTO;
import com.example.tasksapi.dto.CategoryResponseDTO;
import com.example.tasksapi.dto.DeleteCategoryRequestDTO;
import com.example.tasksapi.dto.TaskResponseMapper;
import com.example.tasksapi.service.task.CategoryService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/categories")
public class CategoryController {
    private final CategoryService service;
    private final TaskResponseMapper taskResponseMapper;

    public CategoryController(CategoryService categoryService, TaskResponseMapper taskResponseMapper) {
        this.service = categoryService;
        this.taskResponseMapper = taskResponseMapper;
    }

    @GetMapping()
    public ResponseEntity<ApiResponseDTO<List<CategoryResponseDTO>>> getAllByUser() {
        List<CategoryResponseDTO> data = taskResponseMapper.toCategoryResponses(service.findAllForCurrentUser());
        return ResponseEntity.ok(ApiResponseDTO.success(HttpStatus.OK, null, data));
    }

    @PostMapping
    public ResponseEntity<ApiResponseDTO<CategoryResponseDTO>> createCategory(@RequestBody CategoryDTO dto) {
        CategoryResponseDTO data = taskResponseMapper.toCategoryResponse(service.createCategory(dto));
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponseDTO.success(HttpStatus.CREATED, "Category created", data));
    }

    @PutMapping("/{categoryId}")
    public ResponseEntity<ApiResponseDTO<CategoryResponseDTO>> updateCategory(
            @PathVariable UUID categoryId,
            @RequestBody CategoryDTO dto) {
        CategoryResponseDTO data = taskResponseMapper.toCategoryResponse(service.updateCategory(categoryId, dto));
        return ResponseEntity.ok(ApiResponseDTO.success(HttpStatus.OK, "Category updated", data));
    }

    @DeleteMapping("/{categoryId}")
    public ResponseEntity<ApiResponseDTO<Void>> deleteCategory(
            @PathVariable UUID categoryId,
            @RequestBody(required = false) DeleteCategoryRequestDTO dto) {
        service.deleteCategory(categoryId, dto);

        return ResponseEntity.ok(ApiResponseDTO.success(HttpStatus.OK, "Category deleted", null));
    }
}
