package com.example.kpass.domain.coupon.repository;

import java.util.Optional;

import com.example.kpass.domain.coupon.entity.MemberCoupon;

public interface MemberCouponRepositoryCustom {
	Optional<MemberCoupon> findByIdWithCoupon(Long memberCouponId);
}
