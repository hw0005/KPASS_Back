package com.example.kpass.domain.coupon.dto;

import com.example.kpass.domain.coupon.entity.MemberCoupon;

import lombok.Getter;

@Getter
public class MyCouponResponse {
	// 결제페이지의 보유 쿠폰 선택 셀렉트 박스에 내가 가진 미사용 쿠폰 목록 보여주는 응답 DTO
	
	private Long memberCouponId;
	private String code;
	private String name;
	private Integer discountAmount;
	private String validUntil;
	
	// 회원이 보유한 쿠폰 엔티티를 프론트 드롭다운용 응답 DTO로 변환하는 일반 생성자
	public MyCouponResponse(MemberCoupon mc) {
		this.memberCouponId = mc.getId();
		this.code = mc.getCoupon().getCode();
		this.name = mc.getCoupon().getName();
		this.discountAmount = mc.getCoupon().getDiscountAmount();
		this.validUntil = mc.getCoupon().getValidUntil().toString();
	}
}
