package com.nextinnomind.agro_speak_backend.ai.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AIResponse<T> {
    private boolean success;
    private String message;
    private T data;
}
