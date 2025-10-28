package com.jobsearch.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "user_profiles")
public class UserProfile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private User user;

    @ElementCollection
    @CollectionTable(name = "profile_desired_titles", joinColumns = @JoinColumn(name = "profile_id"))
    @Column(name = "title")
    private List<String> desiredTitles;

    @ElementCollection
    @CollectionTable(name = "profile_skills", joinColumns = @JoinColumn(name = "profile_id"))
    @Column(name = "skill")
    private List<String> skills;

    @Column(name = "years_experience")
    private Integer yearsExperience = 0;

    @Column(name = "min_salary")
    private Long minSalary = 0L;

    @ElementCollection
    @CollectionTable(name = "profile_locations", joinColumns = @JoinColumn(name = "profile_id"))
    @Column(name = "location")
    private List<String> locations;

    @Column(name = "remote_ok")
    private Boolean remoteOk = false;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Constructors
    public UserProfile() {}

    public UserProfile(User user) {
        this.user = user;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public List<String> getDesiredTitles() { return desiredTitles; }
    public void setDesiredTitles(List<String> desiredTitles) { this.desiredTitles = desiredTitles; }

    public List<String> getSkills() { return skills; }
    public void setSkills(List<String> skills) { this.skills = skills; }

    public Integer getYearsExperience() { return yearsExperience; }
    public void setYearsExperience(Integer yearsExperience) { this.yearsExperience = yearsExperience; }

    public Long getMinSalary() { return minSalary; }
    public void setMinSalary(Long minSalary) { this.minSalary = minSalary; }

    public List<String> getLocations() { return locations; }
    public void setLocations(List<String> locations) { this.locations = locations; }

    public Boolean getRemoteOk() { return remoteOk; }
    public void setRemoteOk(Boolean remoteOk) { this.remoteOk = remoteOk; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}