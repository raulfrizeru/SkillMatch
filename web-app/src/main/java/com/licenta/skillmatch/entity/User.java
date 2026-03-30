package com.licenta.skillmatch.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "users")
@Inheritance(strategy = InheritanceType.JOINED)
@Data
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private String username;
    private String email;
    private String password;

    @ManyToOne
    @JoinColumn(name = "group_id")
    private UserGroup userGroup;

}
