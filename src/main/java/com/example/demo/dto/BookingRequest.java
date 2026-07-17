package com.example.demo.dto;

import lombok.Data;

import java.util.List;

@Data
public class BookingRequest {
    private Long serviceId;
    private Long clientId;            // FK -> User.id (user-profile có sẵn)
    private Long expertId;            // FK -> Member.id (bảng chuyên gia có sẵn)
    private List<Long> addonIds;      // danh sách Service_Addon.id đã chọn
    private String appointmentDate;   // "yyyy-MM-dd"
    private String timeSlot;          // "HH:mm"
    private String messageContent;
    private String attachmentUrl;
    private String captchaToken;
    private String captchaAnswer;
}