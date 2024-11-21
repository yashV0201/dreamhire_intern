package com.dreamhire.candidate_scores.repository;

import com.dreamhire.candidate_scores.model.CalibratedTable;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface CalibratedTableRespository extends MongoRepository<CalibratedTable, Integer> {
    void deleteByProfileName(String profileName);

    CalibratedTable findByProfileName(String profileName);

}
