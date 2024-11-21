package com.dreamhire.candidate_scores.dto;

import lombok.*;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Data
@Builder
public class CandidateDto {
    String name;

    List<String> email;

    String phone;

}
