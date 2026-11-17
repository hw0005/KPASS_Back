package com.example.kpass.domain.coupon.repository;

import java.util.Optional;

import com.example.kpass.domain.coupon.entity.Coupon;

public interface CouponRepositoryCustom {
	// 쿠폰 단 건
	Optional<Coupon> findByCode(String code);
}
