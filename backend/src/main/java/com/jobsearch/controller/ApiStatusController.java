package com.jobsearch.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api-status")
public class ApiStatusController {

    private static final Logger logger = LoggerFactory.getLogger(ApiStatusController.class);
    
    @Value("${app.external-apis.rapidapi.key}")
    private String rapidApiKey;

    @Value("${app.external-apis.rapidapi.host}")
    private String rapidApiHost;

    private final WebClient webClient;

    public ApiStatusController(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.baseUrl("https://jsearch.p.rapidapi.com").build();
    }

    @GetMapping("/jsearch-quota")
    public ResponseEntity<Map<String, Object>> checkJSearchQuota() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Make a minimal test request to check quota
            Mono<String> testResponse = webClient.get()
                    .uri("/search?query=test&page=1&num_pages=1")
                    .header("x-rapidapi-key", rapidApiKey)
                    .header("x-rapidapi-host", rapidApiHost)
                    .retrieve()
                    .onStatus(
                        status -> status.value() == 429,
                        clientResponse -> {
                            logger.warn("Rate limit hit (429) for JSearch API quota check");
                            return Mono.error(new RuntimeException("Rate limit exceeded"));
                        }
                    )
                    .bodyToMono(String.class);

            String result = testResponse.block();
            response.put("status", "OK");
            response.put("message", "JSearch API is accessible");
            response.put("hasQuota", true);
            
        } catch (Exception e) {
            if (e.getMessage().contains("Rate limit exceeded") || e.getMessage().contains("429")) {
                response.put("status", "RATE_LIMITED");
                response.put("message", "Rate limit exceeded. Please wait before making more requests.");
                response.put("hasQuota", false);
                response.put("recommendation", "Wait 1 hour or upgrade your RapidAPI subscription");
            } else {
                response.put("status", "ERROR");
                response.put("message", "Error checking API status: " + e.getMessage());
                response.put("hasQuota", false);
            }
        }
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/recommendations")
    public ResponseEntity<Map<String, Object>> getRecommendations() {
        Map<String, Object> recommendations = new HashMap<>();
        
        recommendations.put("rateLimitSolutions", java.util.Arrays.asList(
            "Upgrade your RapidAPI subscription for higher limits",
            "Reduce the number of results per fetch (currently limited to 10)",
            "Add longer delays between requests (currently 2 seconds)",
            "Use the application during off-peak hours",
            "Cache results to reduce API calls"
        ));
        
        recommendations.put("currentSettings", java.util.Map.of(
            "maxResultsPerSource", 10,
            "delayBetweenRequests", "2 seconds",
            "delayBetweenAPIs", "3 seconds",
            "retryDelay", "60 seconds on 429 error"
        ));
        
        recommendations.put("rapidAPILimits", java.util.Map.of(
            "freeLimit", "500 requests/month",
            "basicLimit", "10,000 requests/month",
            "proLimit", "100,000 requests/month"
        ));
        
        return ResponseEntity.ok(recommendations);
    }
}