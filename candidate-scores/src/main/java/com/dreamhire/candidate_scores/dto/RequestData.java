package com.dreamhire.candidate_scores.dto;

import lombok.*;

@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RequestData {
    private String fileName;
    private String data;
    private String table;
    private String profileName;
}
