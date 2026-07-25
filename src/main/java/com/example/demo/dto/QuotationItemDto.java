package com.example.demo.dto;

import lombok.Data;

@Data
public class QuotationItemDto {
    private String itemName;
    private String description;
    private Integer quantity;
    private Double unitPrice;
    private Double subtotal;
}
