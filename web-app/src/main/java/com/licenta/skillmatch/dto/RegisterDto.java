package com.licenta.skillmatch.dto;

import lombok.Data;

@Data
public class RegisterDto {
    private String username;
    private String email;
    private String password;

    private String confirmPassword;
    private String role;

    private String firstName;
    private String lastName;
    private String companyName;
}
