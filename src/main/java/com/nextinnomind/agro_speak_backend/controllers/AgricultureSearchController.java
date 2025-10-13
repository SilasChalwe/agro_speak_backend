package com.nextinnomind.agro_speak_backend.controller;

import com.nextinnomind.agro_speak_backend.service.AgricultureSearchService;
import com.nextinnomind.agro_speak_backend.dto.SearchRequest;
import com.nextinnomind.agro_speak_backend.dto.SearchResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/search")
public class AgricultureSearchController {

    private final AgricultureSearchService agricultureSearchService;

    public AgricultureSearchController(AgricultureSearchService agricultureSearchService) {
        this.agricultureSearchService = agricultureSearchService;
    }

    @PostMapping
    public ResponseEntity<SearchResponse> search(@RequestBody SearchRequest request) {
        try {
            String answer = agricultureSearchService.search(request.getQuery(), request.getLanguage());
            SearchResponse response = new SearchResponse();
            response.setAnswer(answer);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            SearchResponse response = new SearchResponse();
            response.setAnswer("Error: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
}