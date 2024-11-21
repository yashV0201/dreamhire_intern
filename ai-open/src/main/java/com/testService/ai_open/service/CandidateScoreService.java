package com.testService.ai_open.service;

import com.mongodb.DuplicateKeyException;
import com.testService.ai_open.model.mongodb.CandidateScores;
import com.testService.ai_open.model.postgres.JobProfile;
import com.testService.ai_open.model.postgres.TestResume;
import com.testService.ai_open.repository.mongodb.CandidateScoreRepository;
import com.testService.ai_open.repository.postgres.ProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;

@Service
@RequiredArgsConstructor
public class CandidateScoreService {

    @Autowired
    private CandidateScoreRepository candidateScoreRepository;


    private static  ProfileRepository profileRepository;

    @Autowired
    private static OpenAiService openAiService;

    public ResponseEntity<List<CandidateScores>> assessCandidates(String profileName) throws IOException {
        Optional<JobProfile> profileOptional= profileRepository.findById(profileName);
        if(profileOptional.isPresent()){
            JobProfile profile = profileOptional.get();
            if(profile.getWeightedTable() != null){
                String prompt = profile.getWeightedTable()+"\n"+ openAiService.ratingPrompt;

                for(TestResume file: profile.getTestProfiles()){
                    String resume = "File Name: "+file.getFileName()+"\n"+"Content: "+file.getTextData();
                    String weightedTab = openAiService.processTextWithOpenAI(prompt,resume);
                    String results = this.saveCandidateScore(weightedTab,file.getFileName(),profileName);
                }

                List<CandidateScores> candidateScores = this.getCandidatesByProfileNameSortedByTotalScore(profileName);

                profile.setScoreTable(candidateScores.toString());

                profileRepository.save(profile);

                return ResponseEntity.ok(candidateScores);

            }else{
                return ResponseEntity.notFound().build();
            }
        }else{
            return ResponseEntity.notFound().build();
        }

    }

    public List<CandidateScores> getCandidatesByProfileNameSortedByTotalScore(String profileName) {
        return candidateScoreRepository.findByProfileNameOrderByTotalScoreDesc(profileName);
    }

    //converts individual score tables into mapped values  (to be saves to MongoDB later)
    public Map<String, Object> parseAssessedTable(String tableResponse) {
        List<List<String>> table = new ArrayList<>();
        Map<String,Object> response = new HashMap<>();
        Map<String,Integer> score = new HashMap<>();
        // Split the markdown table into lines
        String[] lines = tableResponse.split("\n");

        // Iterate over the lines starting from the second line to skip the header separator
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];

            // Skip the header separator line
            if (line.contains("---")) continue;

            // Remove leading and trailing pipes and split by pipe
            String[] columns = line.substring(1, line.length() - 1).split("\\|");
            if(i==0 || i==lines.length-1){
                if(i==0){
                    response.put("name",columns[2].trim());
                }else{
                    response.put("total",columns[2].trim());
                }
                continue;
            }

            // Trim whitespace and add to row
            List<String> row = new ArrayList<>();
            score.put(columns[0].trim()+" ("+columns[1].trim()+")", Integer.parseInt(columns[2].trim()));
//            for (String column : columns) {
//                row.add(column.trim());
//            }

            // Add row to the table
            table.add(row);
        }
        response.put("scores", score);

        return response;
    }

    public String saveCandidateScore(String tableResponse, String fileName, String profileName) {
        Map<String, Object> response = parseAssessedTable(tableResponse);

        String name = (String) response.get("name");
        double totalScore = (Double) response.get("total");
        @SuppressWarnings("unchecked")
        Map<String, Integer> scores = (Map<String, Integer>) response.get("scores");

        CandidateScores candidateScore = new CandidateScores();
        candidateScore.setFileName(fileName);
        candidateScore.setTotalScore(totalScore);
        candidateScore.setScores(scores);
        candidateScore.setProfileName(profileName);

        try {
            CandidateScores existingCandidate = candidateScoreRepository.findByfileName(fileName);
            if (existingCandidate != null) {
                // Update existing document
                existingCandidate.setTotalScore(totalScore);
                existingCandidate.setScores(scores);
                candidateScoreRepository.save(existingCandidate);
                return "Candidate score updated successfully.";
            } else {
                // Save new document
                candidateScoreRepository.save(candidateScore);
                return "Candidate score saved successfully.";
            }
        } catch (DuplicateKeyException e) {
            return "Duplicate candidate entry.";
        }
    }



}
