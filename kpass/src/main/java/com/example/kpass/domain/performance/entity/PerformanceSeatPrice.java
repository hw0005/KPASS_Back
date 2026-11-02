package com.example.kpass.domain.performance.entity;

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
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name="performanceseatprice")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class PerformanceSeatPrice {
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String seatType; // 예: R석, S석, 일반석 등

    @Column(nullable = false)
    private Integer price;    // 예: 15000

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "performance_id")
    private Performance performance;

    @Builder
    public PerformanceSeatPrice(String seatType, Integer price) {
        this.seatType = seatType;
        this.price = price;
    }
    public static PerformanceSeatPrice createPrice(String seatType, Integer price) {
    	PerformanceSeatPrice psp = PerformanceSeatPrice.builder()
    	.seatType(seatType)
    	.price(price)
    	.build();
    	
    	return psp;
    }

    // 연관관계 편의 메서드용 setter
    public void setPerformance(Performance performance) {
        this.performance = performance;
    }
}
