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
public class MockJSearchJobService {
    
    private static final Logger logger = LoggerFactory.getLogger(MockJSearchJobService.class);
    private final JobRepository jobRepository;
    private final Random random = new Random();

    @Autowired
    public MockJSearchJobService(JobRepository jobRepository) {
        this.jobRepository = jobRepository;
    }

    public List<Job> fetchAndSaveJobs(String role, String location, int maxResults) {
        logger.info("MOCK: Starting JSearch fetch for role: '{}', location: '{}', maxResults: {}", role, location, maxResults);
        
        // Simulate API delay
        try {
            Thread.sleep(1000 + random.nextInt(2000)); // 1-3 second delay
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        List<Job> mockJobs = generateMockJSearchJobs(role, location, maxResults);
        List<Job> savedJobs = new ArrayList<>();

        for (Job job : mockJobs) {
            // Check if job already exists
            if (!jobRepository.findByExternalId(job.getExternalId()).isPresent()) {
                Job savedJob = jobRepository.save(job);
                savedJobs.add(savedJob);
                logger.debug("MOCK: Saved new job from JSearch: {}", job.getTitle());
            } else {
                logger.debug("MOCK: Job already exists, skipping: {}", job.getExternalId());
            }
        }

        logger.info("MOCK: Fetched {} new jobs from JSearch for query: {} in {}", savedJobs.size(), role, location);
        return savedJobs;
    }

    private List<Job> generateMockJSearchJobs(String role, String location, int maxResults) {
        List<Job> jobs = new ArrayList<>();
        
        String[] companies = {
            "TCS", "Infosys", "Wipro", "HCL Technologies", "Tech Mahindra", 
            "Cognizant", "Accenture India", "IBM India", "Microsoft India", "Amazon India",
            "Flipkart", "Swiggy", "Zomato", "Ola", "Paytm", "BYJU'S"
        };
        
        String[] locations = {
            "Bangalore, Karnataka", "Mumbai, Maharashtra", "Pune, Maharashtra", 
            "Hyderabad, Telangana", "Chennai, Tamil Nadu", "Gurgaon, Haryana",
            "Noida, Uttar Pradesh", "Kolkata, West Bengal"
        };
        
        String[] jobTitles = {
            role + " - Technology", 
            "Senior " + role,
            role + " - Product Development",
            "Lead " + role,
            role + " - Operations",
            role + " - Digital Transformation",
            role + " - Analytics",
            "Associate " + role
        };
        
        String[] descriptions = {
            "Join our dynamic team and lead innovative projects in a fast-paced environment. We offer competitive compensation and excellent growth opportunities.",
            "We are looking for an experienced professional to drive strategic initiatives and manage cross-functional teams.",
            "Exciting opportunity to work with cutting-edge technology and make a significant impact on our business operations.",
            "Lead and mentor a team of professionals while driving operational excellence and customer satisfaction.",
            "Work on challenging projects with global impact while collaborating with diverse, talented teams."
        };

        for (int i = 0; i < Math.min(maxResults, 15); i++) {
            Job job = new Job();
            
            // Generate unique external ID
            job.setExternalId("mock_jsearch_" + System.currentTimeMillis() + "_" + i);
            
            // Set job details
            job.setTitle(jobTitles[random.nextInt(jobTitles.length)]);
            job.setCompany(companies[random.nextInt(companies.length)]);
            job.setLocation(location.isEmpty() ? locations[random.nextInt(locations.length)] : location);
            job.setSource("jsearch");
            
            // Set salary (in INR)
            long baseSalary = 800000 + random.nextInt(2000000); // 8L to 28L base
            job.setSalaryMin(baseSalary);
            job.setSalaryMax(baseSalary + 500000 + random.nextInt(1000000)); // +5L to +15L range
            
            // Set description
            job.setDescription(descriptions[random.nextInt(descriptions.length)]);
            
            // Set URL
            job.setJobUrl("https://example.com/mock-job-" + i);
            
            // Set remote (30% chance)
            job.setIsRemote(random.nextBoolean() && random.nextBoolean());
            
            // Set skills
            List<String> skills = Arrays.asList(
                role.toLowerCase(), "leadership", "teamwork", "communication", 
                "project management", "strategy", "analytics"
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