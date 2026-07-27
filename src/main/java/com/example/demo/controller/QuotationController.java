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

    // TODO: Ideally inject Authentication to get adminUserId. Using static/path var for simplicity if no auth context
    @PostMapping("/from-booking/{bookingId}")
    public ResponseEntity<Quotation> createFromBooking(
            @PathVariable Long bookingId,
            @RequestBody QuotationRequest request,
            @RequestParam(defaultValue = "1") Long adminId) { // dummy admin ID
        request.setBookingId(bookingId);
        return ResponseEntity.ok(quotationService.createQuotationFromBooking(request, adminId));
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
    public ResponseEntity<Project> convertToProject(@PathVariable Long id) {
        return ResponseEntity.ok(quotationService.convertToProject(id));
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