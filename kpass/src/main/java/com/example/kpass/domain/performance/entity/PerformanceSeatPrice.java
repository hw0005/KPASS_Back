package com.example.kpass.domain.performance.entity;

import org.springframework.transaction.support.TransactionTemplate;

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
    @JoinColumn(name = "performanceId")
    private Performance performance;
    
    @Column(nullable = false)
    private Integer stock;

    @Builder
    public PerformanceSeatPrice(String seatType, Integer price, Integer stock) {
        this.seatType = seatType;
        this.price = price;
        this.stock = (stock!=null) ? stock : 100;
    }
    public static PerformanceSeatPrice createPrice(String seatType, Integer price) {
    	PerformanceSeatPrice psp = PerformanceSeatPrice.builder()
    	.seatType(seatType)
    	.price(price)
    	.stock(100)
    	.build();
    	
    	return psp;
    }

    // 연관관계 편의 메서드용 setter
    public void setPerformance(Performance performance) {
        this.performance = performance;
    }
    
    // 좌석 재고 차감
    public void decreaseStock(Performance performance) {
    	if(performance.getStockQuantity() <= 0) {
    		throw new IllegalArgumentException("예매 수량은 1매이상이어야 합니다.");
    	}
    	if(this.stock < performance.getStockQuantity()) {
    		throw new IllegalArgumentException("잔여좌석 부족(좌석: " + this.seatType+ ", 남은 수량: " + this.stock+ "석");
    	}
    	this.stock = this.stock - performance.getStockQuantity();
    }
    
    // 좌석 재고 복구
    public void increaseStcok(int quantity) {
    	if (quantity > 0) {
    		this.stock = this.stock + quantity;
    	}
    }
    
    
    
}
