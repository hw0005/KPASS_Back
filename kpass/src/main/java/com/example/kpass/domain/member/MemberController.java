package com.example.kpass.domain.member;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.kpass.domain.member.entity.Member;
import com.example.kpass.domain.member.service.MemberBO;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class MemberController {
	
	private final MemberBO memberBO;
	
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
		
		Map<String, Object> result = new HashMap<>();
		result.put("code", 200);
		result.put("message", "로그인 성공");
		result.put("memberId", member.getEmail());
		result.put("password", member.getPassword());
		
		return ResponseEntity.ok(result);
	}
	
	
	
	
}
