package com.dreamhire.pre_filtering_service.services;

import com.dreamhire.pre_filtering_service.dtos.RequestDto;
import com.dreamhire.pre_filtering_service.feign.OpenAIInterface;
import com.dreamhire.pre_filtering_service.models.CsvRecord;
import com.opencsv.CSVReader;
import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;
import lombok.RequiredArgsConstructor;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CsvService {

    private final OpenAIInterface openAIInterface;

    public List<CsvRecord> parseCsv(MultipartFile file) throws Exception {
        try (CSVReader reader = new CSVReader(new InputStreamReader(file.getInputStream()))) {
            CsvToBean<CsvRecord> csvToBean = new CsvToBeanBuilder<CsvRecord>(reader)
                    .withType(CsvRecord.class)
                    .withIgnoreLeadingWhiteSpace(true)
                    .build();
            return csvToBean.parse();
        }
    }

    public String evaluateCv(MultipartFile csv, MultipartFile cv) throws Exception {
        List<CsvRecord> records = this.parseCsv(csv);
        String prompt = generatePrompt(records);
        String text = extractTextFromPDF(cv);
        RequestDto requestDto = new RequestDto();
        requestDto.setText(text);
        requestDto.setPrompt(prompt);
        // Implement your logic to call OpenAI API using the prompt and CV
        // Example code:
        // String response = openAiService.evaluate(prompt, cv);
        // return response;
        return openAIInterface.getResponseFromOpenAi(requestDto); // Replace with actual response
    }

    public String generatePrompt(List<CsvRecord> records){
        StringBuilder promptBuilder = new StringBuilder("Evaluate the resume based on the following criteria and give response as \"passed\" or \"not passed\" and if not passed then why(give the reason according to each of the factors analytically).\n" +
                "the response should be in JSON format like { verdict:\"not passed\", reason:\"xyz criteria not matching\"}" +
                " :\n");
        for(CsvRecord record : records){
            promptBuilder.append("Factor: ").append(record.getFactors())
                    .append(" - Options: ").append(record.getOptions()).append("\n")
                    .append("Prompt: ").append(record.getPrompt()).append("\n");
        }

        //now we need to do that for a list of resumes
        //to do that we need to

        return promptBuilder.toString();
    }

    public String extractTextFromPDF(MultipartFile file) throws IOException {
        try (InputStream inputStream = file.getInputStream();
             PDDocument document = PDDocument.load(inputStream)) {
            PDFTextStripper pdfStripper = new PDFTextStripper();
            String extractedText = pdfStripper.getText(document);

            // Sanitize the extracted text
            String sanitizedText = sanitizeText(extractedText);
            return sanitizedText;
        }
    }

    private String sanitizeText(String text) {
        // Remove null bytes
        text = text.replace("\u0000", "");

        // Ensure the text is valid UTF-8
        byte[] bytes = text.getBytes(StandardCharsets.UTF_8);
        return new String(bytes, StandardCharsets.UTF_8);
    }
}
