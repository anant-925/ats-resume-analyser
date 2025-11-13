package com.anant.ats.resumeanalyser.repository;

import com.anant.ats.resumeanalyser.model.AnalysisReport;
import com.anant.ats.resumeanalyser.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AnalysisReportRepository extends JpaRepository<AnalysisReport, Long> {
    
    List<AnalysisReport> findByUserOrderByAnalysisDateDesc(User user);

}