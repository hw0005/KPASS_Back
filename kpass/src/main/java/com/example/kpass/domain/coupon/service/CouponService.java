package com.example.kpass.domain.coupon.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.kpass.domain.coupon.dto.CouponEventResponse;
import com.example.kpass.domain.coupon.dto.CouponIssueResponse;
import com.example.kpass.domain.coupon.dto.MyCouponResponse;
import com.example.kpass.domain.coupon.entity.Coupon;
import com.example.kpass.domain.coupon.entity.MemberCoupon;
import com.example.kpass.domain.coupon.repository.CouponRepository;
import com.example.kpass.domain.coupon.repository.MemberCouponRepository;
import com.example.kpass.domain.member.entity.Member;
import com.example.kpass.domain.member.repository.MemberRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CouponService {
	
	private final CouponRepository couponRepository;
	private final MemberCouponRepository memberCouponRepository;
	private final MemberRepository memberRepository;
	
	// 내 보유 미사용 쿠폰 목록 조회
	public List<MyCouponResponse> getMyUsableCoupons(Long memberId) {
		List<MemberCoupon> coupons = memberCouponRepository.findUsableCouponsByMemberId(memberId);
		List<MyCouponResponse> responseList = new ArrayList<>();
		
		// 조회한 엔티티 리스트 꺼내
		for (MemberCoupon mc : coupons) {
			responseList.add(new MyCouponResponse(mc));
		}
		
		return responseList;
	}
	
	// 진행 중인 선착순 쿠폰 이벤트 목록 조회(내가 이미 받았는지 조회)
	public List<CouponEventResponse> getActiveCouponEvent(Long memberId) {
		Set<Long> issuedCouponIds = memberCouponRepository.findIssuedCouponIdsByMemberId(memberId);
		
		List<Coupon> activeCoupons = couponRepository.findAllActioveCoupons();
		List<CouponEventResponse> responseList = new ArrayList<>();
		
		for (Coupon coupon : activeCoupons) {
			boolean alreadyIssued = issuedCouponIds.contains(coupon.getId());
			responseList.add(new CouponEventResponse(coupon, alreadyIssued));
		}
		
		return responseList;
	}
	
	// 실제 쿠폰 발급 DB 처리(데이터 변경 작업)
	@Transactional
	public CouponIssueResponse issueCoupon(Long memberId, String code) {
		Optional<Coupon> optCoupon = couponRepository.findByCode(code);
		
		if(optCoupon.isEmpty()) {
			throw new IllegalArgumentException("존재하지 않는 쿠폰입니다.");
		}
		
		Coupon coupon = optCoupon.get();
		
		// 1인 1매 중복 발급 검증
		if(memberCouponRepository.existsByMemberIdAndCouponId(memberId, coupon.getId())) {
			throw new IllegalStateException("이미 처리되었습니다.");
		}
		
		// 엔티티 내부 -1차감
		coupon.decreaseQuantity();
		
		Optional<Member> optMember = memberRepository.findById(memberId);
		if(optMember.isEmpty()) {
			throw new IllegalArgumentException("회원정보 찾을 수 없습니다.");
		}
		Member member = optMember.get();
		
		MemberCoupon memberCoupon = MemberCoupon.builder()
				.member(member)
				.coupon(coupon)
				.build();
		
		MemberCoupon saved = memberCouponRepository.save(memberCoupon);
		return new CouponIssueResponse(saved);
	}
	
}
