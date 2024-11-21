package com.dreamhire.candidate_scores.model;

import lombok.*;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Map;

@Document(collection = "calibratedTable")
@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CalibratedTable {

    private String profileName;

    private Map<String , Integer> table;
}
