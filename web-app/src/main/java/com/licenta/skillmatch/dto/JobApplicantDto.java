package com.licenta.skillmatch.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JobApplicantDto {
    private Long id;
    private String candidateName;
    private String candidateEmail;
    private String status;
    private Double finalScore;
    private LocalDateTime applyDate;
    private String candidateCvPath;
}

