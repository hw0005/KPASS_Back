package com.example.kpass.domain.payment;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.kpass.domain.payment.service.PaymentCommandService;
import com.example.kpass.domain.reservation.dto.ReservationDtos.ConfirmPaymentRequest;
import com.example.kpass.domain.reservation.dto.ReservationDtos.ConfirmPaymentResponse;
import com.example.kpass.global.auth.LoginMember;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
public class PaymentController {

	private final PaymentCommandService paymentCommandService;

	// create / update : 토스 페이먼츠 결제 최종 승인 API
	@PostMapping("/confirm")
	public ResponseEntity<ConfirmPaymentResponse> confirmPayment(
			@LoginMember Long memberId,
			@RequestBody ConfirmPaymentRequest request) {
		
		ConfirmPaymentResponse response = paymentCommandService.confirmPayment(memberId, request);
		return ResponseEntity.ok(response);

	}

}
