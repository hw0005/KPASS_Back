package com.example.kpass.domain.payment.repository;

import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.example.kpass.domain.payment.entity.Payment;
import com.example.kpass.domain.payment.entity.QPayment;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class PaymentRepositoryImpl implements PaymentRepositoryCustom {
	
	private final JPAQueryFactory jpa;
	private final QPayment pay = QPayment.payment;
	
	 // 예매 id로 단건 조회
	@Override
	public Optional<Payment> findByReservationId(Long reservationId){
		Payment result = jpa
				.selectFrom(pay)
				.where(pay.reservation.id.eq(reservationId))
				.fetchOne();
			
			return Optional.ofNullable(result);
	}
	
	
	// 동일한 토스 paymentKey로 이미 저장된 결제가 있는지 중복 여부 확인
	@Override
	public boolean existsByPaymentKey(String paymentKey) {
		Integer fetchOne = jpa
				.selectOne()
				.from(pay)
				.where(pay.paymentKey.eq(paymentKey))
				.fetchFirst();
				
		return fetchOne != null;		
	}
}
