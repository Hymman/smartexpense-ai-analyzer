package com.smartexpense.invoiceanalyzer.controller;

import com.smartexpense.invoiceanalyzer.service.AiService;
import com.smartexpense.invoiceanalyzer.service.OcrService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.Map;
@CrossOrigin(origins = "http://localhost:3000")
@RestController
@RequestMapping("/api")
public class InvoiceController {

    @Autowired
    private OcrService ocrService;

    @GetMapping("/hello")
    public String helloSmartExpense() {
        return "Hello SmartExpense AI!";
    }

    @PostMapping("/analyze")
    public Map<String, String> analyzeInvoice(@RequestParam("file") MultipartFile file) throws IOException {
        String text;

        if (file.getOriginalFilename().trim().endsWith(".pdf")) {
            text = ocrService.extractTextFromPdf(file);
        } else {
            File convFile = new File(System.getProperty("java.io.tmpdir") + "/" + file.getOriginalFilename());
            file.transferTo(convFile);
            text = ocrService.extractText(convFile);
        }

        String aiComment = aiService.analyzeInvoice(text);

        return Map.of(
                "ocrText", text,
                "aiAnalysis", aiComment
        );
    }



    @Autowired
    private AiService aiService;

}
