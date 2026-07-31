package com.example.demo.service;

import com.example.demo.dto.QuotationItemDto;
import com.example.demo.dto.QuotationRequest;
import com.example.demo.entity.*;
import com.example.demo.repository.*;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;

import com.example.demo.annotation.Auditable;

@Service
public class QuotationService {

    @Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;

    @Autowired private QuotationRepository quotationRepository;

    @Autowired private QuotationItemRepository quotationItemRepository;
    @Autowired private ConsultationAppointmentRepository appointmentRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private ProjectRepository projectRepository;
    @Autowired private ProjectClientRepository projectClientRepository;
    @Autowired private JavaMailSender mailSender;
    @Autowired private SseEmitterService sseEmitterService;
    @Autowired private NotificationRepository notificationRepository;

    @Transactional
    @Auditable(action = "CREATE", table = "quotations")
    public Quotation createQuotationFromBooking(QuotationRequest request, Long adminUserId) {
        ConsultationAppointment booking = appointmentRepository.findById(request.getBookingId())
                .orElseThrow(() -> new RuntimeException("Booking not found"));
        User admin = userRepository.findById(adminUserId)
                .orElseThrow(() -> new RuntimeException("Admin not found"));
        User client = userRepository.findById(booking.getClientId())
                .orElseThrow(() -> new RuntimeException("Client not found"));

        Quotation quotation = new Quotation();
        quotation.setQuoteCode("QUOTE-" + System.currentTimeMillis());
        quotation.setBooking(booking);
        quotation.setClient(client);
        quotation.setCreatedBy(admin);
        quotation.setTitle(request.getTitle());
        quotation.setSubtotal(request.getSubtotal());
        quotation.setDiscountAmount(request.getDiscountAmount());
        quotation.setTaxAmount(request.getTaxAmount());
        quotation.setTotalAmount(request.getTotalAmount());
        quotation.setDepositPercentage(request.getDepositPercentage());
        quotation.setNotes(request.getNotes());
        quotation.setStatus("DRAFT");
        
        Quotation savedQuotation = quotationRepository.save(quotation);

        if (request.getItems() != null) {
            for (QuotationItemDto itemDto : request.getItems()) {
                QuotationItem item = new QuotationItem();
                item.setQuotation(savedQuotation);
                item.setItemName(itemDto.getItemName());
                item.setDescription(itemDto.getDescription());
                item.setQuantity(itemDto.getQuantity());
                item.setUnitPrice(itemDto.getUnitPrice());
                item.setSubtotal(itemDto.getSubtotal());
                quotationItemRepository.save(item);
            }
        }
        return savedQuotation;
    }

