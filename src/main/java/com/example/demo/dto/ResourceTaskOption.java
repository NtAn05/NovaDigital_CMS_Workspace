package com.example.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDate;

@Data
@AllArgsConstructor
public class ResourceTaskOption {
    private Long id;
    private String name;
    private String status;
    private Integer progressPercentage;
    private LocalDate dueDate;
}
