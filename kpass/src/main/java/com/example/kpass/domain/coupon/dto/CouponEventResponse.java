package com.example.kpass.domain.coupon.dto;

import com.example.kpass.domain.coupon.entity.Coupon;

import lombok.Getter;

@Getter
public class CouponEventResponse {
	// 선착순 쿠폰의 드롭다운 목록 그릴 때 프론트에 전달하는 응답 DTO
	
	private Long couponId;
	private String code;
	private String name;
	private Integer discountAmount;
	private Integer totalQuantity;
	private Integer remainQuantity;
	private boolean alreadyIssued;
	
	// 엔티티와 발급 여부 받아 응답 DTO 변환 생성자
	public CouponEventResponse (Coupon coupon, boolean alreadyIssued) {
		this.couponId = couponId;
		this.code = coupon.getCode();
		this.name = coupon.getName();
		this.discountAmount = coupon.getDiscountAmount();
		this.totalQuantity = coupon.getTotalQuantity();
		this.remainQuantity = coupon.getRemainQuantity();
		this.alreadyIssued = alreadyIssued;
	}
	
	
	
	
}
