package com.anant.ats.resumeanalyser.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
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

    public record AnalysisResult(
        int matchPercentage,
        Set<String> matchingKeywords,
        Set<String> missingKeywords,
        String summary,
        String aiSuggestions 
    ) {}

    /**
     * Cleans and tokenizes text into a set of unique keywords.
     */
    private Set<String> extractKeywords(String text) {
        if (text == null || text.isBlank()) {
            return Set.of();
        }

        return Arrays.stream(text.toLowerCase()
                        .replaceAll("[^a-zA-Z0-9\\s]", "")
                        .split("\\s+"))
                        .filter(word -> word.length() > 1 && !STOP_WORDS.contains(word))
                        .collect(Collectors.toSet());
    }
    public AnalysisResult analyze(String resumeText, String jdText) {
        
        Set<String> resumeKeywords = extractKeywords(resumeText);
        Set<String> jdKeywords = extractKeywords(jdText);

        if (jdKeywords.isEmpty()) {
            return new AnalysisResult(0, Set.of(), Set.of(), "Job Description is empty or contains no keywords.", "N/A");
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

        // ---Call the AI ---
        // We call our new service to get actionable feedback
        String aiFeedback = "Loading AI suggestions...";
        try {
            aiFeedback = suggestionService.generateSuggestions(resumeText, jdText, missingKeywords);
        } catch (Exception e) {
            e.printStackTrace();
            aiFeedback = "Error generating AI suggestions. Please check the server logs.";
        }

        // Return the complete result, including the AI feedback
        return new AnalysisResult(matchPercentage, matchingKeywords, missingKeywords, summary, aiFeedback);
    }
}