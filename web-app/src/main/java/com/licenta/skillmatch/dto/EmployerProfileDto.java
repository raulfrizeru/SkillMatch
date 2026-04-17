package com.licenta.skillmatch.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmployerProfileDto {
    private long id;
    private String username;
    private String email;
    private String companyName;
    private String description;
}

