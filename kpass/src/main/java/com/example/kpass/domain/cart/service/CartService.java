package com.example.kpass.domain.cart.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.kpass.domain.cart.dto.CartAddRequestDto;
import com.example.kpass.domain.cart.dto.CartItemResponseDto;
import com.example.kpass.domain.cart.entity.Cart;
import com.example.kpass.domain.cart.repo.CartRepository;
import com.example.kpass.domain.member.entity.Member;
import com.example.kpass.domain.member.repository.MemberRepository;
import com.example.kpass.domain.performance.entity.Performance;
import com.example.kpass.domain.performance.repository.PerformanceRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CartService {

	private final CartRepository cartRepository;
	private final MemberRepository memberRepository;
	private final PerformanceRepository performanceRepository;
	
	
	// 내 장바구니 목록 조회
	public List<CartItemResponseDto> getMyCart(Long memberId) {
		return cartRepository.findAllByMemberIdWithPerformance(memberId);
	}
	
	// 장바구니 담기(동일 공연 존재 시 수량 누적, 없으면 추가)
	@Transactional
	public void addCart(Long memberId, CartAddRequestDto dto) {
		Member m = memberRepository.getReferenceById(memberId);
		Performance p = findTargetPerformance(dto);
		
		int addQuantity = 1;
		if(dto.getQuantity() != null && dto.getQuantity() > 0) {
			addQuantity = dto.getQuantity();
		}
		
		Optional<Cart> exisitingOpt = cartRepository.findByMemberIdAndPerformanceId(memberId, p.getId());
		if(exisitingOpt.isPresent()) {
			Cart exisitingCart = exisitingOpt.get();
			exisitingCart.addQuantity(addQuantity);
			return;
		}
		
		Cart newCart = Cart.builder()
				.member(m)
				.performance(p)
				.quantity(addQuantity)
				.build();
		cartRepository.save(newCart);
	}
	
	
	// 수량 변경
    @Transactional
    public void updateQuantity(Long memberId, Long cartId, int quantity) {
        Optional<Cart> cartOpt = cartRepository.findById(cartId);
        if (cartOpt.isEmpty()) {
            throw new IllegalArgumentException("장바구니 항목을 찾을 수 없습니다.");
        }

        Cart cart = cartOpt.get();
        
        if (!cart.getMember().getId().equals(memberId)) {
            throw new SecurityException("본인의 장바구니만 수정할 수 있습니다.");
        }
        
        cart.updateQuantity(quantity);
    }
    
    
    // 4. 단건 삭제 (단순 파라미터)
    @Transactional
    public void removeCartItem(Long memberId, Long cartId) {
        cartRepository.deleteByIdAndMemberId(cartId, memberId);
    }

    // 5. 선택 항목 일괄 삭제 (단순 파라미터)
    @Transactional
    public void removeSelectedItems(Long memberId, List<Long> cartIds) {
        if (cartIds == null || cartIds.isEmpty()) {
            return;
        }
        cartRepository.deleteAllByMemberIdAndIdIn(memberId, cartIds);
    }
    
    
    
    private Performance findTargetPerformance(CartAddRequestDto dto) {
        if (dto.getPerformanceId() != null) {
            Optional<Performance> byId = performanceRepository.findById(dto.getPerformanceId());
            if (byId.isEmpty()) {
                throw new IllegalArgumentException("공연을 찾을 수 없습니다.");
            }
            return byId.get();
        }

        if (dto.getExternalId() != null) {
            Optional<Performance> byExternalId = performanceRepository.findByExternalId(dto.getExternalId());
            if (byExternalId.isEmpty()) {
                throw new IllegalArgumentException("공연을 찾을 수 없습니다.");
            }
            return byExternalId.get();
        }

        throw new IllegalArgumentException("공연 식별자가 필요합니다.");
    }
	
	
	
}
