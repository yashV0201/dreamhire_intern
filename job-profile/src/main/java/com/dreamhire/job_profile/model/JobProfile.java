package com.dreamhire.job_profile.model;

import jakarta.persistence.*;
import lombok.Data;

import java.util.List;

@Entity
@Table(name="job_profiles")
@Data
public class JobProfile {

    @Id
    private String profileName;

    @Column(columnDefinition = "TEXT")
    private String jobDescription;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<IdealResume> idealResumes;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<TestResume> testResumes;
}
