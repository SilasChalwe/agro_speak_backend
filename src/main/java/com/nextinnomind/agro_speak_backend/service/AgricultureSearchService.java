package com.nextinnomind.agro_speak_backend.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
public class AgricultureSearchService {

    private final WebClient webClient;
    private final TranslationService translationService;

    @Value("${openrouter.api.key}")
    private String openRouterApiKey;

    @Value("${openrouter.model}")
    private String model;

    public AgricultureSearchService(WebClient.Builder builder, TranslationService translationService) {
        this.webClient = builder.baseUrl("https://openrouter.ai/api/v1").build();
        this.translationService = translationService;
    }

    public String search(String query, String language) {
        // Translate user's query to English
        String englishQuery = translationService.translate(query, language, "English");

        //  Ask AI for agricultural info
        String prompt = "You are an agriculture expert. Answer the following question in a clear and simple way:\n\n" + englishQuery;

        String englishAnswer;
        try {
            @SuppressWarnings("null")
            OpenRouterResponse response = this.webClient.post()
                    .uri("/chat/completions")
                    .header("Authorization", "Bearer " + openRouterApiKey)
                    .header("Content-Type", "application/json")
                    .bodyValue("""
                            {
                              "model": "%s",
                              "messages": [{"role": "user", "content": "%s"}]
                            }
                            """.formatted(model, prompt))
                    .retrieve()
                    .bodyToMono(OpenRouterResponse.class)
                    .block();

            englishAnswer = response.getChoices()[0].getMessage().getContent();

        } catch (Exception e) {
            return "Search error: " + e.getMessage();
        }

        //  Translate answer back to user's language
        return translationService.translate(englishAnswer, "English", language);
    }

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