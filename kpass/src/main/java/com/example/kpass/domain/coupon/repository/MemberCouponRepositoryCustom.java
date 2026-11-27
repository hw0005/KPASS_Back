package com.example.kpass.domain.coupon.repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import com.example.kpass.domain.coupon.entity.MemberCoupon;

public interface MemberCouponRepositoryCustom {
	// 보유 쿠폰(memberCoupn)과 할인 금액이 들어가있는 원본쿠폰(coupon)을 한번에 fetchJoin 조회
	Optional<MemberCoupon> findByIdWithCoupon(Long memberCouponId);
	
	// 결제 시 사용가능한 것 조회
	List<MemberCoupon> findUsableCouponsByMemberId(Long memberId);
	
	// 헤더 드롭다운용: 내가 이미 발급받은 쿠폰 id들만 뽑아서(중복없는 set) 반환 조회
	Set<Long> findIssuedCouponIdsByMemberId(Long memerId);
	
	// 전체컬럼 다 가져오지 않고 숫자만 1건 조회해서 null이 아니면 조회함.
	boolean existsByMemberIdAndCouponId(Long memberId, Long couponId);
	
}
