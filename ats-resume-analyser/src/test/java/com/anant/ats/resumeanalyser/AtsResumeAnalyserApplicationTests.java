package com.anant.ats.resumeanalyser;
import org.springframework.test.context.ActiveProfiles;
import com.anant.ats.resumeanalyser.service.SuggestionService; // <-- Still need this import
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

@ActiveProfiles("test")
@SpringBootTest
@MockBean(SuggestionService.class) // <-- 1. PUT THE ANNOTATION HERE
class AtsResumeAnalyserApplicationTests {


    @Test
    void contextLoads() {
        // This test will now pass, and the warning will be gone.
    }

}