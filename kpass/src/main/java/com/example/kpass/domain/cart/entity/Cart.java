package com.example.kpass.domain.cart.entity;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

import com.example.kpass.domain.member.entity.Member;
import com.example.kpass.domain.performance.entity.Performance;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name ="cart")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Cart {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "memberId", nullable = false)
	private Member member;
	
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "performanceId", nullable = false)
	private Performance performance;
	
	
	@Column(name = "quantity")
	private int quantity;
	
	@CreationTimestamp
	@Column(name = "createdAt")
	private LocalDateTime createdAt;
	
	
	@CreationTimestamp
	@Column(name = "updatedAt")
	private LocalDateTime updatedAt;
	
	@Builder
	private Cart(Member member, Performance performance, int quantity) {
		if (member == null || performance == null) {
            throw new IllegalArgumentException("회원과 공연 정보는 필수입니다.");
        }
		this.member = member;
		this.performance = performance;
		this.quantity = Math.max(1, quantity);
	}
	
	// 도메인 연산 1: 수량 누적
    public void addQuantity(int count) {
        if (count <= 0) {
            throw new IllegalArgumentException("추가할 수량은 1개 이상이어야 합니다.");
        }
        this.quantity += count;
    }

    // 도메인 연산 2: 수량 직접 변경
    public void updateQuantity(int quantity) {
        this.quantity = Math.max(1, quantity);
    }

    // 도메인 연산 3: 항목별 총 금액 계산 (서비스에서 getter로 꺼내 곱하지 않고 엔티티가 직접 계산)
    public int calculateTotalPrice() {
        int unitPrice = (this.performance.getPrice() != null) ? this.performance.getPrice() : 0;
        return unitPrice * this.quantity;
    }
	
	
}
