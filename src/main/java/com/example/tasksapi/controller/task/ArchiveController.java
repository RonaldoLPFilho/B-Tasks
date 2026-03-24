package com.example.tasksapi.controller.task;

import com.example.tasksapi.dto.ApiResponseDTO;
import com.example.tasksapi.dto.ArchivedSearchResultDTO;
import com.example.tasksapi.dto.RestoreSectionRequestDTO;
import com.example.tasksapi.dto.RestoreTaskRequestDTO;
import com.example.tasksapi.service.task.ArchiveService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping(value = "/api/archive", produces = MediaType.APPLICATION_JSON_VALUE)
public class ArchiveController {

    private final ArchiveService archiveService;

    public ArchiveController(ArchiveService archiveService) {
        this.archiveService = archiveService;
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponseDTO<List<ArchivedSearchResultDTO>>> search(@RequestParam String q,
                                                                                @RequestParam(required = false) Integer limit) {
        List<ArchivedSearchResultDTO> data = archiveService.searchArchived(q, limit);
        return ResponseEntity.ok(ApiResponseDTO.success(HttpStatus.OK, "Archived search results", data));
    }

    @PostMapping("/tabs/{tabId}/restore")
    public ResponseEntity<ApiResponseDTO<Void>> restoreTab(@PathVariable UUID tabId) {
        archiveService.restoreTab(tabId);
        return ResponseEntity.ok(ApiResponseDTO.success(HttpStatus.OK, "Tab restored", null));
    }

    @PostMapping("/sections/{sectionId}/restore")
    public ResponseEntity<ApiResponseDTO<Void>> restoreSection(@PathVariable UUID sectionId,
                                                               @RequestBody(required = false) RestoreSectionRequestDTO body) {
        archiveService.restoreSection(sectionId, body);
        return ResponseEntity.ok(ApiResponseDTO.success(HttpStatus.OK, "Section restored", null));
    }

    @PostMapping("/tasks/{taskId}/restore")
    public ResponseEntity<ApiResponseDTO<Void>> restoreTask(@PathVariable UUID taskId,
                                                            @RequestBody(required = false) RestoreTaskRequestDTO body) {
        archiveService.restoreTask(taskId, body);
        return ResponseEntity.ok(ApiResponseDTO.success(HttpStatus.OK, "Task restored", null));
    }
}
