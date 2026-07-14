package com.example.demo.controller;

import com.example.demo.dto.ContactRequest;
import com.example.demo.entity.Contact;
import com.example.demo.service.ContactService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/contacts")
public class ContactController {

    @Autowired
    private ContactService contactService;

    @Autowired
    private com.example.demo.repository.UserRepository userRepository;

    @PostMapping
    public ResponseEntity<?> createContact(@Valid @RequestBody ContactRequest request) {
        Contact contact = new Contact();
        contact.setName(request.getName());
        contact.setEmail(request.getEmail());
        contact.setTitle(request.getTitle());
        contact.setContent(request.getContent());
        
        Contact savedContact = contactService.createContact(contact);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Contact message saved successfully!");
        response.put("id", savedContact.getId());
        
        return ResponseEntity.ok(response);
    }

    @org.springframework.web.bind.annotation.GetMapping
    public ResponseEntity<?> getAllContacts() {
        return ResponseEntity.ok(contactService.getAllContacts());
    }

    @PostMapping("/{id}/reply")
    public ResponseEntity<?> replyToContact(@PathVariable Long id, @RequestBody java.util.Map<String, String> body) {
        String replyMessage = body.get("message");
        java.util.Map<String, Object> response = new HashMap<>();
        
        try {
            Contact contact = contactService.getContactById(id);
            contact.setStatus("DONE");
            contact.setReply(replyMessage);
            contact.setRepliedAt(java.time.LocalDateTime.now());
            contactService.updateContact(contact);
            
            // Mock sending email
            System.out.println("----------------------------------------");
            System.out.println("SENDING EMAIL TO: " + contact.getEmail());
            System.out.println("SUBJECT: Re: " + contact.getTitle());
            System.out.println("CONTENT: " + replyMessage);
            System.out.println("----------------------------------------");
            
            response.put("success", true);
            response.put("message", "Reply sent successfully!");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    // API to get contacts of the currently logged-in user (based on the email query param, or JWT in the future)
    @GetMapping("/my")
    public ResponseEntity<?> getMyContacts(@RequestParam String email) {
        java.util.List<Contact> contacts = contactService.getContactsByEmail(email);
        return ResponseEntity.ok(contacts);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteContact(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();
        try {
            Contact contact = contactService.getContactById(id);
            com.example.demo.entity.User currentUser = getCurrentUser();
            
            // Check authorization: ADMIN, MEMBER or the owner of the message
            boolean isAdminOrMember = "ROLE_ADMIN".equalsIgnoreCase(currentUser.getRole()) 
                    || "ROLE_MEMBER".equalsIgnoreCase(currentUser.getRole())
                    || "Team_Member".equalsIgnoreCase(currentUser.getRole());
            boolean isOwner = currentUser.getEmail().equalsIgnoreCase(contact.getEmail());
            
            if (!isAdminOrMember && !isOwner) {
                response.put("success", false);
                response.put("message", "You are not authorized to delete this message.");
                return ResponseEntity.status(403).body(response);
            }
            
            contactService.deleteContact(id);
            response.put("success", true);
            response.put("message", "Contact message deleted successfully!");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @DeleteMapping("/my")
    public ResponseEntity<?> deleteMyContacts(@RequestParam(required = false) java.util.List<Long> ids) {
        Map<String, Object> response = new HashMap<>();
        try {
            com.example.demo.entity.User currentUser = getCurrentUser();
            String email = currentUser.getEmail();
            
            if (ids == null || ids.isEmpty()) {
                contactService.deleteContactsByEmail(email);
                response.put("message", "All your contact messages have been deleted.");
            } else {
                contactService.deleteContactsByIdsAndEmail(ids, email);
                response.put("message", "Selected contact messages have been deleted.");
            }
            
            response.put("success", true);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    private com.example.demo.entity.User getCurrentUser() {
        org.springframework.security.core.Authentication auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new RuntimeException("Unauthorized");
        }
        String username = auth.getName();
        return userRepository.findByUsernameOrEmail(username, username)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
}
