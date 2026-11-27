package com.example.kpass.domain.coupon.entity;

import java.time.LocalDateTime;

import com.example.kpass.domain.member.entity.Member;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
// 백엔드 단에서 1인 1매만 할 수 있게 하기
@Table(
		name = "memberCoupon",
		uniqueConstraints = {
				@UniqueConstraint(name = "ukMemberCoupon", columnNames = {"memberId", "couponId"})
		})
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MemberCoupon {
	
	// 특정 회원(Member)이 발급받은 쿠폰 내역과 실제 사용 여부(isUsed)를 관리
	// memberId와 couponId로 유니크 걸어 한 회원 2번 발급 제한
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name="id")
	private Long id;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "memberId", nullable = false, foreignKey = @ForeignKey(name="fkMemberCouponMember"))
	private Member member;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "couponId", nullable = false, foreignKey = @ForeignKey(name="fkMemberCouponCoupon"))
	private Coupon coupon;
	
	@Column(name="isUsed", nullable = false)
	private Boolean isUsed;
	
	@Column(name="usedAt")
	private LocalDateTime usedAt;
	
	@Column(name="isusedAt", nullable = false) 
	private LocalDateTime isusedAt;
	
	
	@Builder
	public MemberCoupon(Member member, Coupon coupon) {
		this.member = member;
		this.coupon = coupon;
		this.isUsed = false;
		this.isusedAt = LocalDateTime.now();
	}
	
	// 예매 생성 시 회원이 쿠폰을 적용할 때 호출
	public void use(Long requestMemberId) {
		// 지금 너 memberID 맞?
		if(!this.member.getId().equals(requestMemberId)) {
			throw new IllegalArgumentException("본인이 보유한 쿠폰만 사용 가능");
		}
		// 이미사용된?
		if(Boolean.TRUE.equals(this.isUsed)) {
			throw new IllegalArgumentException("이미 사용한 쿠폰입니다.");
		}
		this.isUsed = true;
		this.usedAt = LocalDateTime.now();
	}
	// 쿠폰을 적용해 선점했던 예매가 5분 시간 초과로 만료되거나 취소
	public void restore() {
		this.isUsed = false;
		this.usedAt = null;
	}
	

}
