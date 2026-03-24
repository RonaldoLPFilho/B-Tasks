package com.example.tasksapi.controller.task;

import com.example.tasksapi.dto.ApiResponseDTO;
import com.example.tasksapi.dto.CreateSectionDTO;
import com.example.tasksapi.dto.ReorderSectionsRequest;
import com.example.tasksapi.dto.SectionResponseDTO;
import com.example.tasksapi.dto.TaskResponseMapper;
import com.example.tasksapi.dto.UpdateSectionDTO;
import com.example.tasksapi.service.task.SectionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping(value = "/api/tabs/{tabId}/sections", produces = MediaType.APPLICATION_JSON_VALUE)
public class SectionController {

    private final SectionService sectionService;
    private final TaskResponseMapper taskResponseMapper;

    public SectionController(SectionService sectionService, TaskResponseMapper taskResponseMapper) {
        this.sectionService = sectionService;
        this.taskResponseMapper = taskResponseMapper;
    }

    @GetMapping
    public ResponseEntity<ApiResponseDTO<List<SectionResponseDTO>>> list(@PathVariable UUID tabId,
                                                                         @RequestParam(defaultValue = "false") boolean includeArchived) {
        List<SectionResponseDTO> data = includeArchived
                ? taskResponseMapper.toSectionResponses(sectionService.findByTabIdIncludingArchivedForCurrentUser(tabId), true)
                : taskResponseMapper.toSectionResponses(sectionService.findByTabIdForCurrentUser(tabId));
        return ResponseEntity.ok(ApiResponseDTO.success(HttpStatus.OK, "Sections", data));
    }

    @PostMapping
    public ResponseEntity<ApiResponseDTO<SectionResponseDTO>> create(@PathVariable UUID tabId,
                                                           @RequestBody CreateSectionDTO dto) {
        SectionResponseDTO data = taskResponseMapper.toSectionResponse(sectionService.create(tabId, dto));
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponseDTO.success(HttpStatus.CREATED, "Section created", data));
    }

    @PutMapping("/{sectionId}")
    public ResponseEntity<ApiResponseDTO<SectionResponseDTO>> update(@PathVariable UUID tabId,
                                                          @PathVariable UUID sectionId,
                                                          @RequestBody UpdateSectionDTO dto) {
        SectionResponseDTO data = taskResponseMapper.toSectionResponse(sectionService.update(tabId, sectionId, dto));
        return ResponseEntity.ok(ApiResponseDTO.success(HttpStatus.OK, "Section updated", data));
    }

    @DeleteMapping("/{sectionId}")
    public ResponseEntity<ApiResponseDTO<Void>> delete(@PathVariable UUID tabId, @PathVariable UUID sectionId) {
        sectionService.delete(tabId, sectionId);
        return ResponseEntity.ok(ApiResponseDTO.success(HttpStatus.OK, "Section deleted", null));
    }

    @PatchMapping("/{sectionId}/archive")
    public ResponseEntity<ApiResponseDTO<Void>> archive(@PathVariable UUID tabId, @PathVariable UUID sectionId) {
        sectionService.archive(tabId, sectionId);
        return ResponseEntity.ok(ApiResponseDTO.success(HttpStatus.OK, "Section archived", null));
    }

    @PatchMapping("/{sectionId}/unarchive")
    public ResponseEntity<ApiResponseDTO<Void>> unarchive(@PathVariable UUID tabId, @PathVariable UUID sectionId) {
        sectionService.unarchive(tabId, sectionId);
        return ResponseEntity.ok(ApiResponseDTO.success(HttpStatus.OK, "Section unarchived", null));
    }

    @PatchMapping("/reorder")
    public ResponseEntity<Void> reorder(@PathVariable UUID tabId, @RequestBody ReorderSectionsRequest body) {
        sectionService.reorder(tabId, body.orderedIds());
        return ResponseEntity.noContent().build();
    }
}
