package com.nextinnomind.agro_speak_backend.ai.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TranslationResult {
    private String originalText;
    private String sourceLang;
    private String targetLang;
    private String translation;
}
