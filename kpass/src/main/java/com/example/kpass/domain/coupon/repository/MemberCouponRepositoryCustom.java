package com.example.kpass.domain.coupon.repository;

import java.util.Optional;

import com.example.kpass.domain.coupon.entity.MemberCoupon;

public interface MemberCouponRepositoryCustom {
	// 보유 쿠폰(memberCoupn)과 할인 금액이 들어가있는 원본쿠폰(coupon)을 한번에 fetchJoin 조회
	Optional<MemberCoupon> findByIdWithCoupon(Long memberCouponId);
}
