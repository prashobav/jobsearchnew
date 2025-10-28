package com.jobsearch.controller;

import com.jobsearch.dto.JobSearchRequest;
import com.jobsearch.dto.MessageResponse;
import com.jobsearch.entity.Job;
import com.jobsearch.repository.JobRepository;
import com.jobsearch.service.JobAggregatorService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
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

    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private JobAggregatorService jobAggregatorService;

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

        Sort sort = sortDir.equalsIgnoreCase("desc") ? 
            Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<Job> jobs = jobRepository.findJobsWithFilters(
            title, company, location, minSalary, maxSalary, isRemote, source, pageable);

        return ResponseEntity.ok(jobs);
    }

    @PostMapping("/fetch")
    public ResponseEntity<?> fetchJobs(@Valid @RequestBody JobSearchRequest request) {
        try {
            CompletableFuture<List<Job>> futureJobs = jobAggregatorService.fetchJobsFromAllSources(
                request.getJobTitle(), 
                request.getLocation(), 
                request.getMaxResults() / 2 // Split between sources
            );

            // For async operation, return immediately with a message
            return ResponseEntity.ok(new MessageResponse("Job fetch started. Results will be available shortly."));
            
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(new MessageResponse("Error starting job fetch: " + e.getMessage()));
        }
    }

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getJobStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalJobs", jobAggregatorService.getTotalJobCount());
        stats.put("jSearchJobs", jobAggregatorService.getJobCountBySource("jsearch"));
        stats.put("adzunaJobs", jobAggregatorService.getJobCountBySource("adzuna"));
        
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/filters/locations")
    public ResponseEntity<List<String>> getDistinctLocations() {
        List<String> locations = jobAggregatorService.getDistinctLocations();
        return ResponseEntity.ok(locations);
    }

    @GetMapping("/filters/companies")
    public ResponseEntity<List<String>> getDistinctCompanies() {
        List<String> companies = jobAggregatorService.getDistinctCompanies();
        return ResponseEntity.ok(companies);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Job> getJobById(@PathVariable Long id) {
        return jobRepository.findById(id)
            .map(job -> ResponseEntity.ok().body(job))
            .orElse(ResponseEntity.notFound().build());
    }
}