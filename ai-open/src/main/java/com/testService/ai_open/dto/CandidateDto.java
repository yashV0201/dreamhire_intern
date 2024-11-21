package com.testService.ai_open.dto;

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
