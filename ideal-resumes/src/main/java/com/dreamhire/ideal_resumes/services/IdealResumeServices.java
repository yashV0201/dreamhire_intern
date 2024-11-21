package com.dreamhire.ideal_resumes.services;

import com.dreamhire.ideal_resumes.model.IdealResume;
import com.dreamhire.ideal_resumes.repository.IdealResumeRepository;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class IdealResumeServices {

    private final IdealResumeRepository idealResumeRepository;

    public String extractTextFromPDF(MultipartFile file) throws IOException {
        try (InputStream inputStream = file.getInputStream();
             PDDocument document = PDDocument.load(inputStream)) {
            PDFTextStripper pdfStripper = new PDFTextStripper();
            return pdfStripper.getText(document);
        }
    }


    public ResponseEntity<String> uploadIdealResume(String name, List<MultipartFile> fileList) throws IOException {
        List<IdealResume> idealResumeList = new ArrayList<>();
        for(MultipartFile file : fileList){
            IdealResume resume = new IdealResume();
            String fileName = file.getOriginalFilename();
            String data = this.extractTextFromPDF(file);
            resume.setFileName(fileName);
            resume.setData(data);
            resume.setProfileName(name);
            idealResumeList.add(resume);
        }

        idealResumeRepository.saveAll(idealResumeList);

        return new ResponseEntity<>(idealResumeList.toString(), HttpStatus.CREATED);
    }
}
