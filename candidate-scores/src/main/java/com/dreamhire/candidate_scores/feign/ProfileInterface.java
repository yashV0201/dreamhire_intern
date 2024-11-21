package com.dreamhire.candidate_scores.feign;

import com.dreamhire.candidate_scores.dto.CandidateDto;
import com.dreamhire.candidate_scores.dto.RequestData;
import com.dreamhire.candidate_scores.dto.TestResumeDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@FeignClient("AI-OPEN")
public interface ProfileInterface {
    @GetMapping("/{profileName}/getresume")
    public ResponseEntity<List<TestResumeDto>> getTestResumesFromProfile(@PathVariable String profileName);

    @GetMapping("/{profileName}/getCalibratedTable")
    public ResponseEntity<String> getWeightedTable(@PathVariable String profileName);

    @PostMapping("response")
    public ResponseEntity<String> getResults(@RequestBody RequestData requestData) throws IOException;

    @PostMapping("/{profileName}/save")   //saves calibrated table in the DB
    public ResponseEntity<String> saveCalibratedTable(@PathVariable String profileName) throws IOException;

    @PostMapping("/{profileName}/save-updated-table")
    public ResponseEntity<String> saveUpdatedTable(@PathVariable String profileName, @RequestParam String updatedTable);

    @GetMapping("/{profileName}/getDetails/{fileName}")
    public CandidateDto getCandidateDetails(@PathVariable String profileName, @PathVariable String fileName) throws IOException;

    @GetMapping("/{profileName}/display-table/{fileName}/questions")
    public ResponseEntity<String> getQuestionsOnTopics(@PathVariable String profileName, @PathVariable String fileName,@RequestParam String topic) throws IOException;

}
