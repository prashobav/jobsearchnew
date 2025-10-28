package com.jobsearch.config;

import com.jobsearch.entity.ERole;
import com.jobsearch.entity.Job;
import com.jobsearch.entity.Role;
import com.jobsearch.repository.JobRepository;
import com.jobsearch.repository.RoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Arrays;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private RoleRepository roleRepository;
    
    @Autowired
    private JobRepository jobRepository;

    @Override
    public void run(String... args) throws Exception {
        // Initialize roles if they don't exist
        if (roleRepository.findByName(ERole.ROLE_USER).isEmpty()) {
            roleRepository.save(new Role(ERole.ROLE_USER));
        }
        
        if (roleRepository.findByName(ERole.ROLE_ADMIN).isEmpty()) {
            roleRepository.save(new Role(ERole.ROLE_ADMIN));
        }
        
        // Initialize sample job data if database is empty
        if (jobRepository.count() == 0) {
            createSampleJobs();
        }
    }
    
    private void createSampleJobs() {
        Job[] sampleJobs = {
            createJob("Senior Software Manager", "Infosys Technologies", "Bangalore, Karnataka", 
                     2500000L, 4000000L, false, "software development team leadership project management agile",
                     "Lead a team of 10+ software engineers developing cutting-edge applications.", 
                     "https://example.com/job1", "sample"),
                     
            createJob("Project Manager", "Tata Consultancy Services", "Mumbai, Maharashtra", 
                     1800000L, 2800000L, false, "project management pmp scrum stakeholder communication",
                     "Manage multiple projects from conception to completion.", 
                     "https://example.com/job2", "sample"),
                     
            createJob("Product Manager", "Flipkart", "Bangalore, Karnataka", 
                     2200000L, 3500000L, true, "product strategy roadmap user research market analysis",
                     "Drive product vision and strategy for our flagship products.", 
                     "https://example.com/job3", "sample"),
                     
            createJob("Marketing Manager", "Hindustan Unilever", "Mumbai, Maharashtra", 
                     1500000L, 2500000L, false, "digital marketing campaigns brand management analytics",
                     "Develop and execute comprehensive marketing strategies.", 
                     "https://example.com/job4", "sample"),
                     
            createJob("Operations Manager", "Amazon India", "Hyderabad, Telangana", 
                     1800000L, 2800000L, false, "operations supply chain process improvement lean six sigma",
                     "Optimize operational processes and manage day-to-day operations.", 
                     "https://example.com/job5", "sample"),
                     
            createJob("Engineering Manager", "Microsoft India", "Bangalore, Karnataka", 
                     3000000L, 5000000L, false, "engineering leadership cloud architecture team building",
                     "Lead engineering teams building scalable cloud infrastructure.", 
                     "https://example.com/job6", "sample"),
                     
            createJob("Data Manager", "Wipro Digital", "Pune, Maharashtra", 
                     1600000L, 2400000L, false, "data management sql python data analysis reporting",
                     "Manage data pipelines and ensure data quality across systems.", 
                     "https://example.com/job7", "sample"),
                     
            createJob("Account Manager", "Tech Mahindra", "Chennai, Tamil Nadu", 
                     1200000L, 2000000L, false, "client relations sales account management crm communication",
                     "Manage key client accounts and drive revenue growth.", 
                     "https://example.com/job8", "sample")
        };
        
        jobRepository.saveAll(Arrays.asList(sampleJobs));
        System.out.println("✅ Created " + sampleJobs.length + " sample jobs for testing");
    }
    
    private Job createJob(String title, String company, String location, Long salaryMin, Long salaryMax, 
                         Boolean isRemote, String skillsStr, String description, String jobUrl, String source) {
        Job job = new Job();
        job.setExternalId("sample_" + System.currentTimeMillis() + "_" + Math.random());
        job.setTitle(title);
        job.setCompany(company);
        job.setLocation(location);
        job.setSalaryMin(salaryMin);
        job.setSalaryMax(salaryMax);
        job.setIsRemote(isRemote);
        job.setSkills(Arrays.asList(skillsStr.split(" ")));
        job.setDescription(description);
        job.setJobUrl(jobUrl);
        job.setSource(source);
        job.setCreatedAt(LocalDateTime.now());
        job.setUpdatedAt(LocalDateTime.now());
        return job;
    }
}