package com.nextinnomind.agro_speak_backend.dto;

public class SearchRequest {

    private String query;
    private String language;

    public SearchRequest() {
    }

    public SearchRequest(String query, String language) {
        this.query = query;
        this.language = language;
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }
}