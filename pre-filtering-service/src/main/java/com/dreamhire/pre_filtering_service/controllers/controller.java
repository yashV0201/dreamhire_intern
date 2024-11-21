package com.dreamhire.pre_filtering_service.controllers;

import com.dreamhire.pre_filtering_service.models.CsvRecord;
import com.dreamhire.pre_filtering_service.services.CsvService;
import lombok.extern.java.Log;
import lombok.extern.log4j.Log4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/csv")
public class controller {
    private static final Logger log = LoggerFactory.getLogger(controller.class);
    @Autowired
    private CsvService csvService;

    @PostMapping("/upload")
    public String handleCsvUpload(@RequestParam("file") MultipartFile file ,@RequestParam int i ) {
        try {
            List<CsvRecord> records = csvService.parseCsv(file);
            for (CsvRecord record : records) {
                String response = record.getOptions();                System.out.println(response);
                // Handle the response as needed
            }
            return "CSV processed successfully : "+records.get(i).getFactors()+" "+ records.get(i).getOptions();
        } catch (Exception e) {
            e.printStackTrace();
            return "Failed to process CSV: " + e.getMessage();
        }
    }

    @PostMapping("evaluate")
    public ResponseEntity<String> evaluateCV(@RequestParam("file") MultipartFile csv, @RequestParam("cv") MultipartFile cv ) throws Exception {
        return ResponseEntity.ok(csvService.evaluateCv(csv, cv));
    }
}
