package com.example.tasksapi.controller;

import com.example.tasksapi.dto.ApiResponseDTO;
import com.example.tasksapi.dto.StoredFileDTO;
import com.example.tasksapi.dto.StoredFilesPageDTO;
import com.example.tasksapi.service.file.StoredFileService;
import org.springframework.core.io.Resource;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.UUID;

@RestController
@RequestMapping("/api/files")
public class FileController {
    private final StoredFileService storedFileService;

    public FileController(StoredFileService storedFileService) {
        this.storedFileService = storedFileService;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponseDTO<StoredFileDTO>> upload(@RequestParam("file") MultipartFile file) {
        StoredFileDTO data = storedFileService.upload(file);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponseDTO.success(HttpStatus.CREATED, "File uploaded", data));
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponseDTO<StoredFilesPageDTO>> search(@RequestParam(required = false) String name,
                                                                     @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate uploadedFrom,
                                                                     @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate uploadedTo,
                                                                     @RequestParam(required = false) Integer page,
                                                                     @RequestParam(required = false) Integer size) {
        StoredFilesPageDTO data = storedFileService.search(name, uploadedFrom, uploadedTo, page, size);
        return ResponseEntity.ok(ApiResponseDTO.success(HttpStatus.OK, "Files found", data));
    }

    @GetMapping("/{fileId}/download")
    public ResponseEntity<Resource> download(@PathVariable UUID fileId) {
        StoredFileService.FileDownload download = storedFileService.loadForDownload(fileId);
        String contentType = download.metadata().getContentType() == null
                ? MediaType.APPLICATION_OCTET_STREAM_VALUE
                : download.metadata().getContentType();

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .contentLength(download.metadata().getSizeBytes())
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment()
                        .filename(download.metadata().getOriginalFileName(), StandardCharsets.UTF_8)
                        .build()
                        .toString())
                .body(download.resource());
    }

    @DeleteMapping(value = "/{fileId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponseDTO<Void>> delete(@PathVariable UUID fileId) {
        storedFileService.delete(fileId);
        return ResponseEntity.ok(ApiResponseDTO.success(HttpStatus.OK, "File deleted", null));
    }
}
