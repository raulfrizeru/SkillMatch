package com.licenta.skillmatch.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "candidates")
@Data
@EqualsAndHashCode(callSuper = true)
public class Candidate extends User {
    private String firstName;
    private String lastName;
    private String cvFilePath;

    @Column(columnDefinition = "TEXT")
    private String extractedCvJson;
    @Column(columnDefinition = "TEXT")
    private String extractedCvOcr;

    @OneToMany(mappedBy = "candidate", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PastInterview> pastInterviews = new ArrayList<>();

    @JsonIgnore
    @OneToMany(mappedBy = "candidate", cascade = CascadeType.ALL)
    private List<JobApplication> jobApplications = new ArrayList<>();

}
