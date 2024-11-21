package com.dreamhire.candidate_scores.dto;


import lombok.*;

import java.util.List;

@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class QuestionsRequest {
    String id;
    List<String> topics;
}
