package com.licenta.skillmatch.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@Table(name = "employers")
@EqualsAndHashCode(callSuper = true)
public class Employer extends User{
    private String companyName;

    @Column(columnDefinition = "TEXT")
    private String description;

    @JsonIgnore
    @OneToMany(mappedBy = "employer", cascade = CascadeType.ALL)
    private List<JobPost> jobPosts = new ArrayList<>();
}
