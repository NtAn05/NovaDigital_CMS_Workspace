package com.example.demo.repository;

import com.example.demo.entity.PaymentTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PaymentTransactionRepository extends JpaRepository<PaymentTransaction, Long> {
    Optional<PaymentTransaction> findByOrderCode(Long orderCode);
    Optional<PaymentTransaction> findByAppointmentIdAndStatus(Long appointmentId, String status);
    Optional<PaymentTransaction> findByMilestoneIdAndStatus(Long milestoneId, String status);
}
