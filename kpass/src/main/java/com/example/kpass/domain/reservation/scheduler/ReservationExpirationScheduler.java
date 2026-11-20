package com.example.kpass.domain.reservation.scheduler;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.example.kpass.domain.reservation.entity.Reservation;
import com.example.kpass.domain.reservation.entity.ReservationStatus;
import com.example.kpass.domain.reservation.repository.ReservationRepository;
import com.example.kpass.domain.reservation.service.ReservationCommandService;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReservationExpirationScheduler {
	
	private final ReservationRepository reservationRepository;
	private final ReservationCommandService reservationCommandService;
	
	// 30초마다 실행: 5분 결제 제한 시간이 지난 PENDING 주문을 찾아 EXPIRED 처리 후 재고, 쿠폰 원복
	@Scheduled(fixedDelay = 30000)
	@Transactional
	public void expireUnpaidReservation() {
		List<Reservation> expiredList = reservationRepository
				.findExpiredReservationsWithDetails(ReservationStatus.PENDING, LocalDateTime.now());
		
		if(expiredList.isEmpty()) {
			return;
		}
		
		for (int i=0; i<expiredList.size(); i++) {
			Reservation reservation = expiredList.get(i);
			reservation.expire();
			reservationCommandService.restoreStockAndCoupon(reservation);
		}
		log.info("만료된 미결제 예매 {}건 자동 취소 및 재고-쿠폰 복구 완료", expiredList.size());
	}
}
