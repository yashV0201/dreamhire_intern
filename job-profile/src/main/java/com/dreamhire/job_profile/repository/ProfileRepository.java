package com.dreamhire.job_profile.repository;

import com.dreamhire.job_profile.model.JobProfile;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProfileRepository extends JpaRepository<JobProfile,String> {

}
