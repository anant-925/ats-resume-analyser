package com.anant.ats.resumeanalyser.service;

import org.apache.tika.Tika;
import org.apache.tika.exception.TikaException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;

/**
 * A service dedicated to extracting plain text content from various
 * file types (PDF, DOCX, etc.) using Apache Tika.
 */
@Service
public class TextExtractionService {

    /**
     * Extracts all text content from an uploaded file.
     *
     * @param file The resume file uploaded by the user.
     * @return A string containing all extracted text.
     * @throws IOException   If there's an error reading the file's input stream.
     * @throws TikaException If Tika fails to parse the document.
     */
    public String extractTextFromFile(MultipartFile file) throws IOException, TikaException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Cannot extract text from an empty or null file.");
        }

        // The Tika facade is thread-safe and can be reused.
        Tika tika = new Tika();

        // We use a try-with-resources block to ensure the InputStream is automatically closed.
        try (InputStream inputStream = file.getInputStream()) {
            
            // Tika's parseToString method automatically detects the file type
            // (PDF, DOCX, DOC, TXT, etc.) and returns the text content.
            String text = tika.parseToString(inputStream);
            return text;
            
        } catch (IOException | TikaException e) {
            // Log the error in a real application
            System.err.println("Error parsing file: " + e.getMessage());
            throw e; // Re-throw the exception to be handled by the controller
        }
    }
}