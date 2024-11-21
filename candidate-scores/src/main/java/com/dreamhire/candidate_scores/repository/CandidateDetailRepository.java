package com.dreamhire.candidate_scores.repository;

import com.dreamhire.candidate_scores.model.CandidateDetails;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface CandidateDetailRepository extends MongoRepository<CandidateDetails, String> {
}
