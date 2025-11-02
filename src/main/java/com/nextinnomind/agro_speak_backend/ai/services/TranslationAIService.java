package com.nextinnomind.agro_speak_backend.ai.services;

import com.nextinnomind.agro_speak_backend.ai.models.AIResponse;
import com.nextinnomind.agro_speak_backend.ai.models.TranslationResult;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
public class TranslationAIService {

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${ai.flask.base-url:http://localhost:5000}")
    private String flaskApiBase;

    public AIResponse<TranslationResult> translate(String text, String sourceLang, String targetLang) {
        String url = flaskApiBase + "/api/translate";

        try {
            var payload = Map.of(
                "text", text,
                "sourceLang", sourceLang,
                "targetLang", targetLang
            );

            var headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            var request = new HttpEntity<>(payload, headers);
            ResponseEntity<Map> response = restTemplate.postForEntity(url, request, Map.class);
            Map<String, Object> data = response.getBody();

            if (data == null || data.containsKey("error")) {
                return new AIResponse<>(false, (String) data.getOrDefault("error", "Translation failed"), null);
            }

            TranslationResult result = new TranslationResult(
                (String) data.get("originalText"),
                (String) data.get("sourceLang"),
                (String) data.get("targetLang"),
                (String) data.get("translation")
            );

            return new AIResponse<>(true, "Translation successful", result);
        } catch (Exception e) {
            return new AIResponse<>(false, "Translation failed: " + e.getMessage(), null);
        }
    }
}
