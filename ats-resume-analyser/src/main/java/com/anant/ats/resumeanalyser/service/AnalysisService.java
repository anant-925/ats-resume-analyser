package com.anant.ats.resumeanalyser.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile; // <-- ADD THIS IMPORT

import java.util.ArrayList; // <-- ADD THIS IMPORT
import java.util.Arrays;
import java.util.HashSet;
import java.util.List; // <-- ADD THIS IMPORT
import java.util.Set;
import java.util.regex.Matcher; // <-- ADD THIS IMPORT
import java.util.regex.Pattern; // <-- ADD THIS IMPORT
import java.util.stream.Collectors;

@Service
public class AnalysisService {

    @Autowired
    private SuggestionService suggestionService;

    private static final Set<String> STOP_WORDS = Set.of(
        "a", "an", "and", "are", "as", "at", "be", "by", "for", "from",
        "has", "he", "in", "is", "it", "its", "of", "on", "that", "the",
        "to", "was", "were", "will", "with", "you", "your", "job", "description",
        "requirements", "experience", "skills", "responsibilities"
    );

    /**
     * UPDATED: Now includes a List to hold our new compliance warnings.
     */
    public record AnalysisResult(
        int matchPercentage,
        Set<String> matchingKeywords,
        Set<String> missingKeywords,
        String summary,
        String aiSuggestions,
        List<String> complianceWarnings // <-- NEW FIELD
    ) {}

    /**
     * Cleans and tokenizes text into a set of unique keywords.
     */
    private Set<String> extractKeywords(String text) {
        if (text == null || text.isBlank()) return Set.of();
        return Arrays.stream(text.toLowerCase()
                        .replaceAll("[^a-zA-Z0-9\\s]", "")
                        .split("\\s+"))
                        .filter(word -> word.length() > 1 && !STOP_WORDS.contains(word))
                        .collect(Collectors.toSet());
    }

    /**
     * NEW: A method to run all our code-based compliance checks.
     */
    private List<String> runComplianceChecks(String resumeText, MultipartFile file) {
        List<String> warnings = new ArrayList<>();

        // 1. File Type Check (from your list)
        String filename = file.getOriginalFilename();
        if (filename != null && !filename.endsWith(".pdf") && !filename.endsWith(".docx")) {
            warnings.add("Warning: You uploaded a '" + filename.substring(filename.lastIndexOf(".") + 1) + "' file. For best results, always use .pdf or .docx.");
        }

        // 2. Consistent Dates Check (from your list)
        // This regex finds dates like "MM/YYYY" or "Month YYYY"
        Pattern pattern = Pattern.compile(
            "\\b(0[1-9]|1[0-2])\\/(\\d{4})\\b|\\b(January|February|March|April|May|June|July|August|September|October|November|December)\\s(\\d{4})\\b",
            Pattern.CASE_INSENSITIVE
        );
        Matcher matcher = pattern.matcher(resumeText);

        Set<String> dateFormatsFound = new HashSet<>();
        while (matcher.find()) {
            if (matcher.group(1) != null) { // Found "MM/YYYY"
                dateFormatsFound.add("MM/YYYY");
            }
            if (matcher.group(3) != null) { // Found "Month YYYY"
                dateFormatsFound.add("Month YYYY");
            }
        }

        if (dateFormatsFound.size() > 1) {
            warnings.add("Warning: You use inconsistent date formats (e.g., both 'MM/YYYY' and 'Month YYYY'). Pick one and stick to it.");
        }
        if (dateFormatsFound.isEmpty()) {
            warnings.add("Note: No standard date formats (like 'MM/YYYY' or 'Month YYYY') were found. Make sure your dates are clear.");
        }

        // 3. (Future checks like headers, columns, etc. would be added here)
        
        return warnings;
    }


    /**
     * UPDATED: Now accepts the 'file' to run compliance checks.
     */
    public AnalysisResult analyze(String resumeText, String jdText, String companyType, MultipartFile file) {
        
        Set<String> resumeKeywords = extractKeywords(resumeText);
        Set<String> jdKeywords = extractKeywords(jdText);

        if (jdKeywords.isEmpty()) {
            return new AnalysisResult(0, Set.of(), Set.of(), "Job Description is empty or contains no keywords.", "N/A", List.of());
        }

        // --- Keyword analysis ---
        Set<String> matchingKeywords = new HashSet<>(jdKeywords);
        matchingKeywords.retainAll(resumeKeywords);

        Set<String> missingKeywords = new HashSet<>(jdKeywords);
        missingKeywords.removeAll(resumeKeywords);

        double score = ((double) matchingKeywords.size() / jdKeywords.size()) * 100;
        int matchPercentage = (int) Math.round(score);

        String summary = String.format("Found %d out of %d job description keywords.", 
                                       matchingKeywords.size(), jdKeywords.size());

        // --- NEW: Run Compliance Checks ---
        List<String> complianceWarnings = runComplianceChecks(resumeText, file);

        // --- AI Suggestions ---
        String aiFeedback = suggestionService.generateSuggestions(resumeText, jdText, missingKeywords, companyType);

        // --- Return the complete result ---
        return new AnalysisResult(matchPercentage, matchingKeywords, missingKeywords, summary, aiFeedback, complianceWarnings);
    }
}