package com.dreamhire.candidate_scores.service;

import com.dreamhire.candidate_scores.dto.CandidateDto;
import com.dreamhire.candidate_scores.feign.ProfileInterface;
import com.dreamhire.candidate_scores.model.CandidateDetails;
import com.dreamhire.candidate_scores.model.CandidateScores;
import com.dreamhire.candidate_scores.repository.CandidateDetailRepository;
import com.dreamhire.candidate_scores.repository.CandidateScoreRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Log
public class CandidateDetailService {

    private final CandidateScoreRepository candidateScoreRepository;

    private final CandidateDetailRepository candidateDetailRepository;

    private final ProfileInterface profileInterface;

    public ResponseEntity<CandidateDetails> getDetailsById(String profileName, String id) throws IOException {
        if(candidateDetailRepository.findById(id).isPresent()){
            return ResponseEntity.ok(candidateDetailRepository.findById(id).get());
        };
        CandidateScores scores = candidateScoreRepository.findByid(id);
        CandidateDto details = profileInterface.getCandidateDetails(profileName, scores.getFileName());
        CandidateDetails candidateDetails = new CandidateDetails();
        candidateDetails.setName(details.getName());
        log.info(details.getEmail().toString());
        candidateDetails.setEmailId(details.getEmail());
        candidateDetails.setPhoneNumber(details.getPhone());
        candidateDetails.setCandidateScores(scores);
        candidateDetails.setProfileName(profileName);
        candidateDetails.setId(id);
        candidateDetailRepository.save(candidateDetails);
        return ResponseEntity.ok(candidateDetails);
    }

    public ResponseEntity<Map<String, Object>> getQuestionsOnTopics(String profileName, String id, List<String> topics) throws IOException {
        Map<String, Object> questions = new HashMap<>();
        Optional<CandidateDetails> candidateOptional = candidateDetailRepository.findById(id);
        String fileName  = candidateOptional.get().getCandidateScores().getFileName();
        for(String topic : topics){
            String response = profileInterface.getQuestionsOnTopics(profileName, fileName, topic).getBody();
            questions.put(topic,response);
        }

        CandidateDetails candidateDetails = candidateOptional.get();
        candidateDetails.setQuestions(questions);
        log.info(candidateDetails.getId());
        candidateDetailRepository.save(candidateDetails);


        return ResponseEntity.ok(questions);
    }
}
