package com.example.demo.dto;

import lombok.Data;
import java.util.List;

@Data
public class QuotationRequest {
    private Long bookingId;
    private String title;
    private Double subtotal;
    private Double discountAmount;
    private Double taxAmount;
    private Double totalAmount;
    private Double depositPercentage;
    private String notes;
    private List<QuotationItemDto> items;
}
