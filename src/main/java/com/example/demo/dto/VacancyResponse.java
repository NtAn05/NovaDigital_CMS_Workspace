package com.example.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO trả về thông tin một tin tuyển dụng cho phía Frontend.
 * Chỉ phơi bày các trường cần thiết, không trả về toàn bộ Entity.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class VacancyResponse {
    private Long id;
    private String title;
    private String description;
    private String workstream;
    private String location;
    private String jobType;
}
