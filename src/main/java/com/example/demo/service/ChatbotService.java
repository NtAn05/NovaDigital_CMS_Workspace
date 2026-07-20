package com.example.demo.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

@Service
public class ChatbotService {

    @Value("${gemini.api.key}")
    private String geminiApiKey;

    private static final String GEMINI_BASE_URL =
        "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent";

    private static final String SYSTEM_PROMPT =
        "You are Nova AI, a friendly and knowledgeable assistant for NovaDigital Agency. " +
        "NovaDigital is a creative digital agency that specializes in web design, UI/UX design, " +
        "digital marketing, mobile app development, branding, and cloud solutions. " +
        "You help visitors learn about our services, projects, team, booking consultations, and contact info. " +
        "Be warm, professional, and concise. Always answer helpfully. " +
        "If asked something completely unrelated to digital agency topics, gently redirect to NovaDigital topics.";

    public String ask(String userMessage) {
        try {
            String escapedMessage = escapeJson(userMessage);
            String escapedPrompt  = escapeJson(SYSTEM_PROMPT);

            String jsonPayload =
                "{" +
                "  \"system_instruction\": {" +
                "    \"parts\": [{\"text\": \"" + escapedPrompt + "\"}]" +
                "  }," +
                "  \"contents\": [{" +
                "    \"role\": \"user\"," +
                "    \"parts\": [{\"text\": \"" + escapedMessage + "\"}]" +
                "  }]," +
                "  \"generationConfig\": {" +
                "    \"temperature\": 0.7," +
                "    \"maxOutputTokens\": 512" +
                "  }" +
                "}";

            HttpClient client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(15))
                .build();

            // Try 1: x-goog-api-key header (newer AQ. keys)
            String reply = tryRequest(client, jsonPayload,
                GEMINI_BASE_URL,
                "x-goog-api-key", geminiApiKey);

            if (reply != null) return reply;

            // Try 2: Bearer Authorization header
            reply = tryRequest(client, jsonPayload,
                GEMINI_BASE_URL,
                "Authorization", "Bearer " + geminiApiKey);

            if (reply != null) return reply;

            // Try 3: Classic ?key= query param (AIza. keys)
            reply = tryRequest(client, jsonPayload,
                GEMINI_BASE_URL + "?key=" + geminiApiKey,
                null, null);

            if (reply != null) return reply;

            return "Sorry, I'm having trouble connecting right now. Please try again in a moment.";

        } catch (Exception e) {
            System.err.println("[Chatbot] Exception: " + e.getMessage());
            e.printStackTrace();
            return "An error occurred while processing your request. Please try again.";
        }
    }

    /** Returns the extracted reply text if HTTP 200, null otherwise */
    private String tryRequest(HttpClient client, String body,
                              String url, String headerName, String headerValue) {
        try {
            HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .timeout(Duration.ofSeconds(30))
                .POST(HttpRequest.BodyPublishers.ofString(body));

            if (headerName != null) {
                builder.header(headerName, headerValue);
            }

            HttpResponse<String> response = client.send(builder.build(),
                HttpResponse.BodyHandlers.ofString());

            System.out.println("[Chatbot] " + (headerName != null ? headerName : "?key param")
                + " → status: " + response.statusCode());

            if (response.statusCode() == 200) {
                return extractTextFromGeminiResponse(response.body());
            } else {
                System.err.println("[Chatbot] Error body: " + response.body());
                return null;
            }
        } catch (Exception e) {
            System.err.println("[Chatbot] Request attempt failed: " + e.getMessage());
            return null;
        }
    }

    private String escapeJson(String text) {
        if (text == null) return "";
        return text
            .replace("\\", "\\\\")
            .replace("\"", "\\\"")
            .replace("\n", "\\n")
            .replace("\r", "\\r")
            .replace("\t", "\\t");
    }

    private String extractTextFromGeminiResponse(String json) {
        try {
            String[] variants = {"\"text\": \"", "\"text\":\""};
            int startIdx = -1;
            for (String key : variants) {
                int idx = json.indexOf(key);
                if (idx != -1) {
                    startIdx = idx + key.length();
                    break;
                }
            }
            if (startIdx == -1) {
                System.err.println("[Chatbot] No 'text' field in response: " + json);
                return "I received a response but couldn't parse it. Please try again.";
            }
            StringBuilder result = new StringBuilder();
            int i = startIdx;
            while (i < json.length()) {
                char c = json.charAt(i);
                if (c == '"' && (i == 0 || json.charAt(i - 1) != '\\')) break;
                if (c == '\\' && i + 1 < json.length()) {
                    char next = json.charAt(i + 1);
                    switch (next) {
                        case 'n':  result.append('\n'); i += 2; continue;
                        case 'r':  result.append('\r'); i += 2; continue;
                        case 't':  result.append('\t'); i += 2; continue;
                        case '"':  result.append('"');  i += 2; continue;
                        case '\\': result.append('\\'); i += 2; continue;
                        default:   result.append(c);    i++;    continue;
                    }
                }
                result.append(c);
                i++;
            }
            return result.toString().trim();
        } catch (Exception e) {
            e.printStackTrace();
            return "Sorry, I couldn't process the response properly.";
        }
    }
}
