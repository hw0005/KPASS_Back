package com.example.kpass.domain.reservation.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

import com.example.kpass.domain.cart.CartController;
import com.example.kpass.domain.cart.repo.CartRepository;
import com.example.kpass.domain.coupon.entity.MemberCoupon;
import com.example.kpass.domain.coupon.repository.MemberCouponRepository;
import com.example.kpass.domain.member.entity.Member;
import com.example.kpass.domain.member.repository.MemberRepository;
import com.example.kpass.domain.performance.entity.Performance;
import com.example.kpass.domain.performance.entity.PerformanceSeatPrice;
import com.example.kpass.domain.performance.repository.PerformanceRepository;
import com.example.kpass.domain.performance.repository.PerformanceSeatPriceRepository;
import com.example.kpass.domain.reservation.dto.ReservationDtos.CreateReservationRequest;
import com.example.kpass.domain.reservation.dto.ReservationDtos.CreateReservationResponse;
import com.example.kpass.domain.reservation.dto.ReservationDtos.ReservationItemRequest;
import com.example.kpass.domain.reservation.entity.Reservation;
import com.example.kpass.domain.reservation.entity.ReservationItem;
import com.example.kpass.domain.reservation.repository.ReservationRepository;
import com.example.kpass.domain.test.repository.TestRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReservationCommandService {


    private final CartController cartController;
	// CUD: 예매생성결제, 분산락, 외부토스 API 통신, 재고 차감 == Command
	// @Transactional 사용 시: [락 획득] → [재고 차감] → [락 해제] → (0.01초 틈) → [DB 커밋]
	// TransactionTemplate 사용 시: [락 획득] → [트랜잭션 시작 → 재고 차감 → DB 커밋 완료] → [락 해제]
	
    /**
     * 즉 여기는, Redisson(줄세우기)이다. 100명 예매했을 때 50명밖에 안남았을 때 줄세워서 상ㅇ위 50명만
     * 예약되게 하기
     */
	private final RedissonClient redissonClient;
	private final TransactionTemplate transactionTemplate;
	private final MemberRepository memberRepository;
	private final PerformanceRepository performanceRepository;
	private final PerformanceSeatPriceRepository performanceSeatPriceRepository;
	private final MemberCouponRepository memberCouponRepository;
	private final ReservationRepository reservationRepository;
	private final CartRepository cartRepository;


	// create: Redisson MultiLock으로 동시성 제어, 5분 티켓 선점(Status: PENDING).
	public CreateReservationResponse createHoldResponse(Long memberId, CreateReservationRequest request) {
		// 5분 티켓 선점 및 예매 "요청"
		// items;
		// memberCouponId; // 적용할 회원 보유 쿠폰 id
		// totalPrice; // 프론트에서 계산된 최종 결제금액
		
		
		
		List<ReservationItemRequest> items = request.getItems();
		// 예매 "상세" 항목 "요청" dto
		//		cartId; // 장바구니에서 넘어온 cart.id
		//		performanceId; // 공연 id
		//		seatPriceId; // 좌성등급id
		//		seatType; // 좌성 드급명
		//		quantity; // 예매수
		//		unitPrice
		
		if(items == null || items.isEmpty()) {
			throw new IllegalArgumentException("예매할 공연 항목 없음");
		}
		
		// 1. 락 범위 최소화하기 위해 좌석 등급 id / 단일 공연 id 수집
		List<Long> seatPriceIds = new ArrayList<>();
		List<Long> performanceOnlyIds = new ArrayList<>();
		
		for (int i=0; i<items.size(); i++) {
			// 단건 뽑기
			ReservationItemRequest item = items.get(i);
			if(item.getSeatPriceId() != null) {
				// 같은 좌석번호가 아니면
				if (!seatPriceIds.contains(item.getSeatPriceId())) {
					// 저장
					seatPriceIds.add(item.getSeatPriceId());
				}
			}
			else if (item.getPerformanceId() != null) {
				if(!performanceOnlyIds.contains(item.getPerformanceId())) {
					performanceOnlyIds.add(item.getPerformanceId());
				}
			}
		}
		
		// 2. 다건 예매 시 데드락(교착 상태) 방지 위한 id 오름차순 정렬
		Collections.sort(seatPriceIds);
		Collections.sort(performanceOnlyIds);
		
		// 3. 정렬된 id 뽑아가며 분산락 키 생성 후 MultiLock 하나로 묶기
		List<String> lockKeys = new ArrayList<>();
		for (int i=0; i<seatPriceIds.size(); i++) {
			lockKeys.add("lock:seat:" + seatPriceIds.get(i));
		}
		for (int i=0; i<performanceOnlyIds.size(); i++) {
			lockKeys.add("lock:performance:" + performanceOnlyIds.get(i));
		}
		
		// 적용할 쿠폰 있다면
		if (request.getMemberCouponId() != null) {
			lockKeys.add("lock:memberCoupon:" + request.getMemberCouponId());
		}
		if (lockKeys.isEmpty()) {
			throw new IllegalArgumentException("예매 대상 공연 ID(performanceId)가 누락되었습니다.");
		}
		
		// 레디슨 락 걸 배열
		RLock[] lockArray = new RLock[lockKeys.size()];
		for (int i=0; i<lockArray.length; i++ ) {
			lockArray[i] = redissonClient.getLock(lockKeys.get(i));
		}
		
		RLock multiLock = redissonClient.getMultiLock(lockArray);
		
		try {
			// 4. 분산락 획득 시도(최대 5초대기, 획득 후 3초동안 점유)
			// 몇 초동안 시도,  몇 초 동안 점유, 락 획득
			boolean isLocked = multiLock.tryLock(5, 3, TimeUnit.SECONDS);
			
			if(!isLocked) {
				throw new IllegalArgumentException("현재 예매 요청이 몰려 지연되고 있습니다. 잠시 후 시도해주세요");
			}
			
			// 5. 락 내부에서 DB 트랜잭션 실행(DB 커밋 끝난 후 FINALLY에서 락 해제)
			return transactionTemplate.execute(new TransactionCallback<CreateReservationResponse>() {
				@Override
				public CreateReservationResponse doInTransaction(TransactionStatus status) {
					return executeReservationTransaction(memberId, request);
				}
            });
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new IllegalStateException("예매 처리 중 인터럽트 발생");
		}
		finally {
			// 6. DB 커밋 완료 안전하게 해제
			try {
				multiLock.unlock();
				
			} catch (IllegalMonitorStateException ignored) {
				
			}
		}
	}
	
	
	// 실제 트랜잭션 내부에서 실행되는 회원, 쿠폰 검증, 재고 차감, 주문서 저장 로직
	private CreateReservationResponse executeReservationTransaction(Long memberId, CreateReservationRequest request) {
//		5분 선점 예매 생성 "응답" dto
		
//		reservationId;
//		orderUid; // 토스 고유 주문번호
//		orderName; // 토스에 전달할 주문명
//		ticketQuantity;
//		totalPrice;
//		expiredAt; // 5분 선점 만료 시각
		
		
		// 1. 예매 회원 조회
		Optional<Member> memberOpt = memberRepository.findById(memberId);
		if(memberOpt.isEmpty()) {
			throw new IllegalArgumentException("존재하지 않는 회원입니다.");
		}
		Member member = memberOpt.get();
		
		// 2. 쿠폰을 선택한 경우 본인 쿠폰 여부 및 미사용 검증 후 선점 사용
		// isUsed = true == 사용한 거임
		MemberCoupon memberCoupon = null;
		int discountAmount = 0;
		// 쿠폰요청 들어온 거면
		if(request.getMemberCouponId() != null) {
			// 번호로 가져오고
			Optional<MemberCoupon> mcOpt = memberCouponRepository.findByIdWithCoupon(request.getMemberCouponId());
			if(mcOpt.isEmpty()) {
				throw new IllegalArgumentException("존재하지 않는 보유 쿠폰입니다.");
			}
			memberCoupon = mcOpt.get();
			
			// 확인 후 사용완료로 바꾸기
			memberCoupon.use(memberId);
			discountAmount = memberCoupon.getCoupon().getDiscountAmount();
		}
		
		
		// 3. 현재 시각 기준 +5분 결제 만료시간을 가진 PENDING 주문서 생성
		LocalDateTime expiredAt = LocalDateTime.now().plusMinutes(5);
		Reservation reservation = Reservation.builder()
				.member(member)
				.performance(null)
				.memberCoupon(memberCoupon)
				.orderName("K-PASS 공연 예매")
				.ticketQuantity(0)
				.totalPrice(0)
				.expiredAt(expiredAt)
				.build();
		
		// 4. 예매 항목별 실제 DB 단가 확인 및 재고 차감
		List<ReservationItemRequest> items = request.getItems();
		Performance firstPerformance = null;
		int totalTicketQuantity = 0;
		int rawTotalPrice = 0;
		
		for (int i=0; i<items.size(); i++) {
			// 단 건 가져오기
			ReservationItemRequest itemReq = items.get(i);
			
			Optional<Performance> perfOpt = performanceRepository.findById(itemReq.getPerformanceId());
			if(perfOpt.isEmpty()) {
				throw new IllegalArgumentException("존재하지 않는 공연입니다.");
			}
			Performance performance = perfOpt.get();
			if (i==0) {
				firstPerformance = performance;
			}
			
			PerformanceSeatPrice seatPrice = null;
			int actualUnitPrice;
			String actualSeatType;
			
			// 좌석등급(VIP/R/S 등)이 있는 공연이면 좌석 등급 재고 차감, 단일석이면 공연 전체 재고 차감
			if(itemReq.getSeatPriceId() != null) {
				Optional<PerformanceSeatPrice> seatOpt = performanceSeatPriceRepository.findById(itemReq.getSeatPriceId());
				
				if(seatOpt.isEmpty()) {
					throw new IllegalArgumentException("존재하지 않는 좌석 등급이니다.");
				}
				
				seatPrice = seatOpt.get();
				seatPrice.decreaseStock(itemReq.getQuantity());
				
				// 진짜 단건 가격과 타입
				actualUnitPrice = seatPrice.getPrice();
				actualSeatType = seatPrice.getSeatType();
			}
			// 아니고 그냥 단일석이면
			else {
				performance.decreaseStockQuantity(itemReq.getQuantity());
				actualUnitPrice = performance.getPrice();
				actualSeatType = (itemReq.getSeatType() != null) ? itemReq.getSeatType(): "일반석";
			}
			
			// 총 갯수와 가격
			totalTicketQuantity += itemReq.getQuantity();
			rawTotalPrice += actualUnitPrice * itemReq.getQuantity();
			
			ReservationItem reservationItem = ReservationItem.builder()
					.performance(performance)
					.seatPrice(seatPrice)
					.cartId(itemReq.getCartId())
					.seatType(actualSeatType)
					.quantity(itemReq.getQuantity())
					.unitPrice(actualUnitPrice)
					.build();
			
			// 예매 추가
			reservation.addReservationItem(reservationItem);
		}
		
		// 5. 쿠폰 할인 차감 후 프론트 요청 금액과 서버 계산 금액 일치 여부 검증
		int finalTotalPrice = rawTotalPrice - discountAmount;
		if(finalTotalPrice < 0) {
			finalTotalPrice = 0;
		}
		
		if (!Objects.equals(finalTotalPrice, request.getTotalPrice())) {
			throw new IllegalArgumentException("요청 금액과 서버 계산 금액이 일치하지 않습니다.");
		}
		
		// 6. 주문명 요약 및 DB 최종 저장
		String orderName = (firstPerformance != null) ? firstPerformance.getTitle() : "K-PASS 공연 티켓";
		if(items.size() > 1) {
			orderName = orderName + " 외 " + (items.size() - 1) + "건";
		}
		
		// 저장하기
		reservation.updateOrderSummary(firstPerformance, orderName, totalTicketQuantity, finalTotalPrice);
		
		
		// 최종 결제 금액이 0원(무료/전액쿠폰)이면 즉시 삭제 -> PG사 안 가도 된다 이말
		if (finalTotalPrice == 0) {
			reservation.confirm();
			
			List<Long> cartIds = new ArrayList<>();
			List<ReservationItem> reservationItems = reservation.getReservationItems();
			for (int i = 0; i < reservationItems.size(); i++) {
				Long cartId = reservationItems.get(i).getCartId();
				if (cartId != null) {
					cartIds.add(cartId);
				}
			}
			
			if (!cartIds.isEmpty()) {
				cartRepository.deleteAllByIdInBatch(cartIds);
			}
		}
		
		
		Reservation savedReservation = reservationRepository.save(reservation);
		
		return new CreateReservationResponse(
				savedReservation.getId(),
				savedReservation.getOrderUid(),
				savedReservation.getOrderName(), 
				savedReservation.getTicketQuantity(),
				savedReservation.getTotalPrice(),
				savedReservation.getExpiredAt()
		);
	}
	
	// update: 사용자가 결제창에서 이탈하거나 직접 취소할 때 선점 해제 및 재고,쿠폰 원복
	public void cancelHoldRservation(Long memberId, String orderUid) {
		
		transactionTemplate.execute(new TransactionCallbackWithoutResult() {
            @Override
            protected void doInTransactionWithoutResult(TransactionStatus status) {
                Optional<Reservation> resOpt = reservationRepository.findByOrderUidWithDetails(orderUid);
                if (resOpt.isEmpty()) {
                    throw new IllegalArgumentException("존재하지 않는 예매 내역입니다.");
                }
                Reservation reservation = resOpt.get();

                if (!reservation.getMember().getId().equals(memberId)) {
                    throw new IllegalArgumentException("본인의 예매 건만 취소할 수 있습니다.");
                }

                reservation.cancel();
                restoreStockAndCoupon(reservation);
            }
        });
	}
	
	// 공통복구 로직: 취소 또는 5분 만료 시 차감 했던 좌석 재고와 쿠폰 원복
	public void restoreStockAndCoupon(Reservation reservation) {
		List<ReservationItem> items = reservation.getReservationItems();
		for (int i=0; i<items.size(); i++) {
			ReservationItem item = items.get(i);
			
			if(item.getSeatPrice() != null ) {
				item.getSeatPrice().increaseStcok(item.getQuantity());
			}
			else if (item.getPerformance() != null) {
				item.getPerformance().increaseStockQuantity(item.getQuantity());
			}
		}
		
		if(reservation.getMemberCoupon() != null) {
			reservation.getMemberCoupon().restore();
		}
	}
}
