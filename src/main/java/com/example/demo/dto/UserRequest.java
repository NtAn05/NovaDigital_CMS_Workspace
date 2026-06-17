package com.example.demo.dto;

import lombok.Data;

@Data
public class UserRequest {
    private String username;
    private String fullName;
    private String email;
    private String phone;
    private String password;
    private String role;
    private Boolean enabled;
}
