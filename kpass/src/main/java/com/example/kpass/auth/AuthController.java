package com.example.kpass.auth;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.kpass.auth.dto.OAuthUserInfoDto;
import com.example.kpass.auth.service.OAuthService;
import com.example.kpass.domain.member.entity.Member;
import com.example.kpass.domain.member.repository.MemberRepository;
import com.example.kpass.domain.member.service.MemberBO;
import com.example.kpass.global.error.BusinessException;
import com.example.kpass.global.error.ErrorCode;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

	private final MemberBO memberBO;
	private final MemberRepository memberRepository;
	private final List<OAuthService> oauthServices;

	/**
	 * OAuth로그인
	 * @param provider
	 * @param request
	 * @return
	 */
	@PostMapping("/{provider}")
	public ResponseEntity<Map<String, Object>> oauthLogin(
			@PathVariable("provider") String provider,
			@RequestBody Map<String, String> request) {

		// 1. 프론트엔드가 보낸 { "code": "..." } 에서 code 추출
		String code = request.get("code");

		// 2. 요청된 provider("kakao", "naver", "google")에 일치하는 서비스 찾기
		OAuthService targetService = null;
		// service.getProvider() 호출 시 -> KaKaoAuthService의 getProvider()가 실행됨 ("KAKAO" 리턴)
		for (OAuthService service : oauthServices) {
			if (service.getProvider().equalsIgnoreCase(provider)) {
				targetService = service;
				break;
			}
		}

		if (targetService == null) {
			throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR);
		}

		// 3. 해당 소셜 API 호출하여 사용자 정보 DTO 획득
		OAuthUserInfoDto oAuthUser = targetService.getUserInfo(code);

		// 4. MemberBO를 통해 DB 회원 조회 또는 신규 가입(Member.createOAuth)
		Optional<Member> memberOpt = memberRepository.findByEmail(oAuthUser.getEmail());
		
		Map<String, Object> result = new HashMap<>();
		
		if(memberOpt.isPresent()) {
			// 이미 가입 되면 로그인 처리
			Member m = memberOpt.get();
			
			result.put("isRegistered", true);
			result.put("accessToken", "kpass-jwt-token-member-" + m.getId());
            result.put("email", m.getEmail());
            result.put("nickname", m.getNickname());
			
		}
		else {
			// 신규 회원이면 추가 정보(계좌번호) 입력 필요 응답
            result.put("isRegistered", false);
            result.put("provider", oAuthUser.getProvider());
            result.put("providerId", oAuthUser.getProviderId());
            result.put("email", oAuthUser.getEmail());
            result.put("nickname", oAuthUser.getNickname());
            result.put("profileImage", oAuthUser.getProfileImage());
		}
		return ResponseEntity.ok(result);
	}
	
	// 계좌번호를 전달받아 소셜 회원가입 최종 완료
	/**
	 * OAuth 회원가입 -> 로그인했는데 없을 시
	 * @param request
	 * @return
	 */
    @PostMapping("/oauth/register")
    public ResponseEntity<Map<String, Object>> completeOAuthRegister(
    		@RequestBody Map<String, String> request) {
    	
        String email = request.get("email");
        String nickname = request.get("nickname");
        String provider = request.get("provider");
        String providerId = request.get("providerId");
        String profileImage = request.get("profileImage");
        String accountNumber = request.get("accountNumber"); // 필수 계좌번호

        Member member = memberBO.addOAuthUser(email, nickname, provider, providerId, profileImage, accountNumber);

        Map<String, Object> result = new HashMap<>();
        result.put("accessToken", "kpass-jwt-token-member-" + member.getId());
        result.put("email", member.getEmail());
        result.put("nickname", member.getNickname());

        return ResponseEntity.ok(result);
    }

}
