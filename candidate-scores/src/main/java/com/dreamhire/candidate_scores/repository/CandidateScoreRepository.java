package com.dreamhire.candidate_scores.repository;


import com.dreamhire.candidate_scores.model.CandidateScores;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CandidateScoreRepository extends MongoRepository<CandidateScores,Integer> {
    List<CandidateScores> findByProfileNameOrderByTotalScoreDesc(String profileName);

    CandidateScores findByfileName(String fileName);


    CandidateScores findByid(String id);
}
