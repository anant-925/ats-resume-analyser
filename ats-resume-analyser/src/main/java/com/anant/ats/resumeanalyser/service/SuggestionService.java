package com.anant.ats.resumeanalyser.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

@Profile("!test")
@Service
public class SuggestionService {

    @Autowired
    private RestTemplate restTemplate;

    @Value("${gemini.api.key}")
    private String apiKey;

    // This is our working API endpoint
    private static final String GEMINI_API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=";

    /**
     * UPDATED: Now accepts 'companyType'
     */
    public String generateSuggestions(String resumeText, String jdText, Set<String> missingKeywords, String companyType) {
        
        String apiUrl = GEMINI_API_URL + apiKey;
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // 1. Build the new, powerful prompt
        String prompt = buildPrompt(resumeText, jdText, missingKeywords, companyType);

        // 2. Build the JSON request body
        Map<String, Object> textPart = Map.of("text", prompt);
        Map<String, Object> content = Map.of("parts", Collections.singletonList(textPart));
        Map<String, Object> requestBody = Map.of("contents", Collections.singletonList(content));

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        // 3. Call the API
        try {
            String response = restTemplate.postForObject(apiUrl, entity, String.class);
            return parseResponse(response);
        } catch (Exception e) {
            e.printStackTrace();
            return "Error: Could not generate AI suggestions. " + e.getMessage();
        }
    }

    /**
     * Helper method to parse the complex JSON response from Gemini
     */
    private String parseResponse(String jsonResponse) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(jsonResponse);
            
            String text = root.path("candidates")
                              .path(0)
                              .path("content")
                              .path("parts")
                              .path(0)
                              .path("text")
                              .asText();
            
            if (text.isEmpty()) {
                // Handle cases where the API might have blocked the response (safety settings)
                return "Error: Could not extract text from AI response. It may have been blocked for safety reasons. Response: " + jsonResponse;
            }
            return text;

        } catch (Exception e) {
            e.printStackTrace();
            return "Error: Could not parse AI response. " + e.getMessage();
        }
    }

    /**
     * UPDATED: This is our new, much more powerful prompt that
     * uses the 'companyType' and asks for a full analysis.
     */
    private String buildPrompt(String resumeText, String jdText, Set<String> missingKeywords, String companyType) {
        
        String prompt = """
            You are an expert HR recruiter and professional career coach.
            A user is applying for a **%s-Based Company**.
            Analyze their resume against the job description (JD) and provide a detailed review.

            **User's Resume:**
            ---
            %s
            ---

            **Job Description (JD):**
            ---
            %s
            ---

            **My simple analysis found these missing keywords:** %s

            **Analysis Task:**
            Please provide your analysis in the following structured Markdown format:

            ## Job Fit Score
            Give a percentage score (e.g., 75%%) and a one-sentence justification.

            ## Strengths
            * List 2-3 key strengths from the resume that directly align with the JD.

            ## Weaknesses & Gaps
            * List 2-3 critical gaps or weaknesses where the resume does not meet the JD's requirements.

            ## AI-Powered Suggestions
            * Based on the weaknesses, provide 3-5 actionable, full-sentence suggestions
                for how the user can (truthfully) update their resume.
            * **Tailor this advice for a %s-Based company.**
                (e.g., for 'Product', focus on impact and innovation. For 'Service', focus on clients and adaptability.)

            ## Redundancy Check
            * Point out any skills or phrases that are repeated unnecessarily and could be removed.
                If there are no issues, just say "No redundancy issues found."
            """;
        
        // We insert 'companyType' twice into the prompt for context
        return String.format(prompt, companyType, resumeText, jdText, missingKeywords.toString(), companyType);
    }
}