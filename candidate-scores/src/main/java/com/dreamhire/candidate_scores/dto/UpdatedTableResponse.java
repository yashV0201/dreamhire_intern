package com.dreamhire.candidate_scores.dto;

import com.dreamhire.candidate_scores.model.CalibratedTable;
import lombok.*;

@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UpdatedTableResponse {
    public CalibratedTable calibratedTable;
    public String responseTable;
}
