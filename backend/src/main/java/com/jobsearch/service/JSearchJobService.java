package com.jobsearch.service;

import com.jobsearch.entity.Job;
import com.jobsearch.repository.JobRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class JSearchJobService {
    
    private static final Logger logger = LoggerFactory.getLogger(JSearchJobService.class);
    private final WebClient webClient;
    private final ObjectMapper objectMapper;
    private final JobRepository jobRepository;

    @Value("${app.external-apis.rapidapi.key}")
    private String rapidApiKey;

    @Value("${app.external-apis.rapidapi.host}")
    private String rapidApiHost;

    @Autowired
    public JSearchJobService(WebClient.Builder webClientBuilder, 
                            ObjectMapper objectMapper,
                            JobRepository jobRepository) {
        this.webClient = webClientBuilder.baseUrl("https://jsearch.p.rapidapi.com").build();
        this.objectMapper = objectMapper;
        this.jobRepository = jobRepository;
    }

    public List<Job> fetchAndSaveJobs(String role, String location, int maxResults) {
        logger.info("JSearch API Key configured: {}", !rapidApiKey.isEmpty());
        logger.info("Starting JSearch fetch for role: '{}', location: '{}', maxResults: {}", role, location, maxResults);
        
        if (rapidApiKey.isEmpty()) {
            logger.warn("RapidAPI key not configured for JSearch");
            return new ArrayList<>();
        }

        List<Job> jobs = new ArrayList<>();
        int maxPages = Math.max(1, (maxResults + 9) / 10); // JSearch typically returns 10 results per page

        for (int page = 1; page <= maxPages && jobs.size() < maxResults; page++) {
            try {
                String query = role + " jobs in " + location;
                String encodedQuery = URLEncoder.encode(query, StandardCharsets.UTF_8);
                String url = String.format("/search?query=%s&page=%d&num_pages=1&date_posted=all", encodedQuery, page);
                
                final int currentPage = page; // Make variable effectively final for lambda
                logger.info("Making JSearch API request: {}", url);
                
                // Add delay between requests to avoid rate limiting
                if (page > 1) {
                    Thread.sleep(2000); // 2 second delay between requests
                    logger.debug("Added 2 second delay before page {} request", page);
                }
                
                Mono<String> response = webClient.get()
                        .uri(url)
                        .header("x-rapidapi-key", rapidApiKey)
                        .header("x-rapidapi-host", rapidApiHost)
                        .retrieve()
                        .onStatus(
                            status -> status.value() == 429,
                            clientResponse -> {
                                logger.warn("Rate limit hit (429) for JSearch API on page {}", currentPage);
                                return Mono.error(new RuntimeException("Rate limit exceeded - try again later"));
                            }
                        )
                        .onStatus(
                            status -> status.is4xxClientError(),
                            clientResponse -> {
                                logger.error("Client error {} for JSearch API on page {}", clientResponse.statusCode(), currentPage);
                                return Mono.error(new RuntimeException("Client error: " + clientResponse.statusCode()));
                            }
                        )
                        .bodyToMono(String.class);

                String responseBody = response.block();
                logger.info("JSearch API response length: {} characters", responseBody != null ? responseBody.length() : 0);
                
                List<Job> pageJobs = parseJSearchResponse(responseBody);
                logger.info("Parsed {} jobs from JSearch page {}", pageJobs.size(), page);
                
                for (Job job : pageJobs) {
                    if (jobs.size() >= maxResults) break;
                    
                    // Check if job already exists
                    if (!jobRepository.findByExternalId(job.getExternalId()).isPresent()) {
                        Job savedJob = jobRepository.save(job);
                        jobs.add(savedJob);
                        logger.debug("Saved new job from JSearch: {}", job.getTitle());
                    } else {
                        logger.debug("Job already exists, skipping: {}", job.getExternalId());
                    }
                }

                if (pageJobs.isEmpty()) {
                    logger.info("No more results from JSearch, stopping pagination");
                    break; // No more results
                }
                
            } catch (Exception e) {
                if (e.getMessage().contains("Rate limit exceeded")) {
                    logger.warn("Rate limit hit for JSearch API page {}, stopping further requests", page);
                    break; // Stop making more requests if rate limited
                } else if (e.getMessage().contains("429")) {
                    logger.warn("429 Too Many Requests for JSearch API page {}, waiting 60 seconds before continuing", page);
                    try {
                        Thread.sleep(60000); // Wait 1 minute before retrying
                        continue; // Retry the same page
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                } else {
                    logger.error("Error fetching jobs from JSearch API page {}: {}", page, e.getMessage());
                    break; // Stop on other errors
                }
            }
        }

        logger.info("Fetched {} new jobs from JSearch for query: {} in {}", jobs.size(), role, location);
        return jobs;
    }

    private List<Job> parseJSearchResponse(String responseBody) {
        List<Job> jobs = new ArrayList<>();
        
        try {
            JsonNode root = objectMapper.readTree(responseBody);
            JsonNode data = root.get("data");
            
            if (data != null && data.isArray()) {
                for (JsonNode jobNode : data) {
                    Job job = new Job();
                    
                    // Set external ID
                    String id = jobNode.has("job_id") ? jobNode.get("job_id").asText() : UUID.randomUUID().toString();
                    job.setExternalId("jsearch_" + id);
                    
                    // Set basic fields
                    job.setTitle(jobNode.has("job_title") ? jobNode.get("job_title").asText() : "");
                    job.setCompany(jobNode.has("employer_name") ? jobNode.get("employer_name").asText() : "");
                    job.setLocation(jobNode.has("job_city") ? jobNode.get("job_city").asText() : "");
                    job.setSource("jsearch");
                    
                    // Set salary
                    if (jobNode.has("job_min_salary") && !jobNode.get("job_min_salary").isNull()) {
                        job.setSalaryMin(jobNode.get("job_min_salary").asLong());
                    }
                    if (jobNode.has("job_max_salary") && !jobNode.get("job_max_salary").isNull()) {
                        job.setSalaryMax(jobNode.get("job_max_salary").asLong());
                    }
                    
                    // Set description
                    job.setDescription(jobNode.has("job_description") ? jobNode.get("job_description").asText() : "");
                    
                    // Set URL
                    job.setJobUrl(jobNode.has("job_apply_link") ? jobNode.get("job_apply_link").asText() : "");
                    
                    // Set remote
                    job.setIsRemote(jobNode.has("job_is_remote") && jobNode.get("job_is_remote").asBoolean());
                    
                    // Set empty skills list (JSearch doesn't provide structured skills)
                    job.setSkills(new ArrayList<>());
                    
                    // Set timestamps
                    job.setCreatedAt(LocalDateTime.now());
                    job.setUpdatedAt(LocalDateTime.now());
                    
                    jobs.add(job);
                }
            }
        } catch (Exception e) {
            logger.error("Error parsing JSearch response: {}", e.getMessage());
        }
        
        return jobs;
    }
}