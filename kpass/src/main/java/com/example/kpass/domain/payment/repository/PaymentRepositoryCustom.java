package com.example.kpass.domain.payment.repository;

import java.util.Optional;

import com.example.kpass.domain.payment.entity.Payment;

public interface PaymentRepositoryCustom {
	
	// 에먜 id로 결제 영수증 단건 조회
	Optional<Payment> findByReservationId(Long reservationId);
	
	// 동일한 토스 paymentKey로 이미 저장된 결제가 있는지 중복 여부 확인
	boolean existsByPaymentKey(String paymentKey);

}
