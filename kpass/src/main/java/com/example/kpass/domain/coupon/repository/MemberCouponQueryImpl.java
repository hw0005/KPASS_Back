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
public class MemberCouponQueryImpl implements MemberCouponRepositoryCustom{
	
	private final JPAQueryFactory jpa;
	private final QMemberCoupon mc = QMemberCoupon.memberCoupon;
	private final QCoupon c = QCoupon.coupon;
	
	// 보유 쿠폰과 마스터 쿠폰 마스터(할인 금액)을 조회
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
