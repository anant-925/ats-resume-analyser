package com.anant.ats.resumeanalyser.controller;
import com.anant.ats.resumeanalyser.model.AnalysisReport;
import com.anant.ats.resumeanalyser.model.User;
import com.anant.ats.resumeanalyser.repository.AnalysisReportRepository;
import com.anant.ats.resumeanalyser.repository.UserRepository;
import java.util.List;
import com.anant.ats.resumeanalyser.service.AnalysisService;
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
import java.security.Principal; 

@Controller 
public class AnalysisController {

    @Autowired
    private TextExtractionService textExtractionService;

    @Autowired
    private AnalysisService analysisService;
    
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AnalysisReportRepository reportRepository;
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
                                @RequestParam("companyType") String companyType,
                                RedirectAttributes redirectAttributes,
                                Principal principal) { 

        // Check if user is logged in
        if (principal == null) {
            redirectAttributes.addFlashAttribute("error", "You must be logged in to perform an analysis.");
            return "redirect:/login";
        }

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

            AnalysisService.AnalysisResult result = analysisService.analyzeAndSave(
                resumeText, 
                jobDescription, 
                companyType, 
                resumeFile, 
                principal.getName() 
            );
            
            redirectAttributes.addFlashAttribute("result", result);
            redirectAttributes.addFlashAttribute("fileName", resumeFile.getOriginalFilename());

        } catch (IOException | TikaException | IllegalArgumentException e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Error processing file: " + e.getMessage());
        }

        return "redirect:/";
    }
    @GetMapping("/history")
public String showHistoryPage(Model model, Principal principal) {
    if (principal == null) {
        return "redirect:/login";
    }

    User user = userRepository.findByUsername(principal.getName()).get();

    List<AnalysisReport> reports = reportRepository.findByUserOrderByAnalysisDateDesc(user);

    model.addAttribute("reports", reports);

    return "history";
}
}