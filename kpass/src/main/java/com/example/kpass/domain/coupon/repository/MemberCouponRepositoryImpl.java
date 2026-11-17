package com.example.kpass.domain.coupon.repository;

import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.example.kpass.domain.coupon.entity.MemberCoupon;
import com.example.kpass.domain.coupon.entity.QCoupon;
import com.example.kpass.domain.coupon.entity.QMemberCoupon;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class MemberCouponRepositoryImpl implements MemberCouponRepositoryCustom{
	
	private final JPAQueryFactory jpa;
	private final QMemberCoupon mc = QMemberCoupon.memberCoupon;
	private final QCoupon c = QCoupon.coupon;
	
	// // 보유 쿠폰(memberCoupon)과 할인 금액이 들어가있는 원본쿠폰(coupon)을 한번에 fetchJoin 조회
	@Override
	public Optional<MemberCoupon> findByIdWithCoupon(Long memberCouponId) {
		MemberCoupon result = jpa
				.selectFrom(mc)
                .join(mc.coupon, c).fetchJoin()
                .where(mc.id.eq(memberCouponId))
                .fetchOne();

        return Optional.ofNullable(result);
			
				
	}

}
