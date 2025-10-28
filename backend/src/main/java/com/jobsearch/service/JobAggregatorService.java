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
            // Fetch from JSearch (includes Indeed, LinkedIn, etc.)
            logger.info("Starting JSearch job fetch for: {} in {}", jobTitle, location);
            List<Job> jSearchJobs = jSearchJobService.fetchAndSaveJobs(jobTitle, location, maxResultsPerSource);
            allJobs.addAll(jSearchJobs);
            
            // Fetch from Adzuna
            logger.info("Starting Adzuna job fetch for: {} in {}", jobTitle, location);
            List<Job> adzunaJobs = adzunaJobService.fetchAndSaveJobs(jobTitle, location, maxResultsPerSource);
            allJobs.addAll(adzunaJobs);
            
            logger.info("Completed job aggregation. Total new jobs: {} (JSearch: {}, Adzuna: {})", 
                       allJobs.size(), jSearchJobs.size(), adzunaJobs.size());
            
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