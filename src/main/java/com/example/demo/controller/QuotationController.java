package com.example.demo.controller;

import com.example.demo.config.JwtTokenProvider;
import com.example.demo.dto.QuotationRequest;
import com.example.demo.entity.Project;
import com.example.demo.entity.Quotation;
import com.example.demo.entity.User;
import com.example.demo.service.QuotationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.authentication.AnonymousAuthenticationToken;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/quotations")
public class QuotationController {

    @Autowired
    private QuotationService quotationService;

    @Autowired
    private com.example.demo.repository.UserRepository userRepository;

    @GetMapping("/my")
    public ResponseEntity<?> getMyQuotations() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth instanceof AnonymousAuthenticationToken) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "Vui lòng đăng nhập"));
        }
        try {
            return ResponseEntity.ok(quotationService.getQuotationsForClient(auth.getName()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping("/from-booking/{bookingId}")
    public ResponseEntity<?> createFromBooking(
            @PathVariable Long bookingId,
            @RequestBody QuotationRequest request,
            @RequestParam(required = false) Long adminId) {
        request.setBookingId(bookingId);
        
        // Resolve logged in Admin
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User admin = null;
        if (auth != null && auth.isAuthenticated() && !(auth instanceof AnonymousAuthenticationToken)) {
            String username = auth.getName();
            admin = userRepository.findByUsernameOrEmail(username, username).orElse(null);
        }
        
        Long finalAdminId = (admin != null) ? admin.getId() : adminId;
        if (finalAdminId == null) {
            admin = userRepository.findAll().stream()
                    .filter(u -> "ROLE_ADMIN".equals(u.getRole()) || "ADMIN".equals(u.getRole()))
                    .findFirst()
                    .orElse(null);
            if (admin != null) {
                finalAdminId = admin.getId();
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("message", "No admin user found in database."));
            }
        }
        
        try {
            return ResponseEntity.ok(quotationService.createQuotationFromBooking(request, finalAdminId));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping("/{id}/send-email")
    public ResponseEntity<?> sendEmail(@PathVariable Long id) {
        try {
            quotationService.sendEmail(id);
            return ResponseEntity.ok("Email sent successfully");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error sending email: " + e.getMessage());
        }
    }

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @GetMapping("/public/approve")
    public org.springframework.web.servlet.view.RedirectView approveQuotation(@RequestParam String token) {
        try {
            Quotation quotation = quotationService.approveQuotation(token);
            User client = quotation.getClient();

            // Sign the client straight into their account so the home page loads as
            // "logged in" instead of dropping them on a public/anonymous page.
            String jwt = jwtTokenProvider.generateTokenFromUsername(client.getUsername());

            String redirectUrl = "/index.html?quoteApproved=true"
                    + "&qToken=" + encode(jwt)
                    + "&username=" + encode(client.getUsername())
                    + "&fullName=" + encode(client.getFullName())
                    + "&role=" + encode(client.getRole())
                    + "&email=" + encode(client.getEmail())
                    + "&avatarUrl=" + encode(client.getAvatarUrl())
                    + "&quoteTitle=" + encode(quotation.getTitle());

            return new org.springframework.web.servlet.view.RedirectView(redirectUrl);
        } catch (Exception e) {
            return new org.springframework.web.servlet.view.RedirectView("/index.html?quoteError=" + encode(e.getMessage()));
        }
    }

    private String encode(String value) {
        return URLEncoder.encode(value != null ? value : "", StandardCharsets.UTF_8);
    }

    @PostMapping("/{id}/convert-to-project")
    public ResponseEntity<Project> convertToProject(@PathVariable Long id, @RequestBody(required = false) Map<String, Object> body) {
        return ResponseEntity.ok(quotationService.convertToProject(id, body));
    }

    @GetMapping
    public ResponseEntity<List<Quotation>> getAll() {
        return ResponseEntity.ok(quotationService.getAllQuotations());
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<Quotation> getById(@PathVariable Long id) {
        Quotation q = quotationService.getQuotationById(id);
        if(q != null) return ResponseEntity.ok(q);
        return ResponseEntity.notFound().build();
    }
}