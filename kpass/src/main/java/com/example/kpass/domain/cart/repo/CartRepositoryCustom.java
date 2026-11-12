package com.example.kpass.domain.cart.repo;

import java.util.List;
import java.util.Optional;

import com.example.kpass.domain.cart.dto.CartItemResponseDto;
import com.example.kpass.domain.cart.entity.Cart;

public interface CartRepositoryCustom {
	// 리스트로 전부 가져오기
	List<CartItemResponseDto> findAllByMemberIdWithPerformance(Long memberId);
	
	// 동일공연 중복담기 확인용 단건 조회
	Optional<Cart> findByMemberIdAndPerformanceId(Long memberId, Long performanceId);
	
	// 단건 삭제
	void deleteByIdAndMemberId(Long cartId, Long memberId);
	
	
	// 선택항목 일괄삭제
	void deleteAllByMemberIdAndIdIn(Long memberId, List<Long> cartIds);
}
