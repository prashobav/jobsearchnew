package com.jobsearch.service.mock;

import com.jobsearch.entity.Job;
import com.jobsearch.repository.JobRepository;
import com.jobsearch.service.mock.MockAdzunaJobService;
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
    private MockAdzunaJobService mockAdzunaJobService;
    
    @Autowired
    private MockJSearchJobService mockJSearchJobService;
    
    @Autowired
    private JobRepository jobRepository;

    @Async
    public CompletableFuture<List<Job>> fetchJobsFromAllSources(String jobTitle, String location, int maxResultsPerSource) {
        List<Job> allJobs = new ArrayList<>();
        
        try {
            // Limit results to prevent overwhelming mock data
            int limitedResults = Math.min(maxResultsPerSource, 8); // Max 8 per source for testing
            
            logger.info("MOCK: Starting job aggregation for: {} in {} (limited to {} results per source)", 
                       jobTitle, location, limitedResults);
            
            // Fetch from Mock JSearch
            logger.info("MOCK: Starting JSearch job fetch for: {} in {}", jobTitle, location);
            try {
                List<Job> jSearchJobs = mockJSearchJobService.fetchAndSaveJobs(jobTitle, location, limitedResults);
                allJobs.addAll(jSearchJobs);
                logger.info("MOCK: JSearch fetch completed: {} jobs", jSearchJobs.size());
            } catch (Exception e) {
                logger.warn("MOCK: JSearch fetch failed: {}", e.getMessage());
            }
            
            // Add delay between different API calls to simulate real behavior
            Thread.sleep(2000); // 2 second delay between different APIs
            
            // Fetch from Mock Adzuna
            logger.info("MOCK: Starting Adzuna job fetch for: {} in {}", jobTitle, location);
            try {
                List<Job> adzunaJobs = mockAdzunaJobService.fetchAndSaveJobs(jobTitle, location, limitedResults);
                allJobs.addAll(adzunaJobs);
                logger.info("MOCK: Adzuna fetch completed: {} jobs", adzunaJobs.size());
            } catch (Exception e) {
                logger.warn("MOCK: Adzuna fetch failed: {}", e.getMessage());
            }
            
            logger.info("MOCK: Completed job aggregation. Total new jobs: {}", allJobs.size());
            
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