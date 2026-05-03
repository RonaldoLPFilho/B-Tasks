package com.example.tasksapi.controller.howtodo;

import com.example.tasksapi.dto.ApiResponseDTO;
import com.example.tasksapi.dto.CreateHowToDoRequestDTO;
import com.example.tasksapi.dto.HowToDoDetailDTO;
import com.example.tasksapi.dto.HowToDoPageDTO;
import com.example.tasksapi.dto.UpdateHowToDoRequestDTO;
import com.example.tasksapi.service.howtodo.HowToDoService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/how-to-do")
public class HowToDoController {
    private final HowToDoService service;

    public HowToDoController(HowToDoService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<ApiResponseDTO<HowToDoPageDTO>> list(@RequestParam(required = false) String title,
                                                               @RequestParam(required = false) Integer page,
                                                               @RequestParam(required = false) Integer size) {
        HowToDoPageDTO data = service.list(title, page, size);
        return ResponseEntity.ok(ApiResponseDTO.success(HttpStatus.OK, "How To Do documents found", data));
    }

    @PostMapping
    public ResponseEntity<ApiResponseDTO<HowToDoDetailDTO>> create(@RequestBody CreateHowToDoRequestDTO request) {
        HowToDoDetailDTO data = service.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponseDTO.success(HttpStatus.CREATED, "How To Do created", data));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponseDTO<HowToDoDetailDTO>> get(@PathVariable UUID id) {
        HowToDoDetailDTO data = service.get(id);
        return ResponseEntity.ok(ApiResponseDTO.success(HttpStatus.OK, "How To Do found", data));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponseDTO<HowToDoDetailDTO>> update(@PathVariable UUID id,
                                                                   @RequestBody UpdateHowToDoRequestDTO request) {
        HowToDoDetailDTO data = service.update(id, request);
        return ResponseEntity.ok(ApiResponseDTO.success(HttpStatus.OK, "How To Do updated", data));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponseDTO<Void>> delete(@PathVariable UUID id) {
        service.delete(id);
        return ResponseEntity.ok(ApiResponseDTO.success(HttpStatus.OK, "How To Do deleted", null));
    }
}
