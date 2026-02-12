package com.project.manusaiagent.tools;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class PDFGenerationToolTest {

    @Test
    void generatePDF() {
        PDFGenerationTool tool = new PDFGenerationTool();
        String fileName = "content.pdf";
        String content = "some pdf content";
        String result = tool.generatePDF(fileName, content);
        assertNotNull(result);
    }
}