package com.nextinnomind.agro_speak_backend.controllers;

import com.nextinnomind.agro_speak_backend.ai.models.*;
import com.nextinnomind.agro_speak_backend.ai.services.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/ai")
@RequiredArgsConstructor
public class AIController {

    private final SpeechToTextService speechToTextService;
    private final TranslationAIService translationService;
    private final TextToSpeechService textToSpeechService;

    @PostMapping("/transcribe")
    public ResponseEntity<AIResponse<TranscriptionResult>> transcribe(
            @RequestParam("audio") MultipartFile audio,
            @RequestParam("language") String language) {

        AIResponse<TranscriptionResult> result = speechToTextService.transcribe(audio, language);
        return ResponseEntity.status(result.isSuccess() ? 200 : 500).body(result);
    }

    @PostMapping("/translate")
    public ResponseEntity<AIResponse<TranslationResult>> translate(@RequestBody TranslationResult req) {
        AIResponse<TranslationResult> result = translationService.translate(
            req.getOriginalText(), req.getSourceLang(), req.getTargetLang()
        );
        return ResponseEntity.status(result.isSuccess() ? 200 : 500).body(result);
    }

    @PostMapping("/speak")
    public ResponseEntity<AIResponse<SpeechSynthesisResult>> speak(@RequestBody SpeechSynthesisResult req) {
        AIResponse<SpeechSynthesisResult> result = textToSpeechService.speak(req.getText());
        return ResponseEntity.status(result.isSuccess() ? 200 : 500).body(result);
    }
}
