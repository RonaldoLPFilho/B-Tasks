package com.example.tasksapi.controller.task;

import com.example.tasksapi.domain.task.Category;

import com.example.tasksapi.dto.ApiResponseDTO;
import com.example.tasksapi.dto.CategoryDTO;
import com.example.tasksapi.dto.DeleteCategoryRequestDTO;
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

    public CategoryController(CategoryService categoryService) {
        this.service = categoryService;
    }

    @GetMapping()
    public ResponseEntity<ApiResponseDTO<List<Category>>> getAllByUser(@RequestHeader("Authorization") String header) {
        String token = header.substring(7);
        List<Category> data = service.findAllByToken(token);

        return ResponseEntity.ok(ApiResponseDTO.success(HttpStatus.OK, null, data));
    }

    @PostMapping
    public ResponseEntity<ApiResponseDTO<Category>> createCategory(@RequestBody CategoryDTO dto, @RequestHeader("Authorization") String header) {
        String token = header.substring(7);
        Category data = service.createCategory(dto, token);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponseDTO.success(HttpStatus.CREATED, "Category created", data));
    }

    @PutMapping("/{categoryId}")
    public ResponseEntity<ApiResponseDTO<Category>> updateCategory(
            @PathVariable UUID categoryId,
            @RequestBody CategoryDTO dto,
            @RequestHeader("Authorization") String header) {
        String token = header.substring(7);
        Category data = service.updateCategory(categoryId, dto, token);

        return ResponseEntity.ok(ApiResponseDTO.success(HttpStatus.OK, "Category updated", data));
    }

    @DeleteMapping("/{categoryId}")
    public ResponseEntity<ApiResponseDTO<Void>> deleteCategory(
            @PathVariable UUID categoryId,
            @RequestBody(required = false) DeleteCategoryRequestDTO dto,
            @RequestHeader("Authorization") String header) {
        String token = header.substring(7);
        service.deleteCategory(categoryId, dto, token);

        return ResponseEntity.ok(ApiResponseDTO.success(HttpStatus.OK, "Category deleted", null));
    }
}
