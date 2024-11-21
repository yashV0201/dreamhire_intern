package com.testService.ai_open.repository.mongodb;

import com.testService.ai_open.model.mongodb.CandidateScores;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CandidateScoreRepository extends MongoRepository<CandidateScores,Integer> {
    List<CandidateScores> findByProfileNameOrderByTotalScoreDesc(String profileName);

    CandidateScores findByfileName(String fileName);

}
