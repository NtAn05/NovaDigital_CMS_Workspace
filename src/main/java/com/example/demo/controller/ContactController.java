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

    @PostMapping
    public ResponseEntity<?> createContact(@Valid @RequestBody ContactRequest request) {
        Contact contact = new Contact();
        contact.setName(request.getName());
        contact.setEmail(request.getEmail());
        contact.setTitle(request.getTitle());
        contact.setContent(request.getContent());
        
        Contact savedContact = contactService.saveContact(contact);
        
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
            contactService.saveContact(contact);
            
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
}
