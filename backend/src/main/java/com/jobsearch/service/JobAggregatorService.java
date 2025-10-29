package com.jobsearch.service;

import com.jobsearch.entity.Job;
import com.jobsearch.repository.JobRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Service
public class JobAggregatorService {
    
    private static final Logger logger = LoggerFactory.getLogger(JobAggregatorService.class);
    
    @Autowired
    private JSearchJobService jSearchJobService;
    
    @Autowired
    private JobRepository jobRepository;

    @Async
    public CompletableFuture<List<Job>> fetchJobsFromAllSources(String jobTitle, String location, int maxResultsPerSource) {
        List<Job> allJobs = new ArrayList<>();
        
        try {
            // Use JSearch API as the single primary source (aggregates Indeed, LinkedIn, Glassdoor, etc.)
            int maxResults = Math.min(maxResultsPerSource, 25); // Allow up to 25 results from JSearch
            
            logger.info("Starting JSearch job fetch for: {} in {} (up to {} results)", jobTitle, location, maxResults);
            
            try {
                List<Job> jSearchJobs = jSearchJobService.fetchAndSaveJobs(jobTitle, location, maxResults);
                allJobs.addAll(jSearchJobs);
                logger.info("JSearch fetch completed successfully: {} jobs found", jSearchJobs.size());
                
                if (jSearchJobs.isEmpty()) {
                    logger.warn("No jobs found from JSearch API - might be rate limited or no matches found");
                }
                
            } catch (Exception e) {
                logger.error("JSearch fetch failed: {}", e.getMessage());
                
                if (e.getMessage().contains("Rate limit") || e.getMessage().contains("429")) {
                    logger.warn("JSearch API rate limit exceeded. Consider upgrading plan for more requests.");
                } else {
                    logger.error("JSearch API error: {}", e.getMessage());
                }
            }
            
            logger.info("Job aggregation completed. Total new jobs from JSearch: {}", allJobs.size());
            
        } catch (Exception e) {
            logger.error("Error during job aggregation: {}", e.getMessage(), e);
        }
        
        return CompletableFuture.completedFuture(allJobs);
    }

    public long getTotalJobCount() {
        return jobRepository.count();
    }

    public long getJobCountBySource(String source) {
        return jobRepository.countJobsBySource(source);
    }

    public List<String> getDistinctLocations() {
        return jobRepository.findDistinctLocations();
    }

    public List<String> getDistinctCompanies() {
        return jobRepository.findDistinctCompanies();
    }
}