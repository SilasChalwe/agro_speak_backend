package com.nextinnomind.agro_speak_backend.ai.services;

import com.nextinnomind.agro_speak_backend.ai.models.AIResponse;
import com.nextinnomind.agro_speak_backend.ai.models.SpeechSynthesisResult;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
public class TextToSpeechService {

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${ai.flask.base-url:http://localhost:5000}")
    private String flaskApiBase;

    public AIResponse<SpeechSynthesisResult> speak(String text) {
        String url = flaskApiBase + "/api/speak";

        try {
            var payload = Map.of("text", text);
            var headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            var request = new HttpEntity<>(payload, headers);
            @SuppressWarnings("unchecked")
            ResponseEntity<Map<String, Object>> response = (ResponseEntity<Map<String, Object>>)(ResponseEntity<?>) 
                    restTemplate.postForEntity(url, request, Map.class);
            Map<String, Object> data = response.getBody();

            if (data == null || data.containsKey("error")) {
                String errorMsg = data != null ? (String) data.getOrDefault("error", "TTS failed") : "TTS failed";
                return new AIResponse<>(false, errorMsg, null);
            }

            SpeechSynthesisResult result = new SpeechSynthesisResult(
                (String) data.get("text"),
                (String) data.get("audioUrl"),
                (String) data.get("id")
            );

            return new AIResponse<>(true, "Speech synthesis successful", result);
        } catch (Exception e) {
            return new AIResponse<>(false, "TTS failed: " + e.getMessage(), null);
        }
    }
}
