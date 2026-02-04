package org.neueda.rest.project.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/prediction")
@CrossOrigin // Allows your HTML frontend to call this
public class StockPredictionController {

    @PostMapping("/{ticker}")
    public ResponseEntity<?> getPrediction(@PathVariable String ticker) {
        // 1. The URL of your Python Microservice
        String pythonUrl = "http://localhost:5000/predict";

        // 2. Prepare the data to send (JSON: { "ticker": "AAPL" })
        Map<String, String> requestPayload = new HashMap<>();
        requestPayload.put("ticker", ticker);

        RestTemplate restTemplate = new RestTemplate();

        try {
            // 3. Send the request to Python and get the response
            // We expect a Map (JSON) back: { "ticker": "...", "predicted_price": 100.0, "current_price": 98.0 }
            Map<String, Object> response = restTemplate.postForObject(pythonUrl, requestPayload, Map.class);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Error: AI Service is down or unreachable.");
        }
    }
}