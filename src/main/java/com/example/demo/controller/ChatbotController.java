package com.example.demo.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Provides the Gemini config to the frontend so the browser can call
 * Gemini directly. The API key is injected from application.properties
 * and never hard-coded in JS source files.
 */
@RestController
@RequestMapping("/api/chatbot")
public class ChatbotController {

    @Value("${groq.api.key}")
    private String groqApiKey;

    @Value("${groq.model:llama-3.3-70b-versatile}")
    private String groqModel;

    /** Frontend fetches this once to get the key + model */
    @GetMapping("/config")
    public ResponseEntity<?> config() {
        return ResponseEntity.ok(Map.of(
            "apiKey", groqApiKey,
            "model",  groqModel
        ));
    }
}
