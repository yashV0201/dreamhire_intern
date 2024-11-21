package com.dreamhire.job_profile.controller;

import com.dreamhire.job_profile.dto.JobProfileResponse;
import com.dreamhire.job_profile.model.JobProfile;
import com.dreamhire.job_profile.services.ProfileServices;
import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Log
@RestController
@RequestMapping("profile")
@RequiredArgsConstructor
public class controller {

    private final ProfileServices profileServices;

    @PostMapping("new")
    public ResponseEntity<JobProfile> createNewProfile(@RequestPart("jobDescription") String jobDescription, @RequestPart("name") String name){
            return profileServices.createProfile(name, jobDescription);
    }

    @GetMapping("all")
    public List<JobProfileResponse> listAllProfiles(){
        return profileServices.listAllProfiles();
    }

    @GetMapping("{name}")
    public ResponseEntity<JobProfileResponse> findByName(@PathVariable String name){
        return profileServices.findByName(name);
    }

}