    @Transactional
    public void sendEmail(Long quotationId) throws MessagingException {
        Quotation quotation = quotationRepository.findById(quotationId)
                .orElseThrow(() -> new RuntimeException("Quotation not found"));
        
        String token = UUID.randomUUID().toString();
        quotation.setApprovalToken(token);
        quotation.setTokenExpiry(LocalDateTime.now().plusDays(7));
        quotation.setStatus("PROPOSED");
        quotationRepository.save(quotation);

        User client = quotation.getClient();
        String toAddress = client.getEmail();
        String subject = "Project Quotation: " + quotation.getTitle();
        
        String approveUrl = baseUrl + "/api/quotations/public/approve?token=" + token;
        
        String noteSection = "";
        if (quotation.getNotes() != null && !quotation.getNotes().trim().isEmpty()) {
            noteSection = "<div style=\"margin-bottom: 24px; padding: 16px; background-color: #fff8e1; border-left: 4px solid #ffb300; border-radius: 4px; color: #5d4037; font-size: 14px;\">"
                    + "  <strong>Note (Optional):</strong><br/>"
                    + quotation.getNotes().replace("\n", "<br/>")
                    + "</div>";
        }
        
        String safeClientName = client.getFullName() != null ? client.getFullName() : "Valued Client";

        String htmlMsg = "<div style=\"font-family: 'Segoe UI', Helvetica, Arial, sans-serif; max-width: 600px; border: 1px solid #e2e8f0; border-radius: 12px; margin: 20px auto; background: #ffffff; box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.05); overflow: hidden;\">"
                + "  <!-- Header -->"
                + "  <div style=\"background-color: #0b0f19; padding: 25px; text-align: center; border-bottom: 3px solid #4f46e5;\">"
                + "    <img src=\"https://images.unsplash.com/photo-1618005182384-a83a8bd57fbe?auto=format&fit=crop&w=80&h=80&q=80\" alt=\"NovaDigital Logo\" style=\"width: 48px; height: 48px; border-radius: 50%; vertical-align: middle; margin-right: 12px; border: 2px solid #4f46e5; object-fit: cover;\">"
                + "    <span style=\"font-size: 24px; font-weight: 800; color: #ffffff; letter-spacing: 0.5px; vertical-align: middle;\">NOVA<span style=\"color: #4f46e5;\">DIGITAL</span></span>"
                + "  </div>"
                + "  <!-- Body -->"
                + "  <div style=\"padding: 30px; line-height: 1.6; color: #334155;\">"
                + "    <h2 style=\"color: #0f172a; font-size: 20px; font-weight: 700; margin-top: 0; margin-bottom: 16px;\">Hello " + safeClientName + ",</h2>"
                + "    <p style=\"margin-top: 0; margin-bottom: 24px; font-size: 15px; color: #475569;\">We are pleased to send you the quotation for the project <b>" + quotation.getTitle() + "</b>. Please find the details of our pricing proposal below:</p>"
                + "    <!-- Quotation Details Box -->"
                + "    <div style=\"background-color: #f8fafc; padding: 24px; border-radius: 8px; border: 1px solid #e2e8f0; margin-bottom: 24px;\">"
                + "      <h4 style=\"margin-top: 0; margin-bottom: 16px; font-size: 14px; font-weight: 700; text-transform: uppercase; letter-spacing: 0.5px; color: #64748b;\">Quotation Details</h4>"
                + "      <table style=\"width: 100%; border-collapse: collapse; font-size: 15px;\">"
                + "        <tr>"
                + "          <td style=\"padding: 6px 0; font-weight: 600; color: #1e293b; width: 120px; vertical-align: top;\">Quote Code:</td>"
                + "          <td style=\"padding: 6px 0; color: #475569; vertical-align: top;\">" + quotation.getQuoteCode() + "</td>"
                + "        </tr>"
                + "        <tr>"
                + "          <td style=\"padding: 6px 0; font-weight: 600; color: #1e293b; width: 120px; vertical-align: top;\">Subtotal:</td>"
                + "          <td style=\"padding: 6px 0; color: #475569; vertical-align: top;\">$" + String.format("%.2f", quotation.getSubtotal()) + "</td>"
                + "        </tr>"
                + "        <tr>"
                + "          <td style=\"padding: 6px 0; font-weight: 600; color: #1e293b; width: 120px; vertical-align: top;\">Discount:</td>"
                + "          <td style=\"padding: 6px 0; color: #475569; vertical-align: top;\">-$" + String.format("%.2f", quotation.getDiscountAmount()) + "</td>"
                + "        </tr>"
                + "        <tr>"
                + "          <td style=\"padding: 6px 0; font-weight: 600; color: #1e293b; width: 120px; vertical-align: top;\">Tax:</td>"
                + "          <td style=\"padding: 6px 0; color: #475569; vertical-align: top;\">$" + String.format("%.2f", quotation.getTaxAmount()) + "</td>"
                + "        </tr>"
                + "        <tr style=\"border-top: 1px solid #e2e8f0; border-bottom: 1px solid #e2e8f0;\">"
                + "          <td style=\"padding: 10px 0; font-weight: 700; color: #0f172a; width: 120px; vertical-align: top;\">Total Amount:</td>"
                + "          <td style=\"padding: 10px 0; font-weight: 700; color: #4f46e5; vertical-align: top;\">$" + String.format("%.2f", quotation.getTotalAmount()) + "</td>"
                + "        </tr>"
                + "        <tr>"
                + "          <td style=\"padding: 6px 0; font-weight: 600; color: #1e293b; width: 120px; vertical-align: top;\">1st Deposit (" + quotation.getDepositPercentage() + "%):</td>"
                + "          <td style=\"padding: 6px 0; color: #0f172a; font-weight: 600; vertical-align: top;\">$" + String.format("%.2f", quotation.getTotalAmount() * quotation.getDepositPercentage() / 100) + "</td>"
                + "        </tr>"
                + "      </table>"
                + "    </div>"
                + noteSection
                + "    <!-- CTA Area -->"
                + "    <div style=\"text-align: center; margin: 30px 0;\">"
                + "      <a href=\"" + approveUrl + "\" style=\"display: inline-block; background-color: #10b981; color: #ffffff; padding: 12px 28px; text-decoration: none; border-radius: 8px; font-weight: bold; font-size: 16px; box-shadow: 0 4px 6px -1px rgba(16, 185, 129, 0.2);\">Accept & Place Deposit</a>"
                + "    </div>"
                + "    <p style=\"margin-top: 0; margin-bottom: 0; font-size: 14px; color: #64748b;\">By clicking \"Accept & Place Deposit\", you agree to our terms of service and project milestone scheduling. An initial deposit invoice will be generated on your dashboard.</p>"
                + "  </div>"
                + "  <!-- Footer -->"
                + "  <div style=\"background-color: #f8fafc; padding: 20px 30px; border-top: 1px solid #e2e8f0; text-align: center;\">"
                + "    <p style=\"margin: 0; font-size: 12px; color: #94a3b8;\">This is an automated quotation notification message. Please do not reply directly to this email.</p>"
                + "    <p style=\"margin: 8px 0 0 0; font-size: 12px; color: #94a3b8; font-weight: 600;\">© 2026 NovaDigital Co., Ltd. All rights reserved.</p>"
                + "  </div>"
                + "</div>";

        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
        helper.setTo(toAddress);
        helper.setSubject(subject);
        helper.setText(htmlMsg, true);

        mailSender.send(message);

        // Tạo thông báo trong Web Account của khách hàng
        Notification notification = new Notification();
        notification.setUserId(client.getId());
        notification.setTitle("New Quotation Received");
        notification.setMessage("A quotation for \"" + quotation.getTitle() + "\" worth $" + quotation.getTotalAmount() + " has been sent to your email. Please review it.");
        notification.setLink("/rented-project.html");
        notificationRepository.save(notification);
    }

    @Transactional
    public Quotation approveQuotation(String token) {
        Quotation quotation = quotationRepository.findByApprovalToken(token)
                .orElseThrow(() -> new RuntimeException("Invalid token"));
        
        if (quotation.getTokenExpiry() != null && quotation.getTokenExpiry().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Token expired");
        }

        // Idempotency guard: if the client clicks the "Accept" link again after already
        // approving, don't re-fire the SSE alert / duplicate notifications to admin & client.
        if (!"PROPOSED".equals(quotation.getStatus()) && !"NEGOTIATING".equals(quotation.getStatus())) {
            return quotation;
        }

        quotation.setStatus("APPROVED");
        quotationRepository.save(quotation);

        // Once accepted, the linked booking is fully done — admin can't act on it anymore.
        if (quotation.getBooking() != null) {
            ConsultationAppointment booking = quotation.getBooking();
            booking.setStatus(com.example.demo.entity.enums.AppointmentStatus.COMPLETED);
            appointmentRepository.save(booking);
        }
        
        // Bắn SSE Alert cho Admin
        sseEmitterService.sendEventToAll("QUOTE_APPROVED", "Khách hàng " + quotation.getClient().getFullName() + " vừa phê duyệt báo giá " + quotation.getQuoteCode());
        
        // Tạo thông báo trong Web Account của khách hàng
        Notification userNotification = new Notification();
        userNotification.setUserId(quotation.getClient().getId());
        userNotification.setTitle("Quotation Approved");
        userNotification.setMessage("You have successfully approved the quotation \"" + quotation.getTitle() + "\" for $" + quotation.getTotalAmount() + ". Our team will start the project shortly.");
        userNotification.setLink("/rented-project.html");
        notificationRepository.save(userNotification);

        // Tạo thông báo trong Web Account cho các Admin
        List<User> admins = userRepository.findByRole("ROLE_ADMIN");
        for (User admin : admins) {
            Notification adminNotification = new Notification();
            adminNotification.setUserId(admin.getId());
            adminNotification.setTitle("Quotation Approved by Client");
            adminNotification.setMessage("Client " + quotation.getClient().getFullName() + " has approved quotation " + quotation.getQuoteCode() + " ($" + quotation.getTotalAmount() + "). Please create a project now.");
            adminNotification.setLink("/admin.html");
            notificationRepository.save(adminNotification);
        }

        return quotation;
    }

