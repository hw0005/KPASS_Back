package com.example.kpass.domain.reservation.dto;

import java.time.LocalDateTime;
import java.util.List;

import com.querydsl.core.annotations.QueryProjection;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

public class ReservationDtos {
	
	// 5분 티켓 선점 및 예매 생성요청
	@Getter
	@Setter
	@NoArgsConstructor
	public static class CreateReservationRequest {
		private List<ReservationItemRequest> items;
		private Long memberCouponId; // 적용할 회원 보유 쿠폰 id
		private Integer totalPrice; // 프론트에서 계산된 최종 결제금액
	}
	
	// 예매 상세 항목 요청 dto(단건 바로 결제 or 장바구니 다건 결제)
	@Getter
	@Setter
	@NoArgsConstructor
	public static class ReservationItemRequest {
		private Long cartId; // 장바구니에서 넘어온 cart.id
		private Long performanceId; // 공연 id
		private Long seatPriceId; // 좌성등급id
		private String seatType; // 좌성 드급명
		private Integer quantity; // 예매수
		private Integer unitPrice; // 단가
	}
	
	
	// 5분 선점 예매 생성 응답 dto
	@Getter
	@AllArgsConstructor
	public static class CreateReservationResponse {
		private Long reservationId;
		private String orderUid; // 토스 고유 주문번호
		private String orderName; // 토스에 전달할 주문명
		private Integer ticketQuantity;
		private Integer totalPrice;
		private LocalDateTime expiredAt; // 5분 선점 만료 시각
	}
	
	
	// 토스 결제 최종 승인 요청
	@Getter
	@NoArgsConstructor
	public static class ConfirmPaymentRequest {
		private String paymentKey;
		private String orderId; // 프론트 successUrl 
		private Integer amount;
	}
	
	
	// 토스 결제 최종 승인 응답
	@Getter
	@AllArgsConstructor
	public static class ConfirmPaymentResponse {
		private Long reservationId;
		private String orderUid; // 토스 고유 주문번호
		private String orderName; // 토스에 전달할 주문명
		private Integer amount;
		private String status;
		private String method;
		private LocalDateTime approvedAt;
	}
	
	
	// QueryDSL DTO PRojection 전용 예매 내역 응답
	@Getter
	public static class ReservationHistoryResponse {
		private Long reservationId;
        private String orderUid;
        private String orderName;
        private Integer ticketQuantity;
        private Integer totalPrice;
        private String reservationStatus;
        private LocalDateTime createdAt;
        private String paymentMethod;
        private LocalDateTime approvedAt;
        
        @QueryProjection
		public ReservationHistoryResponse(Long reservationId, String orderUid, String orderName, Integer ticketQuantity,
				Integer totalPrice, String reservationStatus, LocalDateTime createdAt, String paymentMethod,
				LocalDateTime approvedAt) {
        	
			this.reservationId = reservationId;
			this.orderUid = orderUid;
			this.orderName = orderName;
			this.ticketQuantity = ticketQuantity;
			this.totalPrice = totalPrice;
			this.reservationStatus = reservationStatus;
			this.createdAt = createdAt;
			this.paymentMethod = paymentMethod;
			this.approvedAt = approvedAt;
		}
	}
}
