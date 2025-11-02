package com.nextinnomind.agro_speak_backend.ai.services;

import com.nextinnomind.agro_speak_backend.ai.models.AIResponse;
import com.nextinnomind.agro_speak_backend.ai.models.TranscriptionResult;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.util.LinkedMultiValueMap;

import java.io.IOException;
import java.util.Map;

@Service
public class SpeechToTextService {

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${ai.flask.base-url:http://localhost:5000}")
    private String flaskApiBase;

    public AIResponse<TranscriptionResult> transcribe(MultipartFile audio, String language) {
        String url = flaskApiBase + "/api/transcribe";

        try {
            var body = new LinkedMultiValueMap<String, Object>();
            body.add("language", language);
            body.add("audio", new ByteArrayResource(audio.getBytes()) {
                @Override
                public String getFilename() {
                    return audio.getOriginalFilename();
                }
            });

            var headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            var request = new HttpEntity<>(body, headers);
            @SuppressWarnings({"unchecked", "null"})
            ResponseEntity<Map<String, Object>> response = (ResponseEntity<Map<String, Object>>)(ResponseEntity<?>) 
                    restTemplate.exchange(url, HttpMethod.POST, request, Map.class);
            Map<String, Object> data = response.getBody();

            if (data == null || data.containsKey("error")) {
                String errorMsg = data != null ? (String) data.getOrDefault("error", "Unknown error") : "Unknown error";
                return new AIResponse<>(false, errorMsg, null);
            }

            TranscriptionResult result = new TranscriptionResult(
                    (String) data.get("language"),
                    (String) data.get("transcription")
            );

            return new AIResponse<>(true, "Transcription successful", result);
        } catch (IOException e) {
            return new AIResponse<>(false, "File error: " + e.getMessage(), null);
        } catch (Exception e) {
            return new AIResponse<>(false, "Transcription failed: " + e.getMessage(), null);
        }
    }
}
