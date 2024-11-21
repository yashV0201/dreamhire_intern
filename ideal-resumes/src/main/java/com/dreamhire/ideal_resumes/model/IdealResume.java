package com.dreamhire.ideal_resumes.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Data
public class IdealResume {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    private String fileName;

    private String profileName;

    @Column(columnDefinition = "TEXT")
    private String data;
}
