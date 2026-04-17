package com.licenta.skillmatch.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CandidateProfileDto {
    private long id;
    private String username;
    private String email;
    private String firstName;
    private String lastName;
    private String cvFilePath;
}

