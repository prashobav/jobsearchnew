package com.jobsearch.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/mock")
public class MockController {

    @Value("${app.mock.enabled:false}")
    private boolean mockEnabled;

    private final ConfigurableEnvironment environment;

    public MockController(ConfigurableEnvironment environment) {
        this.environment = environment;
    }

    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getMockStatus() {
        Map<String, Object> status = new HashMap<>();
        status.put("mockEnabled", mockEnabled);
        status.put("serviceType", mockEnabled ? "MOCK" : "REAL");
        status.put("description", mockEnabled ? 
            "Using mock services for testing - no external API calls" : 
            "Using real external API services");
        
        return ResponseEntity.ok(status);
    }

    @PostMapping("/enable")
    public ResponseEntity<Map<String, Object>> enableMock() {
        // Add property to environment
        Map<String, Object> mockProps = new HashMap<>();
        mockProps.put("app.mock.enabled", "true");
        environment.getPropertySources().addFirst(
            new MapPropertySource("mockOverride", mockProps)
        );
        
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Mock services enabled");
        response.put("mockEnabled", true);
        response.put("note", "Application restart required for full effect");
        
        return ResponseEntity.ok(response);
    }

    @PostMapping("/disable")
    public ResponseEntity<Map<String, Object>> disableMock() {
        // Add property to environment
        Map<String, Object> mockProps = new HashMap<>();
        mockProps.put("app.mock.enabled", "false");
        environment.getPropertySources().addFirst(
            new MapPropertySource("mockOverride", mockProps)
        );
        
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Mock services disabled");
        response.put("mockEnabled", false);
        response.put("note", "Application restart required for full effect");
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/sample-responses")
    public ResponseEntity<Map<String, Object>> getSampleResponses() {
        Map<String, Object> samples = new HashMap<>();
        
        // Sample JSearch response structure
        Map<String, Object> jSearchSample = new HashMap<>();
        jSearchSample.put("description", "Simulates JSearch API responses with tech companies");
        jSearchSample.put("companies", java.util.Arrays.asList(
            "TCS", "Infosys", "Wipro", "HCL", "Microsoft India", "Amazon India", "Flipkart"
        ));
        jSearchSample.put("salaryRange", "8L - 28L INR");
        jSearchSample.put("remoteChance", "30%");
        jSearchSample.put("maxJobs", 15);
        
        // Sample Adzuna response structure  
        Map<String, Object> adzunaSample = new HashMap<>();
        adzunaSample.put("description", "Simulates Adzuna API responses with traditional companies");
        adzunaSample.put("companies", java.util.Arrays.asList(
            "Reliance", "Tata Group", "Mahindra", "HDFC Bank", "ITC Limited"
        ));
        adzunaSample.put("salaryRange", "6L - 24L INR");
        adzunaSample.put("remoteChance", "15%");
        adzunaSample.put("maxJobs", 12);
        
        samples.put("jSearch", jSearchSample);
        samples.put("adzuna", adzunaSample);
        samples.put("totalDelay", "1-5 seconds per API call");
        samples.put("locations", java.util.Arrays.asList(
            "Bangalore", "Mumbai", "Pune", "Hyderabad", "Chennai", "Delhi"
        ));
        
        return ResponseEntity.ok(samples);
    }
}