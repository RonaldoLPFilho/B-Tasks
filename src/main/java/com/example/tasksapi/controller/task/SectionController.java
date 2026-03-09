package com.example.tasksapi.controller.task;

import com.example.tasksapi.domain.task.Section;
import com.example.tasksapi.dto.ApiResponseDTO;
import com.example.tasksapi.dto.CreateSectionDTO;
import com.example.tasksapi.dto.ReorderSectionsRequest;
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

    public SectionController(SectionService sectionService) {
        this.sectionService = sectionService;
    }

    @GetMapping
    public ResponseEntity<ApiResponseDTO<List<Section>>> list(@PathVariable UUID tabId,
                                                              @RequestHeader("Authorization") String authHeader) {
        String token = authHeader.substring(7);
        List<Section> data = sectionService.findByTabId(tabId, token);
        return ResponseEntity.ok(ApiResponseDTO.success(HttpStatus.OK, "Sections", data));
    }

    @PostMapping
    public ResponseEntity<ApiResponseDTO<Section>> create(@PathVariable UUID tabId,
                                                           @RequestBody CreateSectionDTO dto,
                                                           @RequestHeader("Authorization") String authHeader) {
        String token = authHeader.substring(7);
        Section data = sectionService.create(tabId, dto, token);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponseDTO.success(HttpStatus.CREATED, "Section created", data));
    }

    @PutMapping("/{sectionId}")
    public ResponseEntity<ApiResponseDTO<Section>> update(@PathVariable UUID tabId,
                                                          @PathVariable UUID sectionId,
                                                          @RequestBody UpdateSectionDTO dto,
                                                          @RequestHeader("Authorization") String authHeader) {
        String token = authHeader.substring(7);
        Section data = sectionService.update(tabId, sectionId, dto, token);
        return ResponseEntity.ok(ApiResponseDTO.success(HttpStatus.OK, "Section updated", data));
    }

    @DeleteMapping("/{sectionId}")
    public ResponseEntity<ApiResponseDTO<Void>> delete(@PathVariable UUID tabId,
                                                       @PathVariable UUID sectionId,
                                                       @RequestHeader("Authorization") String authHeader) {
        String token = authHeader.substring(7);
        sectionService.delete(tabId, sectionId, token);
        return ResponseEntity.ok(ApiResponseDTO.success(HttpStatus.OK, "Section deleted", null));
    }

    @PatchMapping("/reorder")
    public ResponseEntity<Void> reorder(@PathVariable UUID tabId,
                                        @RequestBody ReorderSectionsRequest body,
                                        @RequestHeader("Authorization") String authHeader) {
        String token = authHeader.substring(7);
        sectionService.reorder(tabId, body.orderedIds(), token);
        return ResponseEntity.noContent().build();
    }
}
