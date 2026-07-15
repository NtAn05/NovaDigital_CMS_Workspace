package com.example.demo.dto;

import lombok.Data;

import java.util.List;

@Data
public class BookingResponse {
    private Long id;
    private Long serviceId;
    private Long clientId;
    private Long expertId;
    private List<Long> addonIds;
    private Double basePrice;
    private Double addonsPrice;
    private Double totalPrice;
    private String appointmentDate;
    private String timeSlot;
    private String status;
    private String messageContent;
    private String attachmentUrl;
}