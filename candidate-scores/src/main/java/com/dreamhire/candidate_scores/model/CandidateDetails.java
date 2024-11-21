package com.dreamhire.candidate_scores.model;


import lombok.*;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.MongoId;

import java.util.List;
import java.util.Map;

@Document(collection = "candidateDetails")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class CandidateDetails {

    @MongoId
    String id;

    String name;
    String phoneNumber;
    List<String> emailId;
    String profileName;

    CandidateScores candidateScores;

    Map<String, Object> Questions;
}
