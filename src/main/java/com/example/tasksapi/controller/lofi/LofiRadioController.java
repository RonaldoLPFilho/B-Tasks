package com.example.tasksapi.controller.lofi;

import com.example.tasksapi.dto.ApiResponseDTO;
import com.example.tasksapi.dto.LofiRadioDTO;
import com.example.tasksapi.service.lofi.LofiRadioCatalogService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/lofi/radios")
public class LofiRadioController {
    private final LofiRadioCatalogService lofiRadioCatalogService;

    public LofiRadioController(LofiRadioCatalogService lofiRadioCatalogService) {
        this.lofiRadioCatalogService = lofiRadioCatalogService;
    }

    @GetMapping
    public ResponseEntity<ApiResponseDTO<List<LofiRadioDTO>>> listRadios() {
        List<LofiRadioDTO> radios = lofiRadioCatalogService.listRadios();
        return ResponseEntity.ok(ApiResponseDTO.success(HttpStatus.OK, "lofi radios available", radios));
    }
}
