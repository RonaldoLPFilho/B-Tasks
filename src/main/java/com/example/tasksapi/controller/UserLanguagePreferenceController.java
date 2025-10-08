package com.example.tasksapi.controller;

import com.example.tasksapi.domain.LanguageOption;
import com.example.tasksapi.dto.ApiResponseDTO;
import com.example.tasksapi.service.user.UserLanguagePreferenceService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/language")
public class UserLanguagePreferenceController {
    private final UserLanguagePreferenceService service;

    public UserLanguagePreferenceController(UserLanguagePreferenceService service) {
        this.service = service;
    }

//    @GetMapping
//    public ResponseEntity<ApiResponseDTO<LanguageOption>> getUserLanguagePreference(){
//        String languageOpt = service.getUserLanguagePreference().getLanguage().toString();
//
//
//    }
}
