package com.testService.ai_open.model.mongodb;

import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.*;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Map;

@Document(collection = "candidate_scores")
@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CandidateScores {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Indexed(unique = true)
    private String fileName;

    private String profileName;

    private Map<String, Integer> scores;
    private double totalScore;

}
