package com.example.kpass.domain.reservation.entity;

import com.example.kpass.domain.performance.entity.Performance;
import com.example.kpass.domain.performance.entity.PerformanceSeatPrice;

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
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name="reservationItem")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ReservationItem {
	// 장바구니에서 여러 공연이나 여러 좌석 등급을 한 번에 결제할 때
	// , 각 공연(performance)과 좌석 등급(seatPrice)별 예매 수량·단가를 개별 기록하는 하위 엔티티
	//어떤 공연의 어떤 좌석 재고를 몇 장 복구해야 하는지 정확히 추적하는 기준
	
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@Column(name="id")
	private Long id;
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="reservationId", nullable=false)
	private Reservation reservation;
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="performanceId", nullable=false)
	private Performance performance;
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name = "seatPriceId")
	private PerformanceSeatPrice seatPrice;
	
	@Column(name="cartId")
	private Long cartId;
	
	@Column(name="seatType", nullable=false, length=50)
	private String seatType;
	
	@Column(name="quantity", nullable=false)
	private Integer quantity;
	
	@Column(name="unitPrice", nullable=false)
	private Integer unitPrice;

	public ReservationItem(Performance performance, PerformanceSeatPrice seatPrice,
			Long cartId, String seatType, Integer quantity, Integer unitPrice) {
		
		this.performance = performance;
		this.seatPrice = seatPrice;
		this.cartId = cartId;
		this.seatType = seatType;
		this.quantity = quantity;
		this.unitPrice = unitPrice;
	}
	
	void assignReservation(Reservation reservation) {
		this.reservation = reservation;
	}
	
	

}
