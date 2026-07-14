package com.example.demo.service;

import com.example.demo.entity.Contact;
import com.example.demo.repository.ContactRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.demo.audit.annotation.Auditable;

@Service
public class ContactService {
    @Autowired
    private ContactRepository contactRepository;

    public Contact saveContact(Contact contact) {
        return contactRepository.save(contact);
    }

    @Auditable(action = "CREATE", table = "contacts")
    public Contact createContact(Contact contact) {
        return contactRepository.save(contact);
    }

    @Auditable(action = "UPDATE", table = "contacts")
    public Contact updateContact(Contact contact) {
        return contactRepository.save(contact);
    }

    public java.util.List<Contact> getAllContacts() {
        return contactRepository.findAllByOrderByCreatedAtDesc();
    }

    public Contact getContactById(Long id) {
        return contactRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Message not found with id = " + id));
    }

    public java.util.List<Contact> getContactsByEmail(String email) {
        return contactRepository.findByEmailOrderByCreatedAtDesc(email);
    }

    public void deleteContact(Long id) {
        contactRepository.deleteById(id);
    }

    public void deleteContactsByEmail(String email) {
        contactRepository.deleteAllByEmail(email);
    }

    public void deleteContactsByIdsAndEmail(java.util.List<Long> ids, String email) {
        contactRepository.deleteAllByIdInAndEmail(ids, email);
    }
}
