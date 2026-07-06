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
        message.setSubject("NovaDigital - Mã xác thực OTP đặt lại mật khẩu");
        message.setText("Chào bạn,\n\n"
                + "Bạn đã yêu cầu đặt lại mật khẩu tại NovaDigital.\n"
                + "Mã OTP của bạn là: " + otp + "\n"
                + "Mã này có hiệu lực trong vòng 5 phút.\n\n"
                + "Nếu bạn không thực hiện yêu cầu này, vui lòng bỏ qua email.\n\n"
                + "Trân trọng,\n"
                + "NovaDigital Team");
        mailSender.send(message);
    }
}
