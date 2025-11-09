package com.anant.ats.resumeanalyser.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
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

    private static final String GEMINI_API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-pro-latest:generateContent?key=";

    public String generateSuggestions(String resumeText, String jdText, Set<String> missingKeywords) {
        
        String apiUrl = GEMINI_API_URL + apiKey;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> textPart = Map.of("text", buildPrompt(resumeText, jdText, missingKeywords));
        Map<String, Object> content = Map.of("parts", Collections.singletonList(textPart));
        Map<String, Object> requestBody = Map.of("contents", Collections.singletonList(content));

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        try {
            String response = restTemplate.postForObject(apiUrl, entity, String.class);
            return parseResponse(response);

        } catch (HttpClientErrorException e) {
            e.printStackTrace();
            return "Error: Could not generate AI suggestions. " + e.getStatusText() + " " + e.getResponseBodyAsString();
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
                return "Error: Could not extract text from AI response. It may have been blocked for safety reasons. Response: " + jsonResponse;
            }
            return text;

        } catch (Exception e) {
            e.printStackTrace();
            return "Error: Could not parse AI response. " + e.getMessage();
        }
    }

    private String buildPrompt(String resumeText, String jdText, Set<String> missingKeywords) {
        String prompt = """
            You are a professional career coach and expert resume writer.
            A user is applying for a job and needs help updating their resume.

            Here is the user's resume:
            --- RESUME START ---
            %s
            --- RESUME END ---

            Here is the job description they are applying for:
            --- JOB START ---
            %s
            --- JOB END ---

            My simple analysis shows the user is missing these keywords: %s

            Your task is to provide 3-5 actionable, full-sentence suggestions.
            These suggestions should help the user thoughtfully incorporate the missing keywords
            into their resume, based on the experience they have already listed.
            Do not just tell them to "add the keyword."
            
            Example good suggestion: "I see the job requires 'Spring Boot' and your resume lists 'Java'. You could update one of your project descriptions to be: 'Developed a RESTful API for [Project] using Java and the Spring Boot framework.'"
            Example bad suggestion: "Add 'Spring Boot' to your resume."

            Provide the suggestions in a clear, easy-to-read format.
            """;
        
        return String.format(prompt, resumeText, jdText, missingKeywords.toString());
    }

    
    
}
