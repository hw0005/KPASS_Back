package com.example.kpass.global.auth;

import java.io.IOException;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.example.kpass.global.error.BusinessException;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
//OncePerRequestFilter: 한 번의 API 요청당 이 필터가 딱 1번만 실행되도록 보장하는 스프링 클래스
public class JwtAuthenticationFilter extends OncePerRequestFilter {

	// 컨트롤러 가기 전 최전방 문지기
	// 언제 실행되나? 프론트엔드에서 백엔드로 들어오는 모든 HTTP 요청마다 컨트롤러에 도착하기 직전에 자동으로 먼저 실행됩니다.
	private final JwtProvider jwtProvider;
    private final StringRedisTemplate redisTemplate;
    
	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {

		// 1. 프론트엔드가 보낸 HTTP 요청 헤더에서 "Authorization" 값을 꺼냄
		String authHeader = request.getHeader("Authorization");

		// 2. 헤더에 토큰이 실려온 경우에만 검사 진행
		// (토큰이 없는 비로그인 요청은 그냥 통과시킴 -> 로그인 API나 공연 목록 조회 같은 공개 API를 위해)
		if (authHeader != null && !authHeader.isBlank()) {

			// "Bearer eyJhbG..." 형태에서 앞의 "Bearer " 7글자를 떼어내고 순수 토큰 문자열만 추출
			String token = authHeader.startsWith("Bearer ") ? authHeader.substring(7).trim() : authHeader.trim();

			// 3. [Redis 블랙리스트 검사] 이 토큰이 방금 로그아웃 처리된 토큰인지 공용 Redis에 확인
			Boolean isBlacklisted = redisTemplate.hasKey("BL:" + token);
			if (Boolean.TRUE.equals(isBlacklisted)) {
				// 로그아웃된 토큰이면 컨트롤러로 넘기지 않고 그 자리에서 401 에러를 쏘고 요청 종료(return)
				response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "로그아웃된 토큰입니다.");
				return;
			}

			// 4. 토큰 해독 및 회원 번호(memberId) 추출
			try {
				Long memberId;
				if (token.startsWith("kpass-jwt-token-member-")) {
					memberId = Long.parseLong(token.replace("kpass-jwt-token-member-", ""));
				} else {
					// JwtProvider를 통해 위변조/만료 검증 후 memberId 추출
					memberId = jwtProvider.getMemberId(token);
				}

				// 5. [핵심] 현재 HTTP 요청 객체(request)에 "loginMemberId"라는 이름표로 회원 번호를 붙여둠
				// - 세션과 달리 이 request 객체는 이번 API 응답이 끝나면 수 밀리초 뒤에 즉시 소멸함
				// - 뒤에 실행될 LoginMemberArgumentResolver가 이 값을 꺼내서 컨트롤러에 넣어줌
				request.setAttribute("loginMemberId", memberId);

			} catch (BusinessException | NumberFormatException e) {
				// 위조되거나 만료된 토큰이면 즉시 401 에러 반환 후 종료
				response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "유효하지 않은 토큰입니다.");
				return;
			}
		}

		// 6. 다음 단계(스프링 시큐리티 -> 컨트롤러)로 요청을 계속 흘려보냄 (이 줄이 없으면 요청이 멈춤)
		filterChain.doFilter(request, response);
	}

}
