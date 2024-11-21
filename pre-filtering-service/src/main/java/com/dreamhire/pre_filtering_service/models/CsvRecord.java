package com.dreamhire.pre_filtering_service.models;

import lombok.*;

@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CsvRecord {
    private String factors;
    private String options;
    private String prompt;
}
