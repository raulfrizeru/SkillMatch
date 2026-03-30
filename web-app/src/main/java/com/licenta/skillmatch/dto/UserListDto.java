package com.licenta.skillmatch.dto;

import lombok.Data;

@Data
public class UserListDto {
    private Long id;
    private String username;
    private String email;
    private String role;
}
