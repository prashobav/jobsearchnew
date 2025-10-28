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
    private AdzunaJobService adzunaJobService;
    
    @Autowired
    private JSearchJobService jSearchJobService;
    
    @Autowired
    private JobRepository jobRepository;

    @Async
    public CompletableFuture<List<Job>> fetchJobsFromAllSources(String jobTitle, String location, int maxResultsPerSource) {
        List<Job> allJobs = new ArrayList<>();
        
        try {
            // Prioritize JSearch API as primary source (works with free tier)
            int jSearchResults = Math.min(maxResultsPerSource, 20); // More results from JSearch
            int adzunaResults = Math.min(maxResultsPerSource, 5);   // Fewer from Adzuna (free tier limited)
            
            // Fetch from JSearch (includes Indeed, LinkedIn, etc.) - PRIMARY SOURCE
            logger.info("Starting JSearch job fetch for: {} in {} (limited to {} results)", jobTitle, location, jSearchResults);
            try {
                List<Job> jSearchJobs = jSearchJobService.fetchAndSaveJobs(jobTitle, location, jSearchResults);
                allJobs.addAll(jSearchJobs);
                logger.info("JSearch fetch completed: {} jobs", jSearchJobs.size());
            } catch (Exception e) {
                logger.warn("JSearch fetch failed: {}", e.getMessage());
            }
            
            // Add delay between different API calls
            Thread.sleep(3000); // 3 second delay between different APIs
            
            // Fetch from Adzuna (SECONDARY SOURCE - limited free tier)
            logger.info("Starting Adzuna job fetch for: {} in {} (limited to {} results due to free tier)", jobTitle, location, adzunaResults);
            try {
                List<Job> adzunaJobs = adzunaJobService.fetchAndSaveJobs(jobTitle, location, adzunaResults);
                allJobs.addAll(adzunaJobs);
                logger.info("Adzuna fetch completed: {} jobs", adzunaJobs.size());
            } catch (Exception e) {
                logger.warn("Adzuna fetch failed (likely free tier limitation): {}", e.getMessage());
                logger.info("Continuing with JSearch results only...");
            }
            
            logger.info("Completed job aggregation. Total new jobs: {}", allJobs.size());
            
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