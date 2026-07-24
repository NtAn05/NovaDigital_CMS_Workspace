package com.example.demo.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginRequest {
    @NotBlank(message = "Username or email cannot be empty")
    private String usernameOrEmail;

    @NotBlank(message = "Password cannot be empty")
    private String password;

    // Optional captcha fields — required only when the server enforces captcha (after 5 failed attempts)
    private String captchaToken;
    private String captchaAnswer;
}
