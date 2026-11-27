package com.example.kpass.domain.coupon.repository;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

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
	
	// 결제창 드롭다운: 아직 안 썻고, 만료일 안 지남
	@Override
	public List<MemberCoupon> findUsableCouponsByMemberId(Long memberId) {
		return jpa
				.selectFrom(mc)
				.join(mc.coupon, c).fetchJoin()
				.where(
						mc.member.id.eq(memberId),
						mc.isUsed.isFalse(),
						c.validUntil.after(LocalDateTime.now())
						)
				.orderBy(c.discountAmount.desc(), mc.id.desc())
				.fetch();
	}
	
	// 헤더 드롭다운
	@Override
	public Set<Long> findIssuedCouponIdsByMemberId(Long memberId) {
		List<Long> ids = jpa
				.select(mc.coupon.id)
				.from(mc)
				.where(mc.member.id.eq(memberId))
				.fetch();
		return new HashSet<>(ids);
	}
	
	//데이터 존재 여부 확인하기 -> 중복 발급 제한	
	@Override
	public boolean existsByMemberIdAndCouponId(Long memberId, Long couponId) {
		Integer fetchOne = jpa
				.selectOne()
				.from(mc)
				.where(
						mc.member.id.eq(memberId),
						mc.coupon.id.eq(couponId)
				)
				.fetchFirst();

		if(fetchOne == null) {
			return false;
		}
		else {
			return true;
		}
	}
	
	
	
	
	
	

}
