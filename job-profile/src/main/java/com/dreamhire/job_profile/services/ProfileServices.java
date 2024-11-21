package com.dreamhire.job_profile.services;

import com.dreamhire.job_profile.dto.JobProfileResponse;
import com.dreamhire.job_profile.model.JobProfile;
import com.dreamhire.job_profile.repository.ProfileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Log
@RequiredArgsConstructor
public class ProfileServices {
    private final ProfileRepository profileRepository;

    public ResponseEntity<JobProfile> createProfile(String name, String jD){
        JobProfile profile = new JobProfile();
        profile.setProfileName(name);
        profile.setJobDescription(jD);
        profileRepository.save(profile);
        return new ResponseEntity<>(profile,HttpStatus.CREATED );
    }

    public List<JobProfileResponse> listAllProfiles() {
        List<JobProfileResponse> responseList = new ArrayList<>();
        List<JobProfile> allProfiles = profileRepository.findAll();
        for(JobProfile profile : allProfiles){
            JobProfileResponse temp = new JobProfileResponse(profile.getProfileName(), profile.getJobDescription());
            responseList.add(temp);
        }

        return responseList;
    }

    public ResponseEntity<JobProfileResponse> findByName(String name) {
        Optional<JobProfile> profileOptional = profileRepository.findById(name);
        if(!profileOptional.isPresent()) return ResponseEntity.notFound().build();

        JobProfile profile = profileOptional.get();

        return new ResponseEntity<>(JobProfileResponse.builder()
                .name(profile.getProfileName())
                .jd(profile.getJobDescription())
                .build(), HttpStatus.OK);
    }
}
