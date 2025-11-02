package com.nextinnomind.agro_speak_backend.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;

@Service
public class TranslationService {

    private final WebClient webClient;

    @Value("${openrouter.api.key}")
    private String openRouterApiKey;

    @Value("${openrouter.translation-model}")
    private String translationModel;

    @Value("${openrouter.base-url}")
    private String baseUrl;

    // Common words map (can be extended easily)
    private static final Map<String, String> BEMBA_COMMON_WORDS = new HashMap<>();
    static {
        BEMBA_COMMON_WORDS.put("how are you", "mulishani");
        BEMBA_COMMON_WORDS.put("maize", "amataba");
        BEMBA_COMMON_WORDS.put("cultivate", "ukubyala");
        BEMBA_COMMON_WORDS.put("to plant", "ukubyala");
        
        BEMBA_COMMON_WORDS.put("kindly asking", "ndeipushako mukwai");
        // Add more common words here...
    }

    public TranslationService(WebClient.Builder builder) {
        this.webClient = builder.build();
    }

    /**
     * Translates text from sourceLanguage to targetLanguage using OpenRouter Gemini API.
     */
    public String translate(String text, String sourceLanguage, String targetLanguage) {

        String prompt;

        if (targetLanguage != null && targetLanguage.equalsIgnoreCase("bemba")) {
            // Build a list of common words and translations to include in the prompt
            StringJoiner commonWordsHint = new StringJoiner(", ");
            for (Map.Entry<String, String> entry : BEMBA_COMMON_WORDS.entrySet()) {
                commonWordsHint.add(
                        String.format("'%s' = '%s'", entry.getKey(), entry.getValue())
                );
            }

            prompt = String.format(
                    "Translate this message [%s] to %s. " +
                    "Before translating, always check and use these common word translations if present: %s. " +
                    "Return only the translated text in %s, no explanations.",
                    text, targetLanguage, commonWordsHint.toString(), targetLanguage
            );

        } else {
            // Normal translation without common words
            prompt = String.format(
                    "Translate this message [%s] from %s to %s. Return only the translated text.",
                    text, sourceLanguage, targetLanguage
            );
        }

        // Build request body for OpenRouter
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", translationModel);

        Map<String, String> message = new HashMap<>();
        message.put("role", "user");
        message.put("content", prompt);

        requestBody.put("messages", List.of(message));

        try {
            @SuppressWarnings("null")
            OpenRouterResponse response = webClient.post()
                    .uri(baseUrl)
                    .header("Authorization", "Bearer " + openRouterApiKey)
                    .header("Accept", "application/json")
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(OpenRouterResponse.class)
                    .block();

            if (response != null && response.getChoices() != null && response.getChoices().length > 0) {
                String translated = response.getChoices()[0].getMessage().getContent();
                System.out.println("Translated text: " + translated); // Debug log
                return translated;
            }

            return "Translation error: empty response";

        } catch (Exception e) {
            e.printStackTrace(); // Full stack trace for debugging
            return "Translation error: " + e.getMessage();
        }
    }

    // Helper inner classes for JSON parsing
    private static class OpenRouterResponse {
        private Choice[] choices;
        public Choice[] getChoices() { return choices; }
    }

    private static class Choice {
        private Message message;
        public Message getMessage() { return message; }
    }

    private static class Message {
        private String content;
        public String getContent() { return content; }
    }
}