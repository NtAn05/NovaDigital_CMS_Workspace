package com.example.demo.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import jakarta.mail.internet.MimeMessage;
import org.springframework.mail.javamail.MimeMessageHelper;

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

    public void sendFeedbackConfirmationEmail(String toEmail, String name, String userMessage) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom("studyhub123vn@gmail.com");
            helper.setTo(toEmail);

            String safeName = (name != null && !name.isBlank()) ? name : "Valued User";
            helper.setSubject("We've Received Your Feedback, " + safeName + "!");

            String safeMsg = (userMessage != null) ? userMessage.replace("\n", "<br>") : "";

            String htmlMsg = "<div style=\"font-family: sans-serif; padding: 20px; line-height: 1.6; color: #333; max-width: 600px; border: 1px solid #eee; border-radius: 8px; margin: 0 auto; background: #ffffff;\">"
                    + "<h2 style=\"color: #0066cc; margin-top: 0;\">Hello " + safeName + ",</h2>"
                    + "<p>We are Nova Team. Thank you for reaching out to us. We have received your feedback form submission and wanted to send a copy for your personal records.</p>"
                    + "<div style=\"background-color: #f9f9f9; padding: 15px; border-left: 4px solid #0066cc; margin: 20px 0;\">"
                    + "<h4 style=\"margin-top: 0;\">Your Submitted Details:</h4>"
                    + "<p><strong>Name:</strong> " + safeName + "</p>"
                    + "<p><strong>Email:</strong> " + toEmail + "</p>"
                    + "<p style=\"margin-bottom: 0;\"><strong>Message:</strong><br>" + safeMsg + "</p>"
                    + "</div>"
                    + "<p>From NovaDigital,</p>"
                    + "<p>Our support team will review your message and reach back out to you if any follow-up actions are required.</p>"
                    + "<hr style=\"border: 0; border-top: 1px solid #eee; margin: 20px 0;\">"
                    + "<p style=\"font-size: 12px; color: #777;\">This is an automated operational notification message. Please do not reply directly to this mail box.</p>"
                    + "</div>";

            helper.setText(htmlMsg, true);
            mailSender.send(message);
        } catch (Exception e) {
            System.err.println("[EmailService] Could not send feedback email: " + e.getMessage());
        }
    }
}