    @Transactional
    public Project convertToProject(Long quotationId) {
        return convertToProject(quotationId, null);
    }

    @Transactional
    public Project convertToProject(Long quotationId, Map<String, Object> projectData) {
        Quotation quotation = quotationRepository.findById(quotationId)
                .orElseThrow(() -> new RuntimeException("Quotation not found"));
        
        if (!"APPROVED".equals(quotation.getStatus()) && !"CONVERTED".equals(quotation.getStatus())) {
            throw new RuntimeException("Quotation must be approved to convert to project");
        }

        if (quotation.getConvertedProject() != null && projectData == null) {
            return quotation.getConvertedProject();
        }

        String title = (projectData != null && projectData.get("title") != null && !((String)projectData.get("title")).isBlank())
                ? ((String) projectData.get("title")).trim()
                : quotation.getTitle();

        String category = (projectData != null && projectData.get("category") != null && !((String)projectData.get("category")).isBlank())
                ? ((String) projectData.get("category")).trim()
                : "Converted from Quote";

        String description = (projectData != null && projectData.get("description") != null && !((String)projectData.get("description")).isBlank())
                ? ((String) projectData.get("description")).trim()
                : (quotation.getNotes() != null && !quotation.getNotes().isBlank() ? quotation.getNotes() : "Project created from quotation: " + quotation.getQuoteCode());

        String imageUrl = (projectData != null && projectData.get("imageUrl") != null) ? (String) projectData.get("imageUrl") : null;
        String technologies = (projectData != null && projectData.get("technologies") != null) ? (String) projectData.get("technologies") : null;

        Double depositAmount = 0.0;
        if (projectData != null && projectData.get("depositAmount") != null) {
            try {
                depositAmount = Double.parseDouble(projectData.get("depositAmount").toString());
            } catch (Exception ignored) {}
        } else if (quotation.getTotalAmount() != null && quotation.getDepositPercentage() != null) {
            depositAmount = quotation.getTotalAmount() * quotation.getDepositPercentage() / 100.0;
        }

        if (title != null && title.length() > 255) title = title.substring(0, 255);
        if (category != null && category.length() > 100) category = category.substring(0, 100);

        Project project = quotation.getConvertedProject() != null ? quotation.getConvertedProject() : new Project();
        project.setTitle(title);
        project.setDescription(description);
        project.setCategory(category);
        if (imageUrl != null && !imageUrl.isBlank()) project.setImageUrl(imageUrl);
        if (technologies != null) project.setTechnologies(technologies);
        project.setDepositAmount(depositAmount);
        if (project.getDepositPaid() == null) {
            project.setDepositPaid(false);
        }

        Project savedProject;
        try {
            savedProject = projectRepository.save(project);
        } catch (Exception e) {
            if (imageUrl != null && imageUrl.length() > 255) {
                project.setImageUrl(imageUrl.substring(0, 255));
                savedProject = projectRepository.save(project);
            } else {
                throw e;
            }
        }

        // Link client user to project
        Long clientId = null;
        if (projectData != null && projectData.get("clientId") != null) {
            try {
                clientId = Long.parseLong(projectData.get("clientId").toString());
            } catch (Exception ignored) {}
        }
        User clientUser = null;
        if (clientId != null && clientId > 0) {
            clientUser = userRepository.findById(clientId).orElse(null);
        } else if (quotation.getClient() != null) {
            clientUser = quotation.getClient();
        }

        if (clientUser != null) {
            if (projectClientRepository.findByProjectIdAndUserId(savedProject.getId(), clientUser.getId()).isEmpty()) {
                ProjectClient pc = new ProjectClient();
                pc.setProject(savedProject);
                pc.setUser(clientUser);
                projectClientRepository.save(pc);
            }
        }

        quotation.setConvertedProject(savedProject);
        quotation.setStatus("CONVERTED");
        quotationRepository.save(quotation);

        return savedProject;
    }
    
    public List<Quotation> getAllQuotations() {
        return quotationRepository.findAll();
    }
    
    public Quotation getQuotationById(Long id) {
        return quotationRepository.findById(id).orElse(null);
    }

    public List<Quotation> getQuotationsForClient(String username) {
        User client = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Client not found"));
        return quotationRepository.findByClientId(client.getId());
    }
}