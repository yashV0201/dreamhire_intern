package com.dreamhire.candidate_scores.dto;

import lombok.*;

import java.util.Map;

@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CalibratedTableDto {
    private String profileName;

    private Map<String , Integer> table;
}
