package com.example.kpass.domain.coupon.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class CouponIssueRequest {
	//프론트에서 보낸 JSON 요청 받아주기
	
	private String code;
	
	
}
