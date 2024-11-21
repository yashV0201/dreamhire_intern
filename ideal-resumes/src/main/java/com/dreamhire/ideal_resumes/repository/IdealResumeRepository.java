package com.dreamhire.ideal_resumes.repository;

import com.dreamhire.ideal_resumes.model.IdealResume;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IdealResumeRepository extends JpaRepository<IdealResume, Long> {
}
