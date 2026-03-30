package com.licenta.skillmatch.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "user_groups")
@Data
public class UserGroup {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private String groupName;

    @JsonIgnore
    @OneToMany(mappedBy = "userGroup", cascade = CascadeType.ALL)
    private List<User> users = new ArrayList<>();
}
