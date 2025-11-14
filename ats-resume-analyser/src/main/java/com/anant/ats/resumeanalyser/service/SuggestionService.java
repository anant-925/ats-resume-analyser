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
@Profile("!test")
@Service
public class SuggestionService {

    @Autowired
    private RestTemplate restTemplate;

    @Value("${gemini.api.key}")
    private String apiKey;

private static final String GEMINI_API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-pro-latest:generateContent?key=";
   
    public String generateSuggestions(String resumeText, String jdText, String companyType) {
        
        String apiUrl = GEMINI_API_URL + apiKey;
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // 1. Build the prompt
        String prompt = buildMasterPrompt(resumeText, jdText, companyType);

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
                return "Error: Could not extract text from AI response. " + jsonResponse;
            }
            return text;
        } catch (Exception e) {
            e.printStackTrace();
            return "Error: Could not parse AI response. " + e.getMessage();
        }
    }
    
    private String buildMasterPrompt(String resumeText, String jdText, String companyType) {
        
        String prompt = """
            You are an expert HR recruiter and professional career coach.
            A user is applying for a **%s-Based Company**.
            Analyze their resume against the job description (JD) and provide a detailed review.
            The analysis MUST be in structured Markdown format.

            **User's Resume:**
            ---
            %s
            ---

            **Job Description (JD):**
            ---
            %s
            ---

            **Analysis Task:**
            Please provide your analysis in the following structured Markdown format:

            ## Job Fit Score
            Give a percentage score (e.g., 75%%) and a one-sentence justification.

            ## Resume Scorecard
            Provide scores out of 100 for the following, with a brief justification for each.
            **IMPORTANT:** The score MUST be in the format (Score/100).
            * **Brevity:** (0-100 score) Is the resume concise? Penalize for long paragraphs.
            * **Impact:** (0-100 score) Does it use quantifiable achievements and strong action verbs?
            * **Technical Skills:** (0-100 score) How relevant are the resume's skills to the JD?
            * **Style:** (0-100 score) Is the resume professional, clear, and easy to read?

            ## Strengths
            * List 2-3 key strengths from the resume that directly align with the JD.

            ## Weaknesses & Gaps
            * List 2-3 critical gaps or weaknesses where the resume does not meet the JD's requirements.

            ## Keyword Optimization
            * **Matching Keywords:** List the top 5-10 keywords found in both the resume and JD.
            * **Missing Keywords:** List the top 5-10 keywords from the JD that are *missing* from the resume.

            ## Action Verbs & Metrics
            * **Action Verbs:** Check if bullet points start with strong action verbs (e.g., 'Managed,' 'Developed'). List any weak examples found (e.g., 'Responsible for...').
            * **Quantifiable Achievements:** Check if the resume uses metrics (e.g., '...by 15%%', '...over 100 users'). Give a specific example of where they could add a metric.
            
            ## Redundancy Check
            * Point out any skills or phrases that are repeated unnecessarily.

            ## AI-Powered Suggestions
            * Based on the weaknesses, provide 2-3 actionable, full-sentence suggestions
              for how the user can (truthfully) update their resume.
            * **Tailor this advice for a %s-Based company.**
            """;
        
        return String.format(prompt, companyType, resumeText, jdText, companyType);
    }
}