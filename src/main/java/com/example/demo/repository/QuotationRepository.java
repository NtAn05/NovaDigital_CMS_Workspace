package com.example.demo.repository;

import com.example.demo.entity.Quotation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface QuotationRepository extends JpaRepository<Quotation, Long> {
    Optional<Quotation> findByApprovalToken(String approvalToken);
    Optional<Quotation> findByBookingId(Long bookingId);
    Optional<Quotation> findByConvertedProjectId(Long convertedProjectId);
    java.util.List<Quotation> findByClientId(Long clientId);
}

