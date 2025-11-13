package com.anant.ats.resumeanalyser.model;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Column;

import java.time.LocalDateTime;

@Entity
@Table(name = "analysis_reports")
public class AnalysisReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // This is the "foreign key" that links this report to a user
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String fileName;

    @Column(nullable = false)
    private String jobDescriptionSummary; // e.g., "Software Engineer at Google"

    // Use @Lob for large text fields (like our AI report)
    @Lob
    @Column(columnDefinition = "TEXT", nullable = false)
    private String aiReport;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String complianceWarnings; // Storing the list as a simple string for now

    private LocalDateTime analysisDate;

    // --- Getters & Setters ---

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getJobDescriptionSummary() {
        return jobDescriptionSummary;
    }

    public void setJobDescriptionSummary(String jobDescriptionSummary) {
        this.jobDescriptionSummary = jobDescriptionSummary;
    }

    public String getAiReport() {
        return aiReport;
    }

    public void setAiReport(String aiReport) {
        this.aiReport = aiReport;
    }

    public String getComplianceWarnings() {
        return complianceWarnings;
    }

    public void setComplianceWarnings(String complianceWarnings) {
        this.complianceWarnings = complianceWarnings;
    }

    public LocalDateTime getAnalysisDate() {
        return analysisDate;
    }

    public void setAnalysisDate(LocalDateTime analysisDate) {
        this.analysisDate = analysisDate;
    }
}