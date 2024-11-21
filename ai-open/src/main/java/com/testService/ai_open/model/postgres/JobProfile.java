package com.testService.ai_open.model.postgres;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Entity
@Table(name="job_profiles")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class JobProfile {

    @Id
    private String name;

    @Column(columnDefinition = "TEXT")
    private String jobDescription;

    @Column(columnDefinition = "TEXT")
    private String weightedTable;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<IdealResume> idealProfiles;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<TestResume> testProfiles;

    @Column(columnDefinition = "TEXT")
    private String scoreTable;


}
