package com.example.demo.controller;

import com.example.demo.entity.Feedback;
import com.example.demo.repository.FeedbackRepository;
import com.example.demo.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/feedback")
public class FeedbackController {

    @Autowired
    private FeedbackRepository feedbackRepository;

    @Autowired
    private EmailService emailService;

    @PostMapping
    public ResponseEntity<?> submitFeedback(@RequestBody Map<String, String> body) {
        String name = body.getOrDefault("name", "").trim();
        String email = body.getOrDefault("email", "").trim();
        String category = body.getOrDefault("category", "General Feedback").trim();
        String message = body.getOrDefault("message", "").trim();

        if (name.isEmpty() || email.isEmpty() || message.isEmpty()) {
            Map<String, Object> err = new HashMap<>();
            err.put("success", false);
            err.put("message", "Name, email, and message are required.");
            return ResponseEntity.badRequest().body(err);
        }

        Feedback feedback = new Feedback(name, email, category, message);
        Feedback saved = feedbackRepository.save(feedback);

        // Send automated confirmation email matching User_FeedBack_Instruction.md specification
        emailService.sendFeedbackConfirmationEmail(email, name, message);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "We value your feedback and will get back to you soon!");
        response.put("id", saved.getId());

        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<?> getAllFeedbacks() {
        return ResponseEntity.ok(feedbackRepository.findAllByOrderByCreatedAtDesc());
    }
}
