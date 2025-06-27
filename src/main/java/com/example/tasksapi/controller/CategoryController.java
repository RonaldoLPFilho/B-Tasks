package com.example.tasksapi.controller;

import com.example.tasksapi.domain.Category;

import com.example.tasksapi.dto.ApiResponseDTO;
import com.example.tasksapi.dto.CategoryDTO;
import com.example.tasksapi.service.CategoryService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
    public ResponseEntity<ApiResponseDTO<Void>> createCategory(@RequestBody CategoryDTO dto, @RequestHeader("Authorization") String header) {
        String token = header.substring(7);
        service.createCategory(dto, token);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponseDTO.success(HttpStatus.CREATED, "Category created", null));
    }
}
