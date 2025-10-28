package com.jobsearch.repository;

import com.jobsearch.entity.Job;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface JobRepository extends JpaRepository<Job, Long> {
    
    Optional<Job> findByExternalId(String externalId);
    
    @Query("SELECT j FROM Job j WHERE " +
           "(:title IS NULL OR :title = '' OR UPPER(j.title) LIKE UPPER(CONCAT('%', :title, '%'))) AND " +
           "(:company IS NULL OR :company = '' OR UPPER(j.company) LIKE UPPER(CONCAT('%', :company, '%'))) AND " +
           "(:location IS NULL OR :location = '' OR UPPER(j.location) LIKE UPPER(CONCAT('%', :location, '%'))) AND " +
           "(:minSalary IS NULL OR j.salaryMax >= :minSalary) AND " +
           "(:maxSalary IS NULL OR j.salaryMin <= :maxSalary) AND " +
           "(:isRemote IS NULL OR j.isRemote = :isRemote) AND " +
           "(:source IS NULL OR :source = '' OR j.source = :source)")
    Page<Job> findJobsWithFilters(
        @Param("title") String title,
        @Param("company") String company,
        @Param("location") String location,
        @Param("minSalary") Long minSalary,
        @Param("maxSalary") Long maxSalary,
        @Param("isRemote") Boolean isRemote,
        @Param("source") String source,
        Pageable pageable
    );
    
    @Query("SELECT j FROM Job j JOIN j.skills s WHERE " +
           "s IN :skills")
    Page<Job> findJobsBySkills(@Param("skills") List<String> skills, Pageable pageable);
    
    List<Job> findByCreatedAtAfter(LocalDateTime date);
    
    @Query("SELECT COUNT(j) FROM Job j WHERE j.source = :source")
    Long countJobsBySource(@Param("source") String source);
    
    @Query("SELECT DISTINCT j.location FROM Job j WHERE j.location IS NOT NULL ORDER BY j.location")
    List<String> findDistinctLocations();
    
    @Query("SELECT DISTINCT j.company FROM Job j WHERE j.company IS NOT NULL ORDER BY j.company")
    List<String> findDistinctCompanies();
}