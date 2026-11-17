	package com.example.kpass.domain.reservation.repository;
	
	import java.time.LocalDateTime;
	import java.util.List;
	import java.util.Optional;
	
	import org.springframework.data.domain.Page;
	import org.springframework.data.domain.Pageable;
	
	import com.example.kpass.domain.reservation.dto.ReservationDtos.ReservationHistoryResponse;
	import com.example.kpass.domain.reservation.entity.Reservation;
	import com.example.kpass.domain.reservation.entity.ReservationStatus;
	
	public interface ReservationRepositoryCustom {
	
		// CUD: 결제 승인 및 취소 시 연관 엔티티 한번에 조회(N+1방지)
		Optional<Reservation> findByOrderUidWithDetails(String orderUid);
		
		// CUD: 5분 만료 스케줄러에서 재고, 쿠폰 복구 대상 예매 목록 조회
		List<Reservation> findExpiredReservationsWithDetails(ReservationStatus status,
				LocalDateTime now);
		
		// Read: 마이페이지 예매 내역을 엔티티 대신 dto로 직접 프로젝션 조회
		Page<ReservationHistoryResponse> findConfirmedHistoryByMemberId(Long memberId, Pageable pageable);
		
	}
