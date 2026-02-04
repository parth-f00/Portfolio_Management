package org.neueda.rest.project.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
public class AIService {

    @Value("${openai.api.key:}")
    private String apiKey;

    private final RestTemplate restTemplate = new RestTemplate();
    private static final Logger log =
            LoggerFactory.getLogger(AIService.class);

    public String explain(String text) {

        if (apiKey == null || apiKey.isBlank()) {
            // AI disabled → return original text
            return text;
        }

        try {
            String url = "https://api.openai.com/v1/chat/completions";

            Map<String, Object> body = new HashMap<>();
            body.put("model", "gpt-3.5-turbo");
            body.put("temperature", 0.4);

            List<Map<String, String>> messages = new ArrayList<>();
            messages.add(Map.of(
                    "role", "system",
                    "content",
                    "You are a financial assistant. " +
                            "Rewrite the response in a friendly, clear way. " +
                            "Do NOT change numbers. Add a short disclaimer."
            ));
            messages.add(Map.of(
                    "role", "user",
                    "content", text
            ));

            body.put("messages", messages);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(apiKey);

            HttpEntity<Map<String, Object>> request =
                    new HttpEntity<>(body, headers);

            ResponseEntity<Map> response =
                    restTemplate.postForEntity(url, request, Map.class);

            Map choice = (Map) ((List) response.getBody().get("choices")).get(0);
            Map message = (Map) choice.get("message");

            return (String) message.get("content");

        } catch (Exception e) {
            // AI failed → return original response
            return text;
        }
    }
}
