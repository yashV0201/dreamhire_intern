package com.dreamhire.job_profile.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name="test_resumes")
@Data
public class TestResume {

    @Id
    private String fileName;
}
