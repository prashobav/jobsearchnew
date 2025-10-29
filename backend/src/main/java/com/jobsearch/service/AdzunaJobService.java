package com.jobsearch.service;

import com.jobsearch.entity.Job;
import com.jobsearch.repository.JobRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@ConditionalOnProperty(name = "app.adzuna.enabled", havingValue = "true")
public class AdzunaJobService {
    
    private static final Logger logger = LoggerFactory.getLogger(AdzunaJobService.class);
    private final WebClient webClient;
    private final ObjectMapper objectMapper;
    private final JobRepository jobRepository;

    @Value("${app.external-apis.adzuna.app-id}")
    private String appId;

    @Value("${app.external-apis.adzuna.app-key}")
    private String appKey;

    @Autowired
    public AdzunaJobService(WebClient.Builder webClientBuilder, 
                           ObjectMapper objectMapper,
                           JobRepository jobRepository) {
        this.webClient = webClientBuilder.baseUrl("https://api.adzuna.com").build();
        this.objectMapper = objectMapper;
        this.jobRepository = jobRepository;
    }

    public List<Job> fetchAndSaveJobs(String what, String where, int maxResults) {
        if (appId.isEmpty() || appKey.isEmpty()) {
            logger.warn("Adzuna API credentials not configured");
            return new ArrayList<>();
        }

        List<Job> jobs = new ArrayList<>();
        int resultsPerPage = 50;
        int pages = Math.max(1, (maxResults + resultsPerPage - 1) / resultsPerPage);

        for (int page = 1; page <= pages && jobs.size() < maxResults; page++) {
            try {
                String url = buildUrl(what, where, page, resultsPerPage);
                
                Mono<String> response = webClient.get()
                        .uri(url)
                        .retrieve()
                        .bodyToMono(String.class);

                String responseBody = response.block();
                List<Job> pageJobs = parseAdzunaResponse(responseBody);
                
                for (Job job : pageJobs) {
                    if (jobs.size() >= maxResults) break;
                    
                    // Check if job already exists
                    if (!jobRepository.findByExternalId(job.getExternalId()).isPresent()) {
                        Job savedJob = jobRepository.save(job);
                        jobs.add(savedJob);
                        logger.debug("Saved new job from Adzuna: {}", job.getTitle());
                    }
                }

                if (pageJobs.size() < resultsPerPage) break; // No more results
                
            } catch (Exception e) {
                logger.error("Error fetching jobs from Adzuna API page {}: {}", page, e.getMessage());
                break;
            }
        }

        logger.info("Fetched {} new jobs from Adzuna for query: {} in {}", jobs.size(), what, where);
        return jobs;
    }

    private String buildUrl(String what, String where, int page, int resultsPerPage) {
        String encodedWhat = URLEncoder.encode(what, StandardCharsets.UTF_8);
        String encodedWhere = URLEncoder.encode(where, StandardCharsets.UTF_8);
        String encodedAppId = URLEncoder.encode(appId, StandardCharsets.UTF_8);
        String encodedAppKey = URLEncoder.encode(appKey, StandardCharsets.UTF_8);
        
        // Use India-specific endpoint (/v1/api/jobs/in/search) for Indian job market
        return String.format("/v1/api/jobs/in/search/%d?app_id=%s&app_key=%s&results_per_page=%d&what=%s&where=%s&content-type=application/json",
                page, encodedAppId, encodedAppKey, resultsPerPage, encodedWhat, encodedWhere);
    }

    private List<Job> parseAdzunaResponse(String responseBody) {
        List<Job> jobs = new ArrayList<>();
        
        try {
            JsonNode root = objectMapper.readTree(responseBody);
            JsonNode results = root.get("results");
            
            if (results != null && results.isArray()) {
                for (JsonNode jobNode : results) {
                    Job job = new Job();
                    
                    // Set external ID
                    String id = jobNode.has("id") ? jobNode.get("id").asText() : UUID.randomUUID().toString();
                    job.setExternalId("adzuna_" + id);
                    
                    // Set basic fields
                    job.setTitle(jobNode.has("title") ? jobNode.get("title").asText() : "");
                    job.setSource("adzuna");
                    
                    // Set company
                    if (jobNode.has("company") && jobNode.get("company").has("display_name")) {
                        job.setCompany(jobNode.get("company").get("display_name").asText());
                    } else {
                        job.setCompany("");
                    }
                    
                    // Set location
                    if (jobNode.has("location") && jobNode.get("location").has("display_name")) {
                        job.setLocation(jobNode.get("location").get("display_name").asText());
                    }
                    
                    // Set salary
                    if (jobNode.has("salary_min") && !jobNode.get("salary_min").isNull()) {
                        job.setSalaryMin(jobNode.get("salary_min").asLong());
                    }
                    if (jobNode.has("salary_max") && !jobNode.get("salary_max").isNull()) {
                        job.setSalaryMax(jobNode.get("salary_max").asLong());
                    }
                    
                    // Set description
                    job.setDescription(jobNode.has("description") ? jobNode.get("description").asText() : "");
                    
                    // Set URL
                    job.setJobUrl(jobNode.has("redirect_url") ? jobNode.get("redirect_url").asText() : "");
                    
                    // Set remote (default false for Adzuna)
                    job.setIsRemote(false);
                    
                    // Set empty skills list
                    job.setSkills(new ArrayList<>());
                    
                    jobs.add(job);
                }
            }
        } catch (Exception e) {
            logger.error("Error parsing Adzuna response: {}", e.getMessage());
        }
        
        return jobs;
    }
}