package com.testService.ai_open.model.postgres;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name="ideal_resume")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class IdealResume implements Resumes {
    @Id
    private String filename;


    @Column(name = "text_data", columnDefinition = "TEXT")
    private String textData;

}
