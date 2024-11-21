package com.dreamhire.job_profile.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;


@Entity
@Table(name="ideal_resumes")
@Data
public class IdealResume {

    @Id
    private String fileName;


}
