package com.testService.ai_open.repository.postgres;

import com.testService.ai_open.model.postgres.JobProfile;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProfileRepository extends JpaRepository<JobProfile,String> {

}
