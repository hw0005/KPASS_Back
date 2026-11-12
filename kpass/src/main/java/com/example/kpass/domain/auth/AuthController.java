package com.example.kpass.domain.auth;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.kpass.domain.auth.dto.OAuthUserInfoDto;
import com.example.kpass.domain.auth.service.OAuthService;
import com.example.kpass.domain.member.entity.Member;
import com.example.kpass.domain.member.repository.MemberRepository;
import com.example.kpass.domain.member.service.MemberBO;
import com.example.kpass.global.auth.JwtProvider;
import com.example.kpass.global.auth.LoginMember;
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
	private final JwtProvider jwtProvider;
	private final StringRedisTemplate redisTemplate;

	/**
	 * OAuth 로그인: POST /api/v1/auth/{provider}
	 */
	@PostMapping("/{provider}")
    public ResponseEntity<Map<String, Object>> oauthLogin(
            @PathVariable("provider") String provider,
            @RequestBody Map<String, String> request) {

        String code = request.get("code");

        OAuthService targetService = null;
        for (OAuthService service : oauthServices) {
            if (service.getProvider().equalsIgnoreCase(provider)) {
                targetService = service;
                break;
            }
        }

        if (targetService == null) {
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR);
        }

        OAuthUserInfoDto oAuthUser = targetService.getUserInfo(code);
        Optional<Member> memberOpt = memberRepository.findByEmail(oAuthUser.getEmail());

        Map<String, Object> result = new HashMap<>();

        if (memberOpt.isPresent()) {
            Member m = memberOpt.get();

            // JWT 발급 및 Redis에 Refresh Token 저장
            String accessToken = jwtProvider.createAccessToken(m.getId());
            String refreshToken = jwtProvider.createRefreshToken(m.getId());
            redisTemplate.opsForValue().set("RT:" + m.getId(), refreshToken, Duration.ofDays(7));

            result.put("isRegistered", true);
            result.put("accessToken", accessToken);
            result.put("refreshToken", refreshToken);
            result.put("email", m.getEmail());
            result.put("nickname", m.getNickname());
        } else {
            result.put("isRegistered", false);
            result.put("provider", oAuthUser.getProvider());
            result.put("providerId", oAuthUser.getProviderId());
            result.put("email", oAuthUser.getEmail());
            result.put("nickname", oAuthUser.getNickname());
            result.put("profileImage", oAuthUser.getProfileImage());
        }

        return ResponseEntity.ok(result);
    }

	/**
	 * OAuth 신규 회원가입 완료: POST /api/v1/auth/oauth/register
	 */
	@PostMapping("/oauth/register")
    public ResponseEntity<Map<String, Object>> completeOAuthRegister(
            @RequestBody Map<String, String> request) {

        String email = request.get("email");
        String nickname = request.get("nickname");
        String provider = request.get("provider");
        String providerId = request.get("providerId");
        String profileImage = request.get("profileImage");
        String accountNumber = request.get("accountNumber");

        Member member = memberBO.addOAuthUser(email, nickname, provider, providerId, profileImage, accountNumber);

        // 가입 직후 JWT 발급 및 Redis에 Refresh Token 저장
        String accessToken = jwtProvider.createAccessToken(member.getId());
        String refreshToken = jwtProvider.createRefreshToken(member.getId());
        redisTemplate.opsForValue().set("RT:" + member.getId(), refreshToken, Duration.ofDays(7));

        Map<String, Object> result = new HashMap<>();
        result.put("accessToken", accessToken);
        result.put("refreshToken", refreshToken);
        result.put("email", member.getEmail());
        result.put("nickname", member.getNickname());

        return ResponseEntity.ok(result);
    }

	/**
	 * Access Token 재발급: POST /api/v1/auth/reissue?refreshToken=...
	 */
	@PostMapping("/reissue")
	public ResponseEntity<Map<String, Object>> reissue(@RequestParam("refreshToken") String refreshToken) {

		Long memberId = jwtProvider.getMemberId(refreshToken);
		String savedRefreshToken = redisTemplate.opsForValue().get("RT:" + memberId);

		if (savedRefreshToken == null || !savedRefreshToken.equals(refreshToken)) {
			throw new BusinessException(ErrorCode.INVALID_TOKEN);
		}

		String newAccessToken = jwtProvider.createAccessToken(memberId);

		Map<String, Object> response = new HashMap<>();
		response.put("code", 200);
		response.put("message", "토큰 재발급 완료");
		response.put("accessToken", newAccessToken);
		return ResponseEntity.ok(response);
	}

	/**
	 * 로그아웃 (Redis Refresh Token 삭제 + Access Token 블랙리스트 등록): POST
	 * /api/v1/auth/logout
	 */
	@PostMapping("/logout")
	public ResponseEntity<Map<String, Object>> logout(@LoginMember Long memberId,
			@RequestHeader(value = "Authorization", required = false) String authHeader) {

		redisTemplate.delete("RT:" + memberId);

		if (authHeader != null && !authHeader.isBlank()) {
			String accessToken = authHeader.startsWith("Bearer ") ? authHeader.substring(7).trim() : authHeader.trim();

			long remainingMs = jwtProvider.getRemainingExpirationMs(accessToken);
			if (remainingMs > 0) {
				redisTemplate.opsForValue().set("BL:" + accessToken, "logout", Duration.ofMillis(remainingMs));
			}
		}

		Map<String, Object> response = new HashMap<>();
		response.put("code", 200);
		response.put("message", "로그아웃 완료");
		return ResponseEntity.ok(response);
	}

}
