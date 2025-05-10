package com.smartexpense.invoiceanalyzer.service;

import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.awt.image.BufferedImage;
import java.io.File;

@Service
public class OcrService {

    @Value("${tesseract.path}")
    private String tesseractPath;

    public String extractText(File imageFile) {
        Tesseract tesseract = new Tesseract();
        tesseract.setDatapath(tesseractPath); // klasör yolu
        tesseract.setLanguage("eng");         // dil belirtildi! 🎯

        try {
            return tesseract.doOCR(imageFile);
        } catch (TesseractException e) {
            return "OCR Error: " + e.getMessage();
        }
    }

    public String extractTextFromPdf(MultipartFile file) {
        try (PDDocument document = PDDocument.load(file.getInputStream())) {
            PDFRenderer pdfRenderer = new PDFRenderer(document);
            BufferedImage image = pdfRenderer.renderImageWithDPI(0, 300); // İlk sayfa, 300 DPI

            Tesseract tesseract = new Tesseract();
            tesseract.setDatapath(tesseractPath);
            tesseract.setLanguage("eng");

            return tesseract.doOCR(image);

        } catch (Exception e) {
            return "PDF OCR Error: " + e.getMessage();
        }
    }


}
