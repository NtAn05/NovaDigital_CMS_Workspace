package com.example.demo.dto;

import lombok.Data;

import java.util.List;

@Data
public class BookingRequest {
    private Long serviceId;
    private Long clientId;            // FK -> User.id (user profile available)
    private Long expertId;            // FK -> Member.id (expert table available)
    private List<Long> addonIds;      // list of selected Service_Addon.id
    private String appointmentDate;   // "yyyy-MM-dd"
    private String timeSlot;          // "HH:mm"
    private String messageContent;
    private String attachmentUrl;
    private String captchaToken;
    private String captchaAnswer;
}