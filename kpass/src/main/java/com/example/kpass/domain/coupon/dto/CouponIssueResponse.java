package com.example.kpass.domain.coupon.dto;

import com.example.kpass.domain.coupon.entity.MemberCoupon;

import lombok.Getter;

@Getter
public class CouponIssueResponse {
	// 선착순 쿠폰 발급 성공 시, 프론트에 돌려주는 응답 DTO
	private Long memberCouponId;
	private String name;
	private Integer discountAmount;
	private String message;
	
	// 쿠폰 발급 성공 후 프론트
	public CouponIssueResponse(MemberCoupon mc) {
		this.memberCouponId = mc.getId();
		this.name = mc.getCoupon().getName();
		this.discountAmount = mc.getCoupon().getDiscountAmount();
		this.message = "쿠폰이 정상적으로 발급되었습니다.";
	}
}
