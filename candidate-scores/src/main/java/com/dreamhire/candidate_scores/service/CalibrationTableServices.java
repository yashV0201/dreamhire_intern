package com.dreamhire.candidate_scores.service;

import com.dreamhire.candidate_scores.dto.CalibratedTableDto;
import com.dreamhire.candidate_scores.feign.ProfileInterface;
import com.dreamhire.candidate_scores.model.CalibratedTable;
import com.dreamhire.candidate_scores.repository.CalibratedTableRespository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class CalibrationTableServices {
    private final CalibratedTableRespository calibratedTableRespository;

    private final CandidateScoreServices candidateScoreServices;

    private final ProfileInterface profileInterface;

    public String parseAndSaveTable(String profileName, String weightedTable) {
        Map<String , Integer> table = new HashMap<>();
        String[] lines = weightedTable.split("\n");

        for (int i = 1; i < lines.length-1; i++) {
            String line = lines[i];

            // Skip the header separator line
            if (line.contains("---")) continue;


            // Remove leading and trailing pipes and split by pipe
            String[] columns = line.substring(1, line.length() - 1).split("\\|");

            table.put(columns[0].trim(),Integer.parseInt(columns[1].trim().replace("%", "").trim()));

        }

        CalibratedTable calibratedTable = new CalibratedTable();
        calibratedTable.setProfileName(profileName);
        calibratedTable.setTable(table);


        calibratedTableRespository.deleteByProfileName(profileName);
        calibratedTableRespository.save(calibratedTable);

        return calibratedTable.toString();

    }

    public String updateCalibrationTable(String profileName, CalibratedTableDto calibratedTableDto) {
        calibratedTableRespository.deleteByProfileName(profileName);
        CalibratedTable calibratedTable = new CalibratedTable();
        calibratedTable.setProfileName(profileName);
        calibratedTable.setTable(calibratedTableDto.getTable());

        calibratedTableRespository.save(calibratedTable);

        //old table
        String table = candidateScoreServices.getWeightedTable(profileName);

        //updatedTable
        String updatedTable = updateAndManageTable(table,calibratedTableDto.getTable());

        //save table to the profile services database
        ResponseEntity<String> tableRes = profileInterface.saveUpdatedTable(profileName, updatedTable);

        return calibratedTable.toString()+"\n"+ tableRes.getBody();
    }

    public String updateAndManageTable(String weightedTable, Map<String, Integer> weightageMap) {
        StringBuilder updatedTable = new StringBuilder();
        String[] lines = weightedTable.split("\n");
        Set<String> existingParameters = new HashSet<>();
        double totalWeight = 0;

        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];

            if (i == 0 || i == 1 || lines[i].contains("---")) {
                updatedTable.append(lines[i]).append("\n");
                continue;
            }

            // Skip any existing Total rows
            if (line.contains(" Total ")) {
                continue;
            }

            // Remove leading and trailing pipes and split by pipe
            String[] columns = line.substring(1, line.length() - 1).split("\\|");
            String parameter = columns[0].trim();

            // Skip parameters not in the weightageMap
            if (!weightageMap.containsKey(parameter)) {
                continue;
            }

            existingParameters.add(parameter);
            columns[1] = " " + weightageMap.get(parameter) + "%";

            // Calculate totals for weights
            double weight = Double.parseDouble(columns[1].trim().replace("%", ""));
            totalWeight += weight;

            String updatedLine = "|" + String.join("|", columns) + "|";
            updatedTable.append(updatedLine).append("\n");
        }

        // Add new parameters from the weightageMap that are not already in the table
        for (Map.Entry<String, Integer> entry : weightageMap.entrySet()) {
            if (!existingParameters.contains(entry.getKey())) {
                String newParameter = entry.getKey();
                int weight = entry.getValue();
                int score = 0; // Default score can be set to 0 or any desired value
                updatedTable.append("| " + newParameter + " | " + weight + "% | " + score + " |\n");

                // Calculate totals for new parameters
                totalWeight += weight;
            }
        }

        // Append the total row at the end
        updatedTable.append("| Total | ").append(String.format("%.1f", totalWeight)).append("% | |\n");

        return updatedTable.toString();
    }

    public ResponseEntity<CalibratedTable> getTable(String profileName) {
        return ResponseEntity.ok(calibratedTableRespository.findByProfileName(profileName));
    }
}
