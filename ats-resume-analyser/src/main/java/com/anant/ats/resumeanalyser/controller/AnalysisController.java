package com.anant.ats.resumeanalyser.controller;

import com.anant.ats.resumeanalyser.service.AnalysisService;
// import com.anant.ats.resumeanalyser.service.SuggestionService;
import com.anant.ats.resumeanalyser.service.TextExtractionService;
import org.apache.tika.exception.TikaException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;

@Controller 
public class AnalysisController {

    @Autowired
    private TextExtractionService textExtractionService;

    @Autowired
    private AnalysisService analysisService;
    
    @GetMapping("/")
    public String homePage(Model model) {

        if (!model.containsAttribute("result")) {
            model.addAttribute("result", null);
        }
        return "index"; 
    }

    @PostMapping("/analyze")
    public String analyzeResume(@RequestParam("resumeFile") MultipartFile resumeFile,
                                @RequestParam("jobDescription") String jobDescription,
                                RedirectAttributes redirectAttributes) {

        // 1. Basic Validation
        if (resumeFile.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Please upload a resume file.");
            return "redirect:/";
        }
        if (jobDescription.isBlank()) {
            redirectAttributes.addFlashAttribute("error", "Please paste the job description.");
            return "redirect:/";
        }

        try {
            String resumeText = textExtractionService.extractTextFromFile(resumeFile);

            AnalysisService.AnalysisResult result = analysisService.analyze(resumeText, jobDescription);
            redirectAttributes.addFlashAttribute("result", result);
            redirectAttributes.addFlashAttribute("fileName", resumeFile.getOriginalFilename());

        } catch (IOException | TikaException | IllegalArgumentException e) {
            e.printStackTrace(); // Log the error
            redirectAttributes.addFlashAttribute("error", "Error processing file: " + e.getMessage());
        }

        return "redirect:/";
    }
   
}