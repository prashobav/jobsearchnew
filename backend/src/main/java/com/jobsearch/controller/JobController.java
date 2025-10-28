package com.jobsearch.controller;

import com.jobsearch.dto.JobSearchRequest;
import com.jobsearch.dto.MessageResponse;
import com.jobsearch.entity.Job;
import com.jobsearch.repository.JobRepository;
import com.jobsearch.service.JobAggregatorService;
import com.jobsearch.service.JSearchJobService;
import com.jobsearch.service.mock.MockJobAggregatorService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/jobs")
public class JobController {

    private static final Logger logger = LoggerFactory.getLogger(JobController.class);

    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private JobAggregatorService jobAggregatorService;

    @Autowired
    private JSearchJobService jSearchJobService;

    @Autowired(required = false)
    private MockJobAggregatorService mockJobAggregatorService;

    @Value("${app.mock.enabled:false}")
    private boolean mockEnabled;

    @GetMapping("/all")
    public ResponseEntity<Page<Job>> getAllJobs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        Sort sort = sortDir.equalsIgnoreCase("desc") ? 
            Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Job> jobs = jobRepository.findAll(pageable);

        return ResponseEntity.ok(jobs);
    }

    @GetMapping("/search")
    public ResponseEntity<Page<Job>> searchJobs(
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String company,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) Long minSalary,
            @RequestParam(required = false) Long maxSalary,
            @RequestParam(required = false) Boolean isRemote,
            @RequestParam(required = false) String source,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        try {
            // Sanitize inputs
            title = (title != null && title.trim().isEmpty()) ? null : title;
            company = (company != null && company.trim().isEmpty()) ? null : company;
            location = (location != null && location.trim().isEmpty()) ? null : location;
            source = (source != null && source.trim().isEmpty()) ? null : source;
            
            logger.info("Searching jobs with filters - title: {}, company: {}, location: {}, minSalary: {}, maxSalary: {}, isRemote: {}, source: {}", 
                       title, company, location, minSalary, maxSalary, isRemote, source);
            
            Sort sort = sortDir.equalsIgnoreCase("desc") ? 
                Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
            
            Pageable pageable = PageRequest.of(page, size, sort);

            Page<Job> jobs = jobRepository.findJobsWithFilters(
                title, company, location, minSalary, maxSalary, isRemote, source, pageable);

            logger.info("Found {} jobs matching search criteria", jobs.getTotalElements());
            return ResponseEntity.ok(jobs);
        } catch (Exception e) {
            // Log the error and return empty page instead of 500
            logger.error("Error in search endpoint: {}", e.getMessage(), e);
            
            // Return empty page with same structure
            Pageable pageable = PageRequest.of(page, size);
            return ResponseEntity.ok(Page.empty(pageable));
        }
    }

    @PostMapping("/fetch")
    public ResponseEntity<?> fetchJobs(@Valid @RequestBody JobSearchRequest request) {
        try {
            String serviceType = mockEnabled ? "MOCK" : "REAL";
            CompletableFuture<List<Job>> futureJobs;
            
            if (mockEnabled && mockJobAggregatorService != null) {
                futureJobs = mockJobAggregatorService.fetchJobsFromAllSources(
                    request.getJobTitle(), 
                    request.getLocation(), 
                    request.getMaxResults() / 2 // Split between sources
                );
            } else {
                futureJobs = jobAggregatorService.fetchJobsFromAllSources(
                    request.getJobTitle(), 
                    request.getLocation(), 
                    request.getMaxResults() / 2 // Split between sources
                );
            }

            // For async operation, return immediately with a message
            return ResponseEntity.ok(new MessageResponse(
                serviceType + " job fetch started. Results will be available shortly."
            ));
            
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(new MessageResponse("Error starting job fetch: " + e.getMessage()));
        }
    }

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getJobStats() {
        Map<String, Object> stats = new HashMap<>();
        
        if (mockEnabled && mockJobAggregatorService != null) {
            stats.put("totalJobs", mockJobAggregatorService.getTotalJobCount());
            stats.put("jSearchJobs", mockJobAggregatorService.getJobCountBySource("jsearch"));
            stats.put("adzunaJobs", mockJobAggregatorService.getJobCountBySource("adzuna"));
            stats.put("serviceType", "MOCK");
        } else {
            stats.put("totalJobs", jobAggregatorService.getTotalJobCount());
            stats.put("jSearchJobs", jobAggregatorService.getJobCountBySource("jsearch"));
            stats.put("adzunaJobs", jobAggregatorService.getJobCountBySource("adzuna"));
            stats.put("serviceType", "REAL");
        }
        
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/filters/locations")
    public ResponseEntity<List<String>> getDistinctLocations() {
        List<String> locations;
        if (mockEnabled && mockJobAggregatorService != null) {
            locations = mockJobAggregatorService.getDistinctLocations();
        } else {
            locations = jobAggregatorService.getDistinctLocations();
        }
        return ResponseEntity.ok(locations);
    }

    @GetMapping("/filters/companies")
    public ResponseEntity<List<String>> getDistinctCompanies() {
        List<String> companies;
        if (mockEnabled && mockJobAggregatorService != null) {
            companies = mockJobAggregatorService.getDistinctCompanies();
        } else {
            companies = jobAggregatorService.getDistinctCompanies();
        }
        return ResponseEntity.ok(companies);
    }

    @PostMapping("/fetch/jsearch-only")
    public ResponseEntity<MessageResponse> fetchJobsFromJSearchOnly(@Valid @RequestBody JobSearchRequest request) {
        try {
            logger.info("Fetching jobs from JSearch API only for: {} in {}", request.getJobTitle(), request.getLocation());
            
            if (mockEnabled && mockJobAggregatorService != null) {
                // Use mock service if enabled
                CompletableFuture<List<Job>> future = mockJobAggregatorService.fetchJobsFromAllSources(
                    request.getJobTitle(), 
                    request.getLocation(), 
                    request.getMaxResults()
                );
                List<Job> jobs = future.get();
                return ResponseEntity.ok(new MessageResponse("✅ Mock JSearch completed! Fetched " + jobs.size() + " jobs"));
            } else {
                // Use real JSearch API only - bypass Adzuna completely
                List<Job> jobs = jSearchJobService.fetchAndSaveJobs(
                    request.getJobTitle(), 
                    request.getLocation(), 
                    Math.min(request.getMaxResults(), 20) // Limit to 20 for free tier
                );
                
                return ResponseEntity.ok(new MessageResponse("✅ JSearch API completed! Fetched " + jobs.size() + " jobs from JSearch"));
            }
        } catch (Exception e) {
            logger.error("Error fetching jobs from JSearch only: {}", e.getMessage(), e);
            return ResponseEntity.ok(new MessageResponse("❌ JSearch fetch failed: " + e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Job> getJobById(@PathVariable Long id) {
        return jobRepository.findById(id)
            .map(job -> ResponseEntity.ok().body(job))
            .orElse(ResponseEntity.notFound().build());
    }
}