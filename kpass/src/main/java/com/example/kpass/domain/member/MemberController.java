package com.example.kpass.domain.member;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.kpass.domain.member.entity.Member;
import com.example.kpass.domain.member.service.MemberBO;
import com.example.kpass.global.auth.JwtProvider;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class MemberController {
	
	private final MemberBO memberBO;
	private final JwtProvider jwtProvider;
	private final StringRedisTemplate redisTemplate;
	
	/**
	 * 회원가입 로직(로컬)
	 * @param email
	 * @param password
	 * @param nickname
	 * @param accountNumber
	 * @return
	 */
	@PostMapping("/register")
	public ResponseEntity<Map<String, Object>> register(
			@RequestParam("email") String email,
			@RequestParam("password") String password,
			@RequestParam("nickname") String nickname,
			@RequestParam("accountNumber") String accountNumber) {
		
		// 중복 등 예외 발생 시 GlobalExceptionHandler(총사령관)가 400 응답으로 처리함
        Member member = memberBO.addLocalUser(email, password, nickname, accountNumber);

        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("message", "회원가입 성공");
        result.put("memberId", member.getId());

        return ResponseEntity.ok(result);
	}
	
	@PostMapping("/login")
	public ResponseEntity<Map<String, Object>> login(
			@RequestParam("email") String email,
			@RequestParam("password") String password) {
		
		//
		Member member = memberBO.login(email, password);
		
		// 소셜 로그인과 동일하게 JWT 발급 및 Redis Refresh Token 저장
		String accessToken = jwtProvider.createAccessToken(member.getId());
		String refreshToken = jwtProvider.createRefreshToken(member.getId());
		redisTemplate.opsForValue().set("RT:" + member.getId(), refreshToken, Duration.ofDays(7));
		
		Map<String, Object> result = new HashMap<>();
		result.put("code", 200);
		result.put("message", "로그인 성공");
		result.put("accessToken", accessToken); 
		result.put("refreshToken", refreshToken); 
		result.put("memberId", member.getEmail());
		result.put("password", member.getPassword());
		result.put("email", member.getEmail());
	    result.put("nickname", member.getNickname());
		
		return ResponseEntity.ok(result);
	}
	
	
	
	
}
