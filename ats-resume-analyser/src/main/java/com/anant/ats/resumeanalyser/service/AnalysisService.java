package com.anant.ats.resumeanalyser.service;

import com.anant.ats.resumeanalyser.model.AnalysisReport;
import com.anant.ats.resumeanalyser.model.User;
import com.anant.ats.resumeanalyser.repository.AnalysisReportRepository;
import com.anant.ats.resumeanalyser.repository.UserRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class AnalysisService {

    @Autowired
    private SuggestionService suggestionService;
    
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AnalysisReportRepository reportRepository;

    // This is the object our controller receives
    public record AnalysisResult(
        String summary,
        String aiSuggestions,
        List<String> complianceWarnings
    ) {}

    /**
     * This is the main public method called by the controller.
     * It runs the analysis AND saves the report to the database.
     */
    public AnalysisResult analyzeAndSave(String resumeText, String jdText, String companyType, MultipartFile file, String username) {
        
        // 1. Run the private analysis method
        AnalysisResult result = analyze(resumeText, jdText, companyType, file);

        // 2. Get the logged-in user
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        // 3. Create a new report object
        AnalysisReport report = new AnalysisReport();
        report.setUser(user);
        report.setFileName(file.getOriginalFilename());
        report.setJobDescriptionSummary(jdText.substring(0, Math.min(jdText.length(), 100)) + "..."); // Create a short summary
        report.setAiReport(result.aiSuggestions());
        report.setComplianceWarnings(String.join("\n", result.complianceWarnings())); // Convert list to a string
        report.setAnalysisDate(LocalDateTime.now());

        // 4. Save the report to the database
        reportRepository.save(report);

        // 5. Return the result to the controller
        return result;
    }


    /**
     * This private method performs the actual analysis.
     * It correctly accepts 'jdText' as a parameter.
     */
    private AnalysisResult analyze(String resumeText, String jdText, String companyType, MultipartFile file) {
        
        List<String> complianceWarnings = runComplianceChecks(resumeText, file);
        
        // Call the AI service (which also needs jdText)
        String aiFeedback = suggestionService.generateSuggestions(resumeText, jdText, companyType);
        
        String summary = "AI analysis complete. See the full report below.";

        return new AnalysisResult(summary, aiFeedback, complianceWarnings);
    }
    
    /**
     * This method runs simple, code-based checks.
     */
    private List<String> runComplianceChecks(String resumeText, MultipartFile file) {
        List<String> warnings = new ArrayList<>();
        
        // File Type Check
        String filename = file.getOriginalFilename();
        if (filename != null && !filename.endsWith(".pdf") && !filename.endsWith(".docx")) {
            warnings.add("File Type: You uploaded a '" + filename.substring(filename.lastIndexOf(".") + 1) + "' file. For ATS, always use .pdf or .docx.");
        }

        // Consistent Dates Check
        Pattern pattern = Pattern.compile("\\b(0[1-9]|1[0-2])\\/(\\d{4})\\b|\\b(Jan|Feb|Mar|Apr|May|Jun|Jul|Aug|Sep|Oct|Nov|Dec)[a-z]*\\s(\\d{4})\\b", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(resumeText);
        Set<String> dateFormatsFound = new HashSet<>();
        while (matcher.find()) {
            if (matcher.group(1) != null) dateFormatsFound.add("MM/YYYY");
            if (matcher.group(3) != null) dateFormatsFound.add("Month YYYY");
        }
        if (dateFormatsFound.size() > 1) {
            warnings.add("Date Format: You use inconsistent date formats (e.g., both 'MM/YYYY' and 'Month YYYY'). Pick one and stick to it.");
        }

        return warnings;
    }
}