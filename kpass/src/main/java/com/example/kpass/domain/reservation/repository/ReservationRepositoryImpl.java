package com.example.kpass.domain.reservation.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;

import com.example.kpass.domain.coupon.entity.QMemberCoupon;
import com.example.kpass.domain.member.entity.QMember;
import com.example.kpass.domain.payment.entity.QPayment;
import com.example.kpass.domain.performance.entity.QPerformance;
import com.example.kpass.domain.performance.entity.QPerformanceSeatPrice;
import com.example.kpass.domain.reservation.dto.QReservationDtos_ReservationHistoryResponse;
import com.example.kpass.domain.reservation.dto.ReservationDtos.ReservationHistoryResponse;
import com.example.kpass.domain.reservation.entity.QReservation;
import com.example.kpass.domain.reservation.entity.QReservationItem;
import com.example.kpass.domain.reservation.entity.Reservation;
import com.example.kpass.domain.reservation.entity.ReservationStatus;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class ReservationRepositoryImpl implements ReservationRepositoryCustom{
	
	private final JPAQueryFactory jpa;
	private final QReservation r = QReservation.reservation;
	private final QReservationItem ri = QReservationItem.reservationItem;
	private final QMember m = QMember.member;
	private final QMemberCoupon mc = QMemberCoupon.memberCoupon;
	private final QPerformance p = QPerformance.performance;
	private final QPerformanceSeatPrice sp = QPerformanceSeatPrice.performanceSeatPrice;
	private final QPayment pay = QPayment.payment;
	
	// CUD: 주문번호(orderUid)로 예매, 회원, 쿠폰, 예매항목, 좌석/공연 정보 한번에 fetchJoin 조회
	@Override
	public Optional<Reservation> findByOrderUidWithDetails(String orderUid) {
		Reservation result = jpa
				.selectFrom(r)
				.distinct()
				.join(r.member, m).fetchJoin()
				.leftJoin(r.memberCoupon, mc).fetchJoin()
				.leftJoin(r.reservationItems, ri).fetchJoin()
				.leftJoin(ri.seatPrice, sp).fetchJoin()
				.leftJoin(ri.performance, p).fetchJoin()
				.where(r.orderUid.eq(orderUid))
				.fetchOne();
		
		return Optional.ofNullable(result);
	}
	
	
	// CUD: 결제대기(Pending) 상태이면서 5분 만료시간(expiredAt)이 지난 예매 목록 조회
	@Override
	public List<Reservation> findExpiredReservationsWithDetails(ReservationStatus status,
			LocalDateTime now) {
		return jpa
				.selectFrom(r)
				.distinct()
				.leftJoin(r.memberCoupon, mc).fetchJoin()
				.leftJoin(r.reservationItems, ri).fetchJoin()
				.leftJoin(ri.seatPrice, sp).fetchJoin()
				.leftJoin(ri.performance, p).fetchJoin()
				.where(
						r.status.eq(status),
						r.expiredAt.lt(now)
				)
				.fetch();
		
	}
	
	
	//READ: 마이페이지 확정 예매 내역 조회
	@Override
	public Page<ReservationHistoryResponse> findConfirmedHistoryByMemberId(Long memberId, Pageable pageable) {
		// 엔티티 전체를 영속성 컨텍스트 안 올림 필요한 것만 조회
		List<ReservationHistoryResponse> content = jpa
				.select(new QReservationDtos_ReservationHistoryResponse(
						r.id,
						r.orderUid,
						r.orderName,
						r.ticketQuantity,
						r.totalPrice,
						r.status.stringValue(),
						r.createdAt,
						pay.method,
						pay.approvedAt
						))
				.from(r)
				.leftJoin(pay).on(pay.reservation.id.eq(r.id))
				.where(
						r.member.id.eq(memberId),
						r.status.eq(ReservationStatus.CONFIRMED)
				)
				.orderBy(r.createdAt.desc())
				.offset(pageable.getOffset())
				.limit(pageable.getPageSize())
				.fetch();
		
		// 조인 제거한 가벼운 count query
		JPAQuery<Long> countQuery = jpa
				.select(r.count())
				.from(r)
				.where(
					r.member.id.eq(memberId),
					r.status.eq(ReservationStatus.CONFIRMED)
				);
		
		// 페이지로 데이터 넘기기
		return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchOne);
	}
	
	
}
