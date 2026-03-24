package com.example.tasksapi.controller.task;

import com.example.tasksapi.dto.ApiResponseDTO;
import com.example.tasksapi.dto.CreateTabDTO;
import com.example.tasksapi.dto.DeleteTabRequestDTO;
import com.example.tasksapi.dto.TabResponseDTO;
import com.example.tasksapi.dto.TaskResponseMapper;
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
    private final TaskResponseMapper taskResponseMapper;

    public TabController(TabService tabService, TaskResponseMapper taskResponseMapper) {
        this.tabService = tabService;
        this.taskResponseMapper = taskResponseMapper;
    }

    @GetMapping
    public ResponseEntity<ApiResponseDTO<List<TabResponseDTO>>> getAllActive() {
        List<TabResponseDTO> data = taskResponseMapper.toTabResponses(tabService.findAllForCurrentUser(), false);
        return ResponseEntity.ok(ApiResponseDTO.success(HttpStatus.OK, "Tabs", data));
    }

    @GetMapping("/all")
    public ResponseEntity<ApiResponseDTO<List<TabResponseDTO>>> getAllIncludingArchived() {
        List<TabResponseDTO> data = taskResponseMapper.toTabResponses(tabService.findAllIncludingArchivedForCurrentUser(), true);
        return ResponseEntity.ok(ApiResponseDTO.success(HttpStatus.OK, "All tabs", data));
    }

    @GetMapping("/{tabId}")
    public ResponseEntity<ApiResponseDTO<TabResponseDTO>> getById(@PathVariable UUID tabId,
                                                                  @RequestParam(defaultValue = "false") boolean includeArchived) {
        TabResponseDTO data = taskResponseMapper.toTabResponse(tabService.findByIdForCurrentUser(tabId), includeArchived);
        return ResponseEntity.ok(ApiResponseDTO.success(HttpStatus.OK, "Tab found", data));
    }

    @PostMapping
    public ResponseEntity<ApiResponseDTO<TabResponseDTO>> create(@RequestBody CreateTabDTO dto) {
        TabResponseDTO data = taskResponseMapper.toTabResponse(tabService.create(dto), false);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponseDTO.success(HttpStatus.CREATED, "Tab created", data));
    }

    @PutMapping("/{tabId}")
    public ResponseEntity<ApiResponseDTO<TabResponseDTO>> update(@PathVariable UUID tabId, @RequestBody UpdateTabDTO dto) {
        TabResponseDTO data = taskResponseMapper.toTabResponse(tabService.update(tabId, dto), true);
        return ResponseEntity.ok(ApiResponseDTO.success(HttpStatus.OK, "Tab updated", data));
    }

    @PatchMapping("/{tabId}/archive")
    public ResponseEntity<ApiResponseDTO<Void>> archive(@PathVariable UUID tabId) {
        tabService.archive(tabId);
        return ResponseEntity.ok(ApiResponseDTO.success(HttpStatus.OK, "Tab archived", null));
    }

    @PatchMapping("/{tabId}/unarchive")
    public ResponseEntity<ApiResponseDTO<Void>> unarchive(@PathVariable UUID tabId) {
        tabService.unarchive(tabId);
        return ResponseEntity.ok(ApiResponseDTO.success(HttpStatus.OK, "Tab unarchived", null));
    }

    @DeleteMapping("/{tabId}")
    public ResponseEntity<ApiResponseDTO<Void>> delete(@PathVariable UUID tabId,
                                                       @RequestBody(required = false) DeleteTabRequestDTO body) {
        String password = body != null ? body.password() : null;
        tabService.delete(tabId, password);
        return ResponseEntity.ok(ApiResponseDTO.success(HttpStatus.OK, "Tab and its tasks removed", null));
    }
}
