package com.dreamhire.candidate_scores.controller;

import com.dreamhire.candidate_scores.dto.CalibratedTableDto;
import com.dreamhire.candidate_scores.dto.QuestionsRequest;
import com.dreamhire.candidate_scores.dto.TestResumeDto;
import com.dreamhire.candidate_scores.dto.UpdatedTableResponse;
import com.dreamhire.candidate_scores.model.CalibratedTable;
import com.dreamhire.candidate_scores.model.CandidateDetails;
import com.dreamhire.candidate_scores.model.CandidateScores;
import com.dreamhire.candidate_scores.service.CalibrationTableServices;
import com.dreamhire.candidate_scores.service.CandidateDetailService;
import com.dreamhire.candidate_scores.service.CandidateScoreServices;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("{profileName}")
@RequiredArgsConstructor
public class Controller {

    private final CandidateScoreServices candidateScoreServices;

    private final CalibrationTableServices calibrationTableServices;

    private final CandidateDetailService candidateDetailService;

//    @PostMapping()
//    public ResponseEntity<CandidateScores> saveScores(){
//        return candidateScoreServices.saveCandidateScores();
//    }

    @GetMapping("candidates/assess")
    public ResponseEntity<List<CandidateScores>> assessCandidates(@PathVariable String profileName) throws IOException {
        return candidateScoreServices.assessCandidates(profileName);

    }

    @GetMapping("candidates/list")
    public ResponseEntity<List<TestResumeDto>> listCandidates(@PathVariable String profileName){
        List<TestResumeDto> testResumeList = candidateScoreServices.getResumeList(profileName);
        return ResponseEntity.ok(testResumeList);
    }

    @GetMapping("table")
    public ResponseEntity<CalibratedTable> saveTable(@PathVariable String profileName){
        String weightedTable = candidateScoreServices.getWeightedTable(profileName);
//        return candidateScoreServices.saveCalibratedTable(profileName, weightedTable);
        return calibrationTableServices.getTable(profileName);
    }

    @GetMapping("candidates/scores")
    public ResponseEntity<List<CandidateScores>> getScores(@PathVariable String profileName){
        return candidateScoreServices.getScoresFromFilename(profileName);
    }

    @PostMapping("calibrated-table")
    public String saveCalibratedTable(@PathVariable String profileName ,@RequestParam String weightedTable){
        return calibrationTableServices.parseAndSaveTable(profileName, weightedTable);
    }

    @PostMapping("calibrated-table/update")
    public String updateCalibratedTable(@PathVariable String profileName, @RequestBody CalibratedTableDto calibratedTableDto){
        return calibrationTableServices.updateCalibrationTable(profileName, calibratedTableDto);
    }

    @GetMapping("candidates/show-assessment")
    public ResponseEntity<List<CandidateScores>> getCandidateScores(@PathVariable String profileName){
        return candidateScoreServices.getScoresFromFilename(profileName);
    }

    @GetMapping("candidates/show-details")
    public ResponseEntity<CandidateDetails> getCandidateDetails(@PathVariable String profileName, @RequestParam String id) throws IOException {
        return candidateDetailService.getDetailsById(profileName, id);
    }

    @GetMapping("candidates/get-questions")
    public ResponseEntity<Map<String,Object>> getQuestionsOnTopics(@PathVariable String profileName, @RequestBody QuestionsRequest questionsRequest) throws IOException {

        return candidateDetailService.getQuestionsOnTopics(profileName, questionsRequest.getId(), questionsRequest.getTopics());
    }

}
