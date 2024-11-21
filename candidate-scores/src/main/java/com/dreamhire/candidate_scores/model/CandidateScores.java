package com.dreamhire.candidate_scores.model;


import lombok.*;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.MongoId;

import java.util.Map;

@Document(collection = "candidateScores")
@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CandidateScores {

    @MongoId
    private String id;


    @Indexed(unique = true)
    private String fileName;

    private String profileName;

    private Map<String, Integer> scores;
    private double totalScore;

}
