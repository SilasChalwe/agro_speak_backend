package com.nextinnomind.agro_speak_backend.ai.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SpeechSynthesisResult {
    private String text;
    private String audioUrl;
    private String id;
}
