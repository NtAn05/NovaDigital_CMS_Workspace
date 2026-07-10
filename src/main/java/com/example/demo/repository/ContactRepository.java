package com.example.demo.repository;

import com.example.demo.entity.Contact;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ContactRepository extends JpaRepository<Contact, Long> {
    java.util.List<Contact> findByEmailOrderByCreatedAtDesc(String email);
    java.util.List<Contact> findAllByOrderByCreatedAtDesc();

    @org.springframework.transaction.annotation.Transactional
    void deleteAllByEmail(String email);

    @org.springframework.transaction.annotation.Transactional
    void deleteAllByIdInAndEmail(java.util.List<Long> ids, String email);
}
