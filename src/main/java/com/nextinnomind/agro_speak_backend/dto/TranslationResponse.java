package com.nextinnomind.agro_speak_backend.dto;

public class TranslationResponse {
    private String translatedText;

    public TranslationResponse() {}

    public TranslationResponse(String translatedText) {
        this.translatedText = translatedText;
    }

    public String getTranslatedText() {
        return translatedText;
    }

    public void setTranslatedText(String translatedText) {
        this.translatedText = translatedText;
    }
}