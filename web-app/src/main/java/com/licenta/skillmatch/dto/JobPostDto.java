package com.licenta.skillmatch.dto;

import com.licenta.skillmatch.entity.Employer;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JobPostDto {
    private long id;
    private String title;
    private String description;
    private String extractedJobJson;
    private boolean isActive;
    private Long employerId;
    private String companyName;
    private String employerDescription;
    private LocalDateTime createdDate;
    private int applicantCount;
    private Double candidateScore;
}
