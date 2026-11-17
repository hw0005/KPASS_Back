package com.example.kpass.domain.reservation.service;

import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import com.example.kpass.domain.coupon.repository.MemberCouponRepository;
import com.example.kpass.domain.member.repository.MemberRepository;
import com.example.kpass.domain.performance.repository.PerformanceRepository;
import com.example.kpass.domain.performance.repository.PerformanceSeatPriceRepository;
import com.example.kpass.domain.reservation.repository.ReservationRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReservationCommandService {
	// CUD: 예매생성결제, 분산락, 외부토스 API 통신, 재고 차감 == Command
	// @Transactional 사용 시: [락 획득] → [재고 차감] → [락 해제] → (0.01초 틈!) → [DB 커밋]
	// TransactionTemplate 사용 시: [락 획득] → [트랜잭션 시작 → 재고 차감 → DB 커밋 완료] → [락 해제]
	
	private final RedissonClient redisonClient;
	private final TransactionTemplate transactionTemplate;
	private final MemberRepository memberRepository;
	private final PerformanceRepository performanceRepository;
	private final PerformanceSeatPriceRepository performanceSeatPriceRepository;
	private final MemberCouponRepository memberCouponRepository;
	private final ReservationRepository reservationRepository;
}
