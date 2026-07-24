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
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom("studyhub123vn@gmail.com");
            helper.setTo(toEmail);
            helper.setSubject("Password Reset Verification Code - NovaDigital");

            String htmlMsg = "<div style=\"font-family: 'Segoe UI', Helvetica, Arial, sans-serif; max-width: 600px; border: 1px solid #e2e8f0; border-radius: 16px; margin: 20px auto; background: #ffffff; box-shadow: 0 10px 25px -5px rgba(0, 0, 0, 0.08); overflow: hidden;\">"
                    + "  <!-- Header with Brand Logo -->"
                    + "  <div style=\"background: linear-gradient(135deg, #0f172a 0%, #1e293b 100%); padding: 28px 30px; text-align: center; border-bottom: 3px solid #2563eb;\">"
                    + "    <div style=\"display: inline-flex; align-items: center; justify-content: center;\">"
                    + "      <img src=\"https://images.unsplash.com/photo-1618005182384-a83a8bd57fbe?auto=format&fit=crop&w=100&h=100&q=80\" alt=\"NovaDigital Logo\" style=\"width: 44px; height: 44px; border-radius: 50%; vertical-align: middle; margin-right: 12px; border: 2px solid #2563eb; object-fit: cover;\">"
                    + "      <span style=\"font-size: 24px; font-weight: 800; color: #ffffff; letter-spacing: 0.5px; vertical-align: middle;\">NOVA<span style=\"color: #38bdf8;\">DIGITAL</span></span>"
                    + "    </div>"
                    + "  </div>"
                    + "  <!-- Main Body -->"
                    + "  <div style=\"padding: 36px 32px; line-height: 1.6; color: #334155;\">"
                    + "    <h2 style=\"color: #0f172a; font-size: 20px; font-weight: 700; margin-top: 0; margin-bottom: 12px; text-align: center;\">Password Reset Verification</h2>"
                    + "    <p style=\"margin-top: 0; margin-bottom: 24px; font-size: 15px; color: #475569; text-align: center;\">We received a request to reset your password for your <strong>NovaDigital</strong> account. Use the verification code below to complete the process:</p>"
                    + "    "
                    + "    <!-- OTP Display Box -->"
                    + "    <div style=\"background: linear-gradient(135deg, #f0f9ff 0%, #e0f2fe 100%); border: 1px solid #bae6fd; border-radius: 12px; padding: 20px; text-align: center; margin: 28px 0;\">"
                    + "      <span style=\"font-size: 12px; font-weight: 700; text-transform: uppercase; letter-spacing: 1.5px; color: #0284c7; display: block; margin-bottom: 8px;\">Your Security Code</span>"
                    + "      <div style=\"font-family: 'Courier New', Consolas, monospace; font-size: 36px; font-weight: 800; letter-spacing: 10px; color: #0369a1;\">" + otp + "</div>"
                    + "      <span style=\"font-size: 13px; color: #64748b; margin-top: 8px; display: block;\">⏱️ Valid for <strong>5 minutes</strong></span>"
                    + "    </div>"
                    + "    "
                    + "    <!-- Warning Notice -->"
                    + "    <div style=\"background-color: #f8fafc; border-left: 4px solid #f59e0b; padding: 14px 18px; border-radius: 4px; margin-bottom: 24px;\">"
                    + "      <p style=\"margin: 0; font-size: 13px; color: #78350f; line-height: 1.5;\">"
                    + "        <strong>Security Notice:</strong> If you did not request a password reset, please ignore this email or contact support if you suspect unauthorized activity."
                    + "      </p>"
                    + "    </div>"
                    + "    "
                    + "    <p style=\"margin-top: 0; margin-bottom: 0; font-size: 14px; color: #64748b; text-align: center;\">Need help? Contact our support team at <a href=\"mailto:contact@novadigital.com\" style=\"color: #2563eb; text-decoration: none; font-weight: 600;\">contact@novadigital.com</a></p>"
                    + "  </div>"
                    + "  <!-- Footer -->"
                    + "  <div style=\"background-color: #f8fafc; padding: 22px 30px; border-top: 1px solid #e2e8f0; text-align: center;\">"
                    + "    <p style=\"margin: 0; font-size: 12px; color: #94a3b8;\">This is an automated security notification. Please do not reply directly to this email.</p>"
                    + "    <p style=\"margin: 6px 0 0 0; font-size: 12px; color: #94a3b8; font-weight: 600;\">© 2026 NovaDigital Co., Ltd. All rights reserved.</p>"
                    + "  </div>"
                    + "</div>";

            helper.setText(htmlMsg, true);
            mailSender.send(message);
        } catch (Exception e) {
            System.err.println("[EmailService] Could not send OTP email: " + e.getMessage());
        }
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

            String htmlMsg = "<div style=\"font-family: 'Segoe UI', Helvetica, Arial, sans-serif; max-width: 600px; border: 1px solid #e2e8f0; border-radius: 12px; margin: 20px auto; background: #ffffff; box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.05); overflow: hidden;\">"
                    + "  <!-- Header -->"
                    + "  <div style=\"background-color: #0b0f19; padding: 25px; text-align: center; border-bottom: 3px solid #4f46e5;\">"
                    + "    <img src=\"https://images.unsplash.com/photo-1618005182384-a83a8bd57fbe?auto=format&fit=crop&w=80&h=80&q=80\" alt=\"NovaDigital Logo\" style=\"width: 48px; height: 48px; border-radius: 50%; vertical-align: middle; margin-right: 12px; border: 2px solid #4f46e5; object-fit: cover;\">"
                    + "    <span style=\"font-size: 24px; font-weight: 800; color: #ffffff; letter-spacing: 0.5px; vertical-align: middle;\">NOVA<span style=\"color: #4f46e5;\">DIGITAL</span></span>"
                    + "  </div>"
                    + "  <!-- Body -->"
                    + "  <div style=\"padding: 30px; line-height: 1.6; color: #334155;\">"
                    + "    <h2 style=\"color: #0f172a; font-size: 20px; font-weight: 700; margin-top: 0; margin-bottom: 16px;\">Hello " + safeName + ",</h2>"
                    + "    <p style=\"margin-top: 0; margin-bottom: 24px; font-size: 15px; color: #475569;\">Thank you for contacting NovaDigital. We have successfully received your feedback submission. Below is a copy of the details you submitted for your records.</p>"
                    + "    <!-- Submission Box -->"
                    + "    <div style=\"background-color: #f8fafc; padding: 24px; border-radius: 8px; border: 1px solid #e2e8f0; margin-bottom: 24px;\">"
                    + "      <h4 style=\"margin-top: 0; margin-bottom: 16px; font-size: 14px; font-weight: 700; text-transform: uppercase; letter-spacing: 0.5px; color: #64748b;\">Feedback Details</h4>"
                    + "      <table style=\"width: 100%; border-collapse: collapse; font-size: 15px;\">"
                    + "        <tr>"
                    + "          <td style=\"padding: 6px 0; font-weight: 600; color: #1e293b; width: 80px; vertical-align: top;\">Name:</td>"
                    + "          <td style=\"padding: 6px 0; color: #475569; vertical-align: top;\">" + safeName + "</td>"
                    + "        </tr>"
                    + "        <tr>"
                    + "          <td style=\"padding: 6px 0; font-weight: 600; color: #1e293b; width: 80px; vertical-align: top;\">Email:</td>"
                    + "          <td style=\"padding: 6px 0; color: #475569; vertical-align: top;\">" + toEmail + "</td>"
                    + "        </tr>"
                    + "        <tr>"
                    + "          <td style=\"padding: 6px 0; font-weight: 600; color: #1e293b; width: 80px; vertical-align: top;\">Message:</td>"
                    + "          <td style=\"padding: 6px 0; color: #475569; vertical-align: top; line-height: 1.5; white-space: pre-line;\">" + safeMsg + "</td>"
                    + "        </tr>"
                    + "      </table>"
                    + "    </div>"
                    + "    <p style=\"margin-top: 0; margin-bottom: 0; font-size: 15px; color: #475569;\">Our relationship managers will review your submission and follow up with you shortly. If you have any additional questions, please reach out to us.</p>"
                    + "  </div>"
                    + "  <!-- Footer -->"
                    + "  <div style=\"background-color: #f8fafc; padding: 20px 30px; border-top: 1px solid #e2e8f0; text-align: center;\">"
                    + "    <p style=\"margin: 0; font-size: 12px; color: #94a3b8;\">This is an automated operational notification message. Please do not reply directly to this email.</p>"
                    + "    <p style=\"margin: 8px 0 0 0; font-size: 12px; color: #94a3b8; font-weight: 600;\">© 2026 NovaDigital Co., Ltd. All rights reserved.</p>"
                    + "  </div>"
                    + "</div>";

            helper.setText(htmlMsg, true);
            mailSender.send(message);
        } catch (Exception e) {
            System.err.println("[EmailService] Could not send feedback email: " + e.getMessage());
        }
    }
}
