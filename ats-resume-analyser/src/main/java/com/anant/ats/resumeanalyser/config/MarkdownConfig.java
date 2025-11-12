package com.anant.ats.resumeanalyser.config;

import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

// This file creates a simple service to convert Markdown to HTML

@Configuration
public class MarkdownConfig {

    @Bean
    public MarkdownRenderer markdownRenderer() {
        return new MarkdownRenderer();
    }

    @Component("markdownRenderer") // This is the bean name our HTML is looking for
    public static class MarkdownRenderer {
        private final Parser parser = Parser.builder().build();
        private final HtmlRenderer renderer = HtmlRenderer.builder().build();

        public String render(String markdown) {
            if (markdown == null) {
                return "";
            }
            return renderer.render(parser.parse(markdown));
        }
    }
}