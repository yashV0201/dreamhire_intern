package com.dreamhire.candidate_scores.service;

import com.dreamhire.candidate_scores.dto.RequestData;
import com.dreamhire.candidate_scores.dto.TestResumeDto;
import com.dreamhire.candidate_scores.feign.ProfileInterface;
import com.dreamhire.candidate_scores.model.CalibratedTable;
import com.dreamhire.candidate_scores.model.CandidateScores;
import com.dreamhire.candidate_scores.repository.CalibratedTableRespository;
import com.dreamhire.candidate_scores.repository.CandidateScoreRepository;
import com.mongodb.DuplicateKeyException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CandidateScoreServices {

    private final CandidateScoreRepository candidateScoreRepository;

    private final CalibratedTableRespository calibratedTableRespository;

    private final ProfileInterface profileInterface;

    public ResponseEntity<CandidateScores> saveCandidateScores() {
        CandidateScores candidateScores = new CandidateScores();
        candidateScores.setFileName("abcd");
        return ResponseEntity.ok(candidateScoreRepository.save(candidateScores));
    }

    public List<TestResumeDto> getResumeList(String profileName) {
        List<TestResumeDto> testResumeList = profileInterface.getTestResumesFromProfile(profileName).getBody();
        return testResumeList;
    }

    public String getWeightedTable(String profileName) {
        String table = profileInterface.getWeightedTable(profileName).getBody();
        return table;
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
                    response.put("total",Double.parseDouble(columns[2].trim()));
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


    //-------SAVES paresedTable to DB as CandidateScore
    public String saveCandidateScore(String tableResponse, String fileName, String profileName) {
        Map<String, Object> response = parseAssessedTable(tableResponse);

        String name = (String) response.get("name");
        double totalScore = (Double) response.get("total");
        @SuppressWarnings("unchecked")
        Map<String, Integer> scores = (Map<String, Integer>) response.get("scores");

        CandidateScores candidateScore = new CandidateScores();
//        candidateScore.setId((int) Math.floor(Math.random()));
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

    public ResponseEntity<List<CandidateScores>> assessCandidates(String profileName) throws IOException {
        //candidateScoreRepository.deleteAll();
        List<TestResumeDto> testResumeList = this.getResumeList(profileName);
        String weightedTable = this.getWeightedTable(profileName);
        for(TestResumeDto file: testResumeList){
            RequestData requestData = new RequestData();
            requestData.setFileName(file.getFileName());
            requestData.setData(file.getData());
            requestData.setTable(weightedTable);
            requestData.setProfileName(profileName);

            String tableResponse = profileInterface.getResults(requestData).getBody();
            String saveCandidate = this.saveCandidateScore(tableResponse, file.getFileName(), profileName);
            System.out.println(saveCandidate);

            // Wait for 5-10 seconds before the next iteration
            try {
                long delay = (long) (5000 + Math.random() * 5000); // Random delay between 5 to 10 seconds
                Thread.sleep(delay);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("Thread was interrupted", e);
            }
        }

        List<CandidateScores> scoresList = candidateScoreRepository.findByProfileNameOrderByTotalScoreDesc(profileName);

        return ResponseEntity.ok(scoresList);
    }

    public ResponseEntity<List<CandidateScores>> getScoresFromFilename(String profileName) {
        List<CandidateScores> scoresList = candidateScoreRepository.findByProfileNameOrderByTotalScoreDesc(profileName);
        return ResponseEntity.ok(scoresList);
    }

    public ResponseEntity<CalibratedTable> saveCalibratedTable(String profileName, String weightedTable) {
        CalibratedTable calibratedTable = new CalibratedTable();
        calibratedTable.setProfileName(profileName);
        Map<String , Integer> table = new HashMap<>();

        List<List<String>> tableResp = new ArrayList<>();

        // Split the markdown table into lines
        String[] lines = weightedTable.split("\n");

        String dashedLine = new String();

        for (int i = 2; i < lines.length; i++) {
            String line = lines[i];

            // Skip the header separator line
            if (line.contains("---")){
                dashedLine = line;
                continue;
            }

            // Remove leading and trailing pipes and split by pipe
            String[] columns = line.substring(1, line.length() - 1).split("\\|");

            table.put(columns[0].trim(),Integer.parseInt(columns[1].trim().replace("%","").trim()));

        }

        calibratedTable.setTable(table);
        calibratedTableRespository.save(calibratedTable);
        return ResponseEntity.ok(calibratedTable);
    }
}
