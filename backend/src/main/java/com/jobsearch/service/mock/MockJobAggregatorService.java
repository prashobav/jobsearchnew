package com.jobsearch.service.mock;

import com.jobsearch.entity.Job;
import com.jobsearch.repository.JobRepository;
import com.jobsearch.service.mock.MockJSearchJobService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Service
@ConditionalOnProperty(name = "app.mock.enabled", havingValue = "true")
public class MockJobAggregatorService {
    
    private static final Logger logger = LoggerFactory.getLogger(MockJobAggregatorService.class);
    
    @Autowired
    private MockJSearchJobService mockJSearchJobService;
    
    @Autowired
    private JobRepository jobRepository;

    @Async
    public CompletableFuture<List<Job>> fetchJobsFromAllSources(String jobTitle, String location, int maxResultsPerSource) {
        List<Job> allJobs = new ArrayList<>();
        
        try {
            // Use JSearch Mock only (simulating JSearch aggregating multiple sources)
            int maxResults = Math.min(maxResultsPerSource, 20); // Up to 20 mock jobs for testing
            
            logger.info("MOCK: Starting JSearch job aggregation for: {} in {} (up to {} results)", 
                       jobTitle, location, maxResults);
            
            // Fetch from Mock JSearch (simulates aggregated data from Indeed, LinkedIn, etc.)
            try {
                List<Job> jSearchJobs = mockJSearchJobService.fetchAndSaveJobs(jobTitle, location, maxResults);
                allJobs.addAll(jSearchJobs);
                logger.info("MOCK: JSearch fetch completed successfully: {} jobs found", jSearchJobs.size());
                
                if (jSearchJobs.isEmpty()) {
                    logger.warn("MOCK: No jobs found from JSearch mock service");
                }
                
            } catch (Exception e) {
                logger.error("MOCK: JSearch fetch failed: {}", e.getMessage());
            }
            
            logger.info("MOCK: Job aggregation completed. Total new jobs from JSearch: {}", allJobs.size());
            
        } catch (Exception e) {
            logger.error("MOCK: Error during job aggregation: {}", e.getMessage(), e);
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