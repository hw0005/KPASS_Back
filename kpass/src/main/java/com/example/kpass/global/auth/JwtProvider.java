package com.example.kpass.global.auth;

import java.nio.charset.StandardCharsets;
import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.example.kpass.global.error.BusinessException;
import com.example.kpass.global.error.ErrorCode;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Component
public class JwtProvider {

	/**
	 * 토큰을 찍어내고 검사하는 조폐국 언제 실행되나? 로그인 성공 시(토큰 발급), API 요청 시(토큰 해독), 로그아웃 시(남은 시간 계산)
	 * 호출됩니다.
	 */

	private final SecretKey secretKey;
	private final long accessTokenValidityMs = 1000L * 60 * 30; // 30분
	private final long refreshTokenValidityMs = 1000L * 60 * 60 * 24 * 7; // 7일

	public JwtProvider(@Value("${jwt.secret}") String secret) {
		this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
	}

	/**
	 * [로그인 시 호출] Access Token 생성 - 회원 번호(memberId)를 토큰 내부(subject)에 넣고, 비밀키로
	 * 서명(도장)을 찍어 문자열로 반환
	 */
	public String createAccessToken(Long memberId) {
		Date now = new Date();
		return Jwts.builder().subject(String.valueOf(memberId)) // 토큰 주인 식별자 (예: "1")
				.issuedAt(now) // 발급 시간
				.expiration(new Date(now.getTime() + accessTokenValidityMs)) // 만료 시간 (현재 + 30분)
				.signWith(secretKey) // 위변조 방지를 위해 우리 서버 비밀키로 서명
				.compact(); // "eyJhbG..." 형태의 최종 JWT 문자열로 압축
	}

	/**
	 * [로그인 시 호출] Refresh Token 생성 (Access Token과 구조는 같고 수명만 7일)
	 */
	public String createRefreshToken(Long memberId) {
		Date now = new Date();
		return Jwts.builder().subject(String.valueOf(memberId)).issuedAt(now)
				.expiration(new Date(now.getTime() + refreshTokenValidityMs)).signWith(secretKey).compact();
	}

	/**
	 * [API 요청 시 호출] 토큰 검증 및 회원 번호(memberId) 추출 - parseSignedClaims()가 실행되는 순간 두 가지를
	 * 자동으로 검사함: 1) 우리 서버의 secretKey로 찍힌 도장이 맞는지 (누가 중간에 회원번호를 1->2로 조작했으면 즉시 에러) 2)
	 * 만료 시간(30분)이 지나지 않았는지 (지났으면 즉시 에러)
	 */
	public Long getMemberId(String token) {
		try {
			Claims claims = Jwts.parser().verifyWith(secretKey) // 우리 비밀키로 서명 검증
					.build().parseSignedClaims(token) // 토큰 해독 (위조/만료 시 여기서 JwtException 발생)
					.getPayload(); // 토큰 본문(Payload) 꺼내기

			return Long.parseLong(claims.getSubject()); // 넣어두었던 memberId를 Long으로 변환해 반환
		} catch (JwtException | IllegalArgumentException e) {
			// 토큰이 위조되었거나, 만료되었거나, 형식이 깨졌을 때 401 에러 발생
			throw new BusinessException(ErrorCode.INVALID_TOKEN);
		}
	}

	/**
	 * [로그아웃 시 호출] 토큰의 만료까지 몇 밀리초(ms) 남았는지 계산 - Redis 블랙리스트에 영구 저장하지 않고, 딱 남은
	 * 유효시간만큼만 보관했다가 자동 삭제하기 위함
	 */
	public long getRemainingExpirationMs(String token) {
		try {
			Date expiration = Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload()
					.getExpiration();
			return expiration.getTime() - System.currentTimeMillis();
		} catch (JwtException | IllegalArgumentException e) {
			return 0L; // 이미 만료된 토큰이면 남은 시간 0 반환
		}
	}
}
