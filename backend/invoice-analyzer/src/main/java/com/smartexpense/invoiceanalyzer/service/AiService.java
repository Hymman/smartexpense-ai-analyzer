package com.smartexpense.invoiceanalyzer.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
public class AiService {

    @Value("${openrouter.api.key}")
    private String apiKey;

    @Value("${openrouter.api.url}")
    private String apiUrl;

    @Value("${openrouter.model}")
    private String model;

    public String analyzeInvoice(String ocrText) {
        RestTemplate restTemplate = new RestTemplate();

        String prompt = """
                Aşağıdaki metin bir faturadan OCR ile alınmıştır. Lütfen iki ayrı bölümde cevap ver:
                
                      **1. Doğal Yorumlama:**
                      - Tutar nedir?
                      - Harcama dikkat çekici mi?
                      - Gider tipi nedir?
                      - Genel bir yorum yaz.
                      
                      Lütfen currency alanını ISO 4217 kodu olarak ver (örn: USD, EUR, TRY). Sembol verme.
                
                
                      **2. Yapılandırılmış JSON Bilgileri:**
                      Lütfen aşağıdaki bilgileri mümkünse çıkar ve JSON formatında ayrı olarak ver:
                      ```json
                      {
                        "invoiceNumber": "",
                        "invoiceDate": "",
                        "dueDate": "",
                        "totalAmount": "",
                        "currency": "",
                        "buyer": "",
                        "seller": ""
                      }
                        Lütfen currency alanını ISO 4217 kodu olarak ver (örn: USD, EUR, TRY). Sembol verme.
            """ + ocrText;

        Map<String, Object> body = new HashMap<>();
        body.put("model", model);
        body.put("messages", List.of(
                Map.of("role", "user", "content", prompt)
        ));
        body.put("temperature", 0.7);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + apiKey);
        headers.set("HTTP-Referer", "https://yourdomain.com"); // zorunlu
        headers.set("X-Title", "Invoice Analyzer");

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(apiUrl, entity, Map.class);
            Map<String, Object> resBody = response.getBody();

            List<Map<String, Object>> choices = (List<Map<String, Object>>) resBody.get("choices");
            Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");

            return (String) message.get("content");
        } catch (Exception e) {
            return "AI Error: " + e.getMessage();
        }
    }
}
