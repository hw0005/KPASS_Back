package com.example.kpass.domain.reservation;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.kpass.domain.reservation.dto.ReservationDtos.CreateReservationRequest;
import com.example.kpass.domain.reservation.dto.ReservationDtos.CreateReservationResponse;
import com.example.kpass.domain.reservation.dto.ReservationDtos.ReservationHistoryResponse;
import com.example.kpass.domain.reservation.service.ReservationCommandService;
import com.example.kpass.domain.reservation.service.ReservationQueryService;
import com.example.kpass.global.auth.LoginMember;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/reservation")
@RequiredArgsConstructor
public class ReservationController {
	
	private final ReservationCommandService reservationCommandService;
	private final ReservationQueryService reservationQueryService;
	
	// create: 5분 티켓 선점 주문 생성
	@PostMapping
	public ResponseEntity<CreateReservationResponse> createHoldReservation(
			@LoginMember Long memberId,
			@RequestBody CreateReservationRequest request) {
		
		CreateReservationResponse response = reservationCommandService.createHoldResponse(memberId, request);
		return ResponseEntity.ok(response);
	}
	
	// update: 사용자 결제 취소 시 선점 해제 및 재고 쿠폰 원복
	@PostMapping("/{orderUid}/cancel")
	public ResponseEntity<Void> cancelHolReservation (
			@LoginMember Long memberId,
			@PathVariable("orderUid") String orderUid) {
		
		reservationCommandService.cancelHoldRservation(memberId, orderUid);
		return ResponseEntity.ok().build();
	}
	
	// Read: 마이페이지 예매 확정 내역 조회
	@GetMapping("/me")
	public ResponseEntity<Page<ReservationHistoryResponse>> getMyConfirmedReservations(
			@LoginMember Long memberId,
			@RequestParam(name = "page", defaultValue = "0") int page,
			@RequestParam(name = "size", defaultValue = "10") int size) {
		
		Pageable pageable = PageRequest.of(page, size);
		Page<ReservationHistoryResponse> result = reservationQueryService.getMyConfirmedReservation(memberId, pageable);
		return ResponseEntity.ok(result);
	}
	
	
}
