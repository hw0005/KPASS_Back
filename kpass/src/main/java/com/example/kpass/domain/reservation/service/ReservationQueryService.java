package com.example.kpass.domain.reservation.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.kpass.domain.reservation.dto.ReservationDtos.ReservationHistoryResponse;
import com.example.kpass.domain.reservation.repository.ReservationRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReservationQueryService {

	//화면에 뿌려줄 데이터만 빠르게 긁어오기
	
	private final ReservationRepository reservationRepository;
	
	// read: 마이페이지 확정 예매 내역 조회(영속성 컨텍스트 오버헤드 없이 DTO로 바로 프로젝션)
	public Page<ReservationHistoryResponse> getMyConfirmedReservation(Long memberId, Pageable pageable) {
		return reservationRepository.findConfirmedHistoryByMemberId(memberId, pageable);
	}
	
}
