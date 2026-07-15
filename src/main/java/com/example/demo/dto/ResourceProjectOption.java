package com.example.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ResourceProjectOption {
    private Long id;
    private String title;
    private String category;
    private String technologies;
}
