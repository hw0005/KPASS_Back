package com.example.kpass.domain.reservation.entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.example.kpass.domain.coupon.entity.MemberCoupon;
import com.example.kpass.domain.member.entity.Member;
import com.example.kpass.domain.performance.entity.Performance;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(
		name = "reservation",
		// 5분 지나면 복구시키기 위해 PENDING 상태이면서 시간이 지난 건만 빠르게 조회하도록 돕는 복합 인덱스
		indexes = {
				@Index(name="idxReservationStatusExpired", columnList="status, expiredAt")
		}
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Reservation {
	
	//한 번의 결제 트랜잭션과 5분 티켓 선점 생명주기를 총괄하는 주문·예매 도메
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name="id")
	private Long id;
	
	// 토스페이먼츠 맞추기
	@Column(name="orderUid", nullable = false, unique = true, length = 64)
	private String orderUid;
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="memberId", nullable =false)
	private Member member;
	
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="performanceId")
	private Performance performance;
	
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="memberCouponId")
	private MemberCoupon memberCoupon;
	
	@Column(name="orderName", nullable=false, length=200) 
	private String orderName;
	
	@Column(name="ticketQuantity", nullable = false) 
	private Integer ticketQuantity;
	
	@Column(name="totalPrice", nullable = false) 
	private Integer totalPrice;
	
	@Enumerated(EnumType.STRING)
	@Column(name="status", nullable=false, length=20)
	private ReservationStatus status;
	
	// 결제 가능 유효시간
	@Column(name = "expiredAt", nullable = false)
    private LocalDateTime expiredAt;

    @Column(name = "createdAt", nullable = false)
    private LocalDateTime createdAt;
    
    
    @OneToMany(mappedBy="reservation", cascade=CascadeType.ALL, orphanRemoval = true)
    private List<ReservationItem> reservationItems = new ArrayList<>();
    

    @Builder
	public Reservation(Member member, Performance performance, MemberCoupon memberCoupon,
			String orderName, Integer ticketQuantity, Integer totalPrice,
			LocalDateTime expiredAt) {

    	this.orderUid = "KPASS" + UUID.randomUUID().toString().replace("-", "");
		this.member = member;
		this.performance = performance;
		this.memberCoupon = memberCoupon;
		this.orderName = orderName;
		this.ticketQuantity = ticketQuantity;
		this.totalPrice = totalPrice;
		this.status = (totalPrice != null && totalPrice == 0) ? ReservationStatus.CONFIRMED : ReservationStatus.PENDING;
		this.expiredAt = expiredAt;
	}
    
    // 예매 추가, 상세 항목 추가 양방향 연관관계 편의 메
    public void addReservationItem(ReservationItem item) {
    	this.reservationItems.add(item);
    	item.assignReservation(this);
    }
    
    
    // 총 수량과 최종 결제 금액(쿠폰 할인 반영 후)을 계산 후 확정
    public void updateOrderSummary(Performance performance, String orderName, Integer tickeQuantity, Integer totalPrice) {
    	this.performance = performance;
    	this.orderName = orderName;
    	this.ticketQuantity = tickeQuantity;
    	this.totalPrice = totalPrice;
    	this.status = (totalPrice != null && totalPrice == 0) ? ReservationStatus.CONFIRMED : ReservationStatus.PENDING;
    }
   
    // 토스페이먼츠 최종 승인 API를 호출하기 직전에 실행하는 핵심 보안·무결성 검증
    public void validatePayable(Long requestMemberId, Integer requestAmount) {
    	// 본인 예매 건인
    	if(!this.member.getId().equals(requestMemberId)) {
    		throw new IllegalArgumentException("본인의 예매 건만 결제할 수 있습니다.");
    	}
    	// PEding상태 검증.
    	if(this.status != ReservationStatus.PENDING) {
    		throw new IllegalArgumentException("결제 대기 상태의 예마가 아닙니다. 현재상태: " + this.status);
    	}
    	
    	// 5분 지났어?
    	if(LocalDateTime.now().isAfter(this.expiredAt)) {
    		throw new IllegalArgumentException("결제 대기 시간(5분)이 만료된 예매입니다.");
    	}
    	
    	// 프론트에서 넘어온 금액과 실제 DB금액 일치해?
    	if(!this.totalPrice.equals(requestAmount)) {
    		throw new IllegalArgumentException("결제 요청 금액이 실제 예매 금액과 일치하지 않습니다.");
    	}
    }
    // 예매 시 confirmed 확정
    public void confirm() {
    	this.status = ReservationStatus.CONFIRMED;
    }
    
    //보상트랜잭션이나, 사용자 본인취소
    public void cancel() {
    	if(this.status == ReservationStatus.CONFIRMED) {
    		throw new IllegalArgumentException("이미 결제가 확정된 예매는 선점 취소할 수 없습니다.");
    	}
    	if(this.status == ReservationStatus.PENDING) {
    		this.status = ReservationStatus.CANCELLED;
    	}
    }
    // 5분 초과
    public void expire() {
    	if(this.status == ReservationStatus.PENDING) {
    		this.status = ReservationStatus.EXPIRED;
    	}
    }
    
	
}
