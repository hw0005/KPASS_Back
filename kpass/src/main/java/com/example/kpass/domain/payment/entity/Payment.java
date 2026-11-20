package com.example.kpass.domain.payment.entity;

import java.time.LocalDateTime;

import com.example.kpass.domain.reservation.entity.Reservation;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name="payment")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Payment {
	
	// 토스페이먼츠 최종 승인이 완료된 결제 영수증 정보를 Reservation과 1:1로 매핑해 저장
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name="id")
	private Long id;
	
	@OneToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="reservationId", nullable = false, unique = true)
	private Reservation reservation;
	
	// 결제 중복 방지
	@Column(name="paymentKey", nullable = false, unique=true, length=100)
	private String paymentKey;
	
	@Column(name="amount", nullable=false)
	private Integer amount;
	
	@Column(name="method", length=30)
	private String method;
	
	@Enumerated(EnumType.STRING)
	@Column(name="status", nullable = false, length=20)
	private PaymentStatus status;
	
	@Column(name="approvedAt")
	private LocalDateTime approvedAt;
	
	@Column(name="createdAt", nullable=false)
	private LocalDateTime createdAt;

	@Builder
	public Payment(Reservation reservation, String paymentKey, Integer amount, String method,
			PaymentStatus status, LocalDateTime approvedAt) {
		this.reservation = reservation;
		this.paymentKey = paymentKey;
		this.amount = amount;
		this.method = method;
		this.status = status;
		this.approvedAt = approvedAt;
	}
	
	// 결제 취소 or 반영실패
	public void refund() {
		this.status = PaymentStatus.REFUNDED;
	}
	
	@PrePersist
	public void prePersist() {
	    if (this.createdAt == null) {
	        this.createdAt = LocalDateTime.now();
	    }

	}
	
}
