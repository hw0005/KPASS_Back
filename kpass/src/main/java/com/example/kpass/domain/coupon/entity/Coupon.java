package com.example.kpass.domain.coupon.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "coupon")
@NoArgsConstructor(access = AccessLevel.PROTECTED)

public class Coupon {
	// 관리자가 등록한 할인 쿠폰의 원본 정책(쿠폰 코드, 쿠폰명, 할인 금액, 총 발행 수량, 남은 수량, 만료일시)을 관리
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY) 
	@Column(name = "id")
	private Long id;
	
	@Column(name = "code", nullable = false, unique= true, length = 50)
	private String code;
	
	@Column(name = "name", nullable = false, length = 100)
	private String name;
	
	@Column(name = "discountAmount", nullable= false)
	private Integer discountAmount;
	
	@Column(name = "totalQuantity", nullable = false)
	private Integer totalQuantity;
	
	@Column(name = "remainQuantity", nullable = false)
	private Integer remainQuantity;
	
	@Column(name = "validUntil", nullable = false)
	private LocalDateTime validUntil;
	
	@Column(name = "createdAt", nullable = false)
	private LocalDateTime createdAt;

	
	@Builder
	public Coupon(String code, String name, Integer discountAmount, Integer totalQuantity,
		 LocalDateTime validUntil) {
		
		this.code = code;
		this.name = name;
		this.discountAmount = discountAmount;
		this.totalQuantity = totalQuantity;
		this.remainQuantity = totalQuantity; // 남은수량 박아넣기 근데 처음엔 총 수량임
		this.validUntil = validUntil;
		this.createdAt = LocalDateTime.now();
	}
	
	// 회원이 선착순 쿠폰을 발급받을 때 호출, , 수량 직접 안 깎고 검증 후 차감
	public void decreaseQuantity() {
		// 시간 지났나?
		if(LocalDateTime.now().isAfter(this.validUntil)) {
			throw new IllegalArgumentException("만료된 쿠폰입니다.");
		}
		// 쿠폰소진
		if (this.remainQuantity <= 0) {
			throw new IllegalArgumentException("쿠폰이 모두 소진되었습니다.");
		}
		this.remainQuantity = this.remainQuantity - 1;
	}
	
	
	
	
	
}
