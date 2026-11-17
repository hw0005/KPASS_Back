package com.example.kpass.domain.coupon.repository;

import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.example.kpass.domain.coupon.entity.Coupon;
import com.example.kpass.domain.coupon.entity.QCoupon;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class CouponRepositoryImpl implements CouponRepositoryCustom {
	private final JPAQueryFactory jpa;
	private final QCoupon c = QCoupon.coupon;
	
	// 쿠폰 코드로 마스터 쿠폰 단건 조회
	@Override
	public Optional<Coupon> findByCode(String code) {
		Coupon result = jpa
				.selectFrom(c)
				.where(c.code.eq(code))
				.fetchOne();
		return Optional.ofNullable(result);
	}
	
}
