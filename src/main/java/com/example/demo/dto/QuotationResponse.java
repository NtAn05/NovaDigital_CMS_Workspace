package com.example.demo.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class QuotationResponse {
    private Long id;
    private String quoteCode;
    private Long bookingId;
    private String clientName;
    private String title;
    private String status;
    private String version;
    private Double subtotal;
    private Double discountAmount;
    private Double taxAmount;
    private Double totalAmount;
    private Double depositPercentage;
    private String notes;
    private LocalDateTime createdAt;
    private List<QuotationItemDto> items;
}
