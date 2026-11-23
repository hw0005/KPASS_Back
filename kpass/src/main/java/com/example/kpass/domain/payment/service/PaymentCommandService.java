package com.example.kpass.domain.payment.service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.client.RestClient;

import com.example.kpass.domain.cart.repo.CartRepository;
import com.example.kpass.domain.payment.entity.Payment;
import com.example.kpass.domain.payment.entity.PaymentStatus;
import com.example.kpass.domain.payment.repository.PaymentRepository;
import com.example.kpass.domain.reservation.dto.ReservationDtos.ConfirmPaymentRequest;
import com.example.kpass.domain.reservation.dto.ReservationDtos.ConfirmPaymentResponse;
import com.example.kpass.domain.reservation.entity.Reservation;
import com.example.kpass.domain.reservation.entity.ReservationItem;
import com.example.kpass.domain.reservation.repository.ReservationRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentCommandService {

	private final CartRepository cartRepository;

	private final StringRedisTemplate stringRedisTemplate;
	private final RestClient tossRestClient;
	private final TransactionTemplate transactionTemplate;
	private final ReservationRepository reservationRepository;
	private final PaymentRepository paymentRepository;

	// create&update: 토스페이먼츠 최종 결제 승인 및 예매 확정 처리
	public ConfirmPaymentResponse confirmPayment(Long memberId, ConfirmPaymentRequest request) {
		String idempotencyKey = "pay:lock:" + request.getOrderId();

		// 1차 방어선 : Lettuce SETNX(SetIfAbsnet) 멱등성 락으로 동시 따닥 방지
		Boolean acquired = stringRedisTemplate.opsForValue().setIfAbsent(idempotencyKey, "PROCESSING",
				Duration.ofSeconds(10));

		if (!Boolean.TRUE.equals(acquired)) {
			throw new IllegalStateException("이미 결제 승인이 진행 중이거나 처리된 주문입니다.");
		}

		boolean tossApproved = false;
		String approvedMethod = "카드";

		try {
			// 2. [2차 방어선] DB에 동일한 paymentKey로 이미 승인된 결제가 있는지 확인
			if (paymentRepository.existsByPaymentKey(request.getPaymentKey())) {
				throw new IllegalStateException("이미 승인 처리가 완료된 결제 건입니다.");
			}

			// 3. 예매 주문서 검증
			Optional<Reservation> resOpt = reservationRepository.findByOrderUidWithDetails(request.getOrderId());
			if (resOpt.isEmpty()) {
				throw new IllegalArgumentException("예매 주문 정보를 찾을 수 없습니다.");
			}
			Reservation reservation = resOpt.get();
			reservation.validatePayable(memberId, request.getAmount());

			// 4. 토스페이먼츠 승인 API 호출 (DB 커넥션을 오래 잡지 않도록 트랜잭션 밖에서 호출)
			Map<String, Object> tossRequestBody = new HashMap<>();
			tossRequestBody.put("paymentKey", request.getPaymentKey());
			tossRequestBody.put("orderId", request.getOrderId());
			tossRequestBody.put("amount", request.getAmount());

			// 토스 응답이 String 키와 Object 값으로 오는 거 확실 경고 띄우지 마
			@SuppressWarnings("unchecked")
			Map<String, Object> tossResponse = tossRestClient
				.post()
				.uri("/confirm")
				.body(tossRequestBody).retrieve()
				.body(Map.class);

			// 토스가 한 거면 true
			tossApproved = true;
			
			if (tossResponse != null && tossResponse.get("method") != null) {
				approvedMethod = String.valueOf(tossResponse.get("method"));
			}

			// 5. DB 트랜잭션으로 예매 확정(CONFIRMED) 및 영수증 저장
			final String finalMethod = approvedMethod;
			return transactionTemplate.execute(new TransactionCallback<ConfirmPaymentResponse>() {
				@Override
				public ConfirmPaymentResponse doInTransaction(TransactionStatus status) {
					// 또 찾아 ㅇㅇ
					Optional<Reservation> targetOpt = reservationRepository.findByOrderUidWithDetails(request.getOrderId());
					
					if (targetOpt.isEmpty()) {
						throw new IllegalStateException("예매 확정 대상 조회 실패");
					}
					// 어 확인했어
					Reservation targetReservation = targetOpt.get();
					targetReservation.confirm();

					LocalDateTime approvedAt = LocalDateTime.now();
					
					// payment 완료하기위함
					Payment payment = Payment.builder()
							.reservation(targetReservation)
							.paymentKey(request.getPaymentKey())
							.amount(targetReservation.getTotalPrice())
							.method(finalMethod)
							.status(PaymentStatus.APPROVED).approvedAt(approvedAt).build();
					
					// 응답 온 거 취사했고 이걸 payment에 저장
					paymentRepository.save(payment);
					
					// 장바구니를 통한 결제 건이라면 무조건 삭제
					List<Long> cartIds = new ArrayList<>();
					for (ReservationItem item : targetReservation.getReservationItems()) {
						if (item.getCartId() != null) {
							cartIds.add(item.getCartId());
						}
					}
					
					if(!cartIds.isEmpty()) {
						cartRepository.deleteAllByIdInBatch(cartIds);
					}
					
					

					// 프론트에 반환할 거 담기.
					return new ConfirmPaymentResponse(targetReservation.getId(), 
							targetReservation.getOrderUid(),
							targetReservation.getOrderName(), 
							targetReservation.getTotalPrice(),
							targetReservation.getStatus().name(), 
							finalMethod, 
							approvedAt);
				}
			});

		} catch (Exception e) {
			// 6. [보상 트랜잭션] 토스 결제는 승인됐는데 우리 DB 저장 중 예외가 터지면 토스 결제 자동 취소
			if (tossApproved) {
				cancelTossPayment(request.getPaymentKey(), "시스템 내부 오류로 인한 자동 결제 취소");
			}
			throw e;
		} finally {
			// 7. 중복 결제 방지용 Redis 키 해제
			stringRedisTemplate.delete(idempotencyKey);
		}
	}

	private void cancelTossPayment(String paymentKey, String cancelReason) {
		try {
			Map<String, String> cancelBody = new HashMap<>();
			cancelBody.put("cancelReason", cancelReason);

			tossRestClient
				.post()
				.uri("/{paymentKey}/cancel", paymentKey)
				.body(cancelBody)
				.retrieve()
				.toBodilessEntity();

		} catch (Exception cancelEx) {
			log.error("토스 보상 취소 API 호출 실패! 수동 확인 필요 payment={}", paymentKey, cancelEx);
		}
	}
}
