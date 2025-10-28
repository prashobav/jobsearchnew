package com.jobsearch.service.mock;

import com.jobsearch.entity.Job;
import com.jobsearch.repository.JobRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

@Service
@ConditionalOnProperty(name = "app.mock.enabled", havingValue = "true")
public class MockAdzunaJobService {
    
    private static final Logger logger = LoggerFactory.getLogger(MockAdzunaJobService.class);
    private final JobRepository jobRepository;
    private final Random random = new Random();

    @Autowired
    public MockAdzunaJobService(JobRepository jobRepository) {
        this.jobRepository = jobRepository;
    }

    public List<Job> fetchAndSaveJobs(String what, String where, int maxResults) {
        logger.info("MOCK: Starting Adzuna fetch for what: '{}', where: '{}', maxResults: {}", what, where, maxResults);
        
        // Simulate API delay
        try {
            Thread.sleep(800 + random.nextInt(1500)); // 0.8-2.3 second delay
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        List<Job> mockJobs = generateMockAdzunaJobs(what, where, maxResults);
        List<Job> savedJobs = new ArrayList<>();

        for (Job job : mockJobs) {
            // Check if job already exists
            if (!jobRepository.findByExternalId(job.getExternalId()).isPresent()) {
                Job savedJob = jobRepository.save(job);
                savedJobs.add(savedJob);
                logger.debug("MOCK: Saved new job from Adzuna: {}", job.getTitle());
            } else {
                logger.debug("MOCK: Job already exists, skipping: {}", job.getExternalId());
            }
        }

        logger.info("MOCK: Fetched {} new jobs from Adzuna for query: {} in {}", savedJobs.size(), what, where);
        return savedJobs;
    }

    private List<Job> generateMockAdzunaJobs(String what, String where, int maxResults) {
        List<Job> jobs = new ArrayList<>();
        
        String[] companies = {
            "Reliance Industries", "Tata Group", "Mahindra Group", "Aditya Birla Group",
            "Godrej Group", "L&T", "ITC Limited", "Bajaj Group", "Wipro Consumer",
            "Asian Paints", "HDFC Bank", "ICICI Bank", "Kotak Mahindra", "Yes Bank"
        };
        
        String[] locations = {
            "Mumbai, Maharashtra", "Delhi, Delhi", "Bangalore, Karnataka", 
            "Chennai, Tamil Nadu", "Kolkata, West Bengal", "Pune, Maharashtra",
            "Ahmedabad, Gujarat", "Surat, Gujarat", "Jaipur, Rajasthan"
        };
        
        String[] jobTitles = {
            what + " - Business Operations", 
            "Deputy " + what,
            what + " - Strategic Planning",
            "Regional " + what,
            what + " - Business Development",
            what + " - Finance & Operations",
            "Assistant " + what,
            what + " - Corporate Affairs"
        };
        
        String[] descriptions = {
            "Excellent opportunity to work with one of India's leading organizations. Drive business growth and operational excellence.",
            "Join our leadership team and contribute to strategic decision-making while managing diverse business functions.",
            "We seek a dynamic professional to lead business initiatives and foster organizational growth in a collaborative environment.",
            "Take on challenging responsibilities in a growth-oriented company with strong market presence and expansion plans.",
            "Lead cross-functional teams and drive innovation in a traditional industry undergoing digital transformation."
        };

        for (int i = 0; i < Math.min(maxResults, 12); i++) {
            Job job = new Job();
            
            // Generate unique external ID
            job.setExternalId("mock_adzuna_" + System.currentTimeMillis() + "_" + i);
            
            // Set job details
            job.setTitle(jobTitles[random.nextInt(jobTitles.length)]);
            job.setCompany(companies[random.nextInt(companies.length)]);
            job.setLocation(where.isEmpty() ? locations[random.nextInt(locations.length)] : where);
            job.setSource("adzuna");
            
            // Set salary (in INR) - Adzuna typically has more traditional companies with different salary ranges
            long baseSalary = 600000 + random.nextInt(1800000); // 6L to 24L base
            job.setSalaryMin(baseSalary);
            job.setSalaryMax(baseSalary + 400000 + random.nextInt(800000)); // +4L to +12L range
            
            // Set description
            job.setDescription(descriptions[random.nextInt(descriptions.length)]);
            
            // Set URL
            job.setJobUrl("https://adzuna.in/mock-job-" + i);
            
            // Set remote (15% chance - traditional companies less likely to offer remote)
            job.setIsRemote(random.nextInt(100) < 15);
            
            // Set skills
            List<String> skills = Arrays.asList(
                what.toLowerCase(), "business management", "strategic planning", 
                "stakeholder management", "operations", "finance"
            );
            job.setSkills(skills);
            
            // Set timestamps
            job.setCreatedAt(LocalDateTime.now());
            job.setUpdatedAt(LocalDateTime.now());
            
            jobs.add(job);
        }
        
        return jobs;
    }
}