package com.example.tasksapi.controller.task;

import com.example.tasksapi.domain.task.Tab;
import com.example.tasksapi.dto.ApiResponseDTO;
import com.example.tasksapi.dto.CreateTabDTO;
import com.example.tasksapi.dto.DeleteTabRequestDTO;
import com.example.tasksapi.dto.UpdateTabDTO;
import com.example.tasksapi.service.task.TabService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping(value = "/api/tabs", produces = MediaType.APPLICATION_JSON_VALUE)
public class TabController {

    private final TabService tabService;

    public TabController(TabService tabService) {
        this.tabService = tabService;
    }

    @GetMapping
    public ResponseEntity<ApiResponseDTO<List<Tab>>> getAllActive(@RequestHeader("Authorization") String authHeader) {
        String token = authHeader.substring(7);
        List<Tab> data = tabService.findAllByToken(token);
        return ResponseEntity.ok(ApiResponseDTO.success(HttpStatus.OK, "Tabs", data));
    }

    @GetMapping("/all")
    public ResponseEntity<ApiResponseDTO<List<Tab>>> getAllIncludingArchived(@RequestHeader("Authorization") String authHeader) {
        String token = authHeader.substring(7);
        List<Tab> data = tabService.findAllIncludingArchived(token);
        return ResponseEntity.ok(ApiResponseDTO.success(HttpStatus.OK, "All tabs", data));
    }

    @GetMapping("/{tabId}")
    public ResponseEntity<ApiResponseDTO<Tab>> getById(@PathVariable UUID tabId) {
        Tab data = tabService.findById(tabId);
        return ResponseEntity.ok(ApiResponseDTO.success(HttpStatus.OK, "Tab found", data));
    }

    @PostMapping
    public ResponseEntity<ApiResponseDTO<Tab>> create(@RequestBody CreateTabDTO dto, @RequestHeader("Authorization") String authHeader) {
        String token = authHeader.substring(7);
        Tab data = tabService.create(dto, token);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponseDTO.success(HttpStatus.CREATED, "Tab created", data));
    }

    @PutMapping("/{tabId}")
    public ResponseEntity<ApiResponseDTO<Tab>> update(@PathVariable UUID tabId, @RequestBody UpdateTabDTO dto,
                                                      @RequestHeader("Authorization") String authHeader) {
        String token = authHeader.substring(7);
        Tab data = tabService.update(tabId, dto, token);
        return ResponseEntity.ok(ApiResponseDTO.success(HttpStatus.OK, "Tab updated", data));
    }

    @PatchMapping("/{tabId}/archive")
    public ResponseEntity<ApiResponseDTO<Void>> archive(@PathVariable UUID tabId, @RequestHeader("Authorization") String authHeader) {
        String token = authHeader.substring(7);
        tabService.archive(tabId, token);
        return ResponseEntity.ok(ApiResponseDTO.success(HttpStatus.OK, "Tab archived", null));
    }

    @PatchMapping("/{tabId}/unarchive")
    public ResponseEntity<ApiResponseDTO<Void>> unarchive(@PathVariable UUID tabId, @RequestHeader("Authorization") String authHeader) {
        String token = authHeader.substring(7);
        tabService.unarchive(tabId, token);
        return ResponseEntity.ok(ApiResponseDTO.success(HttpStatus.OK, "Tab unarchived", null));
    }

    @DeleteMapping("/{tabId}")
    public ResponseEntity<ApiResponseDTO<Void>> delete(@PathVariable UUID tabId,
                                                       @RequestBody(required = false) DeleteTabRequestDTO body,
                                                       @RequestHeader("Authorization") String authHeader) {
        String token = authHeader.substring(7);
        String password = body != null ? body.password() : null;
        tabService.delete(tabId, token, password);
        return ResponseEntity.ok(ApiResponseDTO.success(HttpStatus.OK, "Tab and its tasks removed", null));
    }
}
