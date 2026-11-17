package com.example.kpass.domain.payment.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.kpass.domain.payment.entity.Payment;

public interface PaymentRepository extends JpaRepository<Payment, Long>, PaymentRepositoryCustom {

}
