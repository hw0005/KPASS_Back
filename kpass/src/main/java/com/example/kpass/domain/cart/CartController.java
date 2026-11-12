package com.example.kpass.domain.cart;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.kpass.domain.cart.dto.CartAddRequestDto;
import com.example.kpass.domain.cart.dto.CartItemResponseDto;
import com.example.kpass.domain.cart.service.CartService;
import com.example.kpass.domain.member.repository.MemberRepository;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/cart")
@RequiredArgsConstructor
public class CartController {
	
	private final CartService cartService;
	private final MemberRepository memberRepository;
	
	private Long getMemberId(HttpSession session) {
        Object memberId = session.getAttribute("memberId");
        if (memberId == null) {
            throw new IllegalStateException("로그인이 필요한 서비스입니다.");
        }
        return ((Number) memberId).longValue();
    }
	
	
	// 내 장바구니 조회
	@GetMapping
	public ResponseEntity<Map<String, Object>> getMyCart(HttpSession httpSession) {
		Long memberId = (Long) httpSession.getAttribute("memberId");
		List<CartItemResponseDto> list = cartService.getMyCart(memberId);
		
		Map<String, Object> result = new HashMap<>();
		
		result.put("code", 200);
		result.put("message", "장바구니 조회 완료");
		result.put("data", list);
		
		return ResponseEntity.ok(result);
	}
	
	// 장바구니 담기
	@PostMapping
    public ResponseEntity<Map<String, Object>> addCart(
            HttpSession session,
            @RequestBody CartAddRequestDto dto) {
        Long memberId = (Long) session.getAttribute("memberId");
        if (memberId == null) {
            throw new IllegalStateException("로그인이 필요한 서비스입니다.");
        }

        cartService.addCart(memberId, dto);

        Map<String, Object> response = new HashMap<>();
        response.put("code", 200);
        response.put("message", "장바구니 담기 완료");
        return ResponseEntity.ok(response);
    }
	
	// 수량 변경
	@PostMapping("/update")
    public ResponseEntity<Map<String, Object>> updateQuantity(
            HttpSession session,
            @RequestParam("cartId") Long cartId,
            @RequestParam("quantity") int quantity) {
        Long memberId = (Long) session.getAttribute("memberId");
        if (memberId == null) {
            throw new IllegalStateException("로그인이 필요한 서비스입니다.");
        }

        cartService.updateQuantity(memberId, cartId, quantity);

        Map<String, Object> response = new HashMap<>();
        response.put("code", 200);
        response.put("message", "수량 변경 완료");
        return ResponseEntity.ok(response);
    }
	
	
	// 단건 삭제
	@PostMapping("/delete")
    public ResponseEntity<Map<String, Object>> removeCartItem(
            HttpSession session,
            @RequestParam("cartId") Long cartId) {
		
        Long memberId = (Long) session.getAttribute("memberId");
        if (memberId == null) {
            throw new IllegalStateException("로그인이 필요한 서비스입니다.");
        }

        cartService.removeCartItem(memberId, cartId);

        Map<String, Object> response = new HashMap<>();
        response.put("code", 200);
        response.put("message", "장바구니 삭제 완료");
        return ResponseEntity.ok(response);
    }
	
	
	// 선택항목 일괄 삭제
	@PostMapping("/delete-selected")
    public ResponseEntity<Map<String, Object>> removeSelected(
            HttpSession session,
            @RequestParam("cartIds") List<Long> cartIds) {
		
        Long memberId = (Long) session.getAttribute("memberId");
        if (memberId == null) {
            throw new IllegalStateException("로그인이 필요한 서비스입니다.");
        }

        cartService.removeSelectedItems(memberId, cartIds);

        Map<String, Object> response = new HashMap<>();
        response.put("code", 200);
        response.put("message", "선택 항목 삭제 완료");
        return ResponseEntity.ok(response);
    }
	
}
