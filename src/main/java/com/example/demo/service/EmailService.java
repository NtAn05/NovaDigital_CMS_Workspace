package com.example.demo.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    public void sendOtpEmail(String toEmail, String otp) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("studyhub123vn@gmail.com");
        message.setTo(toEmail);
        message.setSubject("NovaDigital - OTP Password Reset Verification Code");
        message.setText("Hello,\n\n"
                + "You have requested to reset your password at NovaDigital.\n"
                + "Your OTP verification code is: " + otp + "\n"
                + "This code is valid for 5 minutes.\n\n"
                + "If you did not make this request, please ignore this email.\n\n"
                + "Best regards,\n"
                + "NovaDigital Team");
        mailSender.send(message);
    }
}
