package com.example.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
public class ServiceResponse {
    private Long id;
    private String title;
    private String description;
    private String iconUrl;
}

