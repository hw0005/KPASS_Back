package com.example.kpass.domain.coupon.repository;

import java.time.LocalDateTime;
import java.util.List;
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
	
	// 쿠폰 코드로 마스터 쿠폰 단건 조회(없을 수도 있으니 Optional)
	@Override
	public Optional<Coupon> findByCode(String code) {
		Coupon result = jpa
				.selectFrom(c)
				.where(c.code.eq(code))
				.fetchOne();
		return Optional.ofNullable(result);
	}

	// 현재시간기준 만료되지 않은 유효한 쿠폰 목록 최신순 조회
	@Override
	public List<Coupon> findAllActioveCoupons() {
		return jpa
				.select(c)
				.from(c)
				.where(c.validUntil.after(LocalDateTime.now()))
				.orderBy(c.id.desc())
				.fetch();
	}
	
	
}
