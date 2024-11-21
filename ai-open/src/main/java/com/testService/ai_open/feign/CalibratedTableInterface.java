package com.testService.ai_open.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient("CANDIDATE-SCORES")
public interface CalibratedTableInterface {

    @PostMapping("{profileName}/calibrated-table")
    public String saveCalibratedTable(@PathVariable String profileName , @RequestParam String weightedTable);
}
