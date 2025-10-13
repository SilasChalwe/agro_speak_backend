package com.nextinnomind.agro_speak_backend.controllers;

import com.nextinnomind.agro_speak_backend.dto.TranslationRequest;
import com.nextinnomind.agro_speak_backend.dto.TranslationResponse;
import com.nextinnomind.agro_speak_backend.service.TranslationService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/translate")
@CrossOrigin
public class TranslationController {

    private final TranslationService translationService;

    public TranslationController(TranslationService translationService) {
        this.translationService = translationService;
    }

    @PostMapping
    public TranslationResponse translate(@RequestBody TranslationRequest request) {
        String translated = translationService.translate(
                request.getText(),
                request.getSourceLanguage(),
                request.getTargetLanguage()
        );
        return new TranslationResponse(translated);
    }
}