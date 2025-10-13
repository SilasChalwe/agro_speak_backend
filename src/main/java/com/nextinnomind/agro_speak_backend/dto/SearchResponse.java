package com.nextinnomind.agro_speak_backend.dto;

public class SearchResponse {

    private String answer;

    public SearchResponse() {
    }

    public SearchResponse(String answer) {
        this.answer = answer;
    }

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }
}