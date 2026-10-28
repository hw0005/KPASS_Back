package com.example.kpass.auth.service;

import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import com.example.kpass.auth.dto.OAuthUserInfoDto;
import com.example.kpass.global.error.BusinessException;
import com.example.kpass.global.error.ErrorCode;

@Service
public class KaKaoAuthService implements OAuthService {
	@Value("${oauth.providers.kakao.client-id}")
	private String clientId;

	@Value("${oauth.providers.kakao.redirect-uri}")
	private String redirectUri;

	@Value("${oauth.providers.kakao.token-url}")
	private String tokenUrl;

	@Value("${oauth.providers.kakao.user-info-url}")
	private String userInfoUrl;

	private final RestTemplate restTemplate = new RestTemplate();

	// 너 카카오다 명시
	@Override
	public String getProvider() {
		return "kakao";
	}

	@Override
	public OAuthUserInfoDto getUserInfo(String code) {
		// [확인 1] 프론트엔드가 보내준 code 수신 확인
		System.out.println("==========================================");
		System.out.println("[백엔드 Step 1] 프론트에서 넘어온 인가 코드: " + code);

		String accessToken = getAccessToken(code);

		// [확인 2] code를 카카오에 넘겨받은 access_token 확인
		System.out.println("[백엔드 Step 2] 카카오에서 발급받은 access_token: " + accessToken);

		HttpHeaders headers = new HttpHeaders();
		headers.setBearerAuth(accessToken);
		headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

		HttpEntity<Void> request = new HttpEntity<>(headers);
		ResponseEntity<Map> response = restTemplate.exchange(userInfoUrl, HttpMethod.GET, request, Map.class);

		Map<String, Object> body = response.getBody();

		// [확인 3] 카카오가 넘겨준 유저 정보 원본 JSON Map 확인
		System.out.println("[백엔드 Step 3] 카카오 유저 원본 데이터: " + body);
		System.out.println("==========================================");

		if (body == null) {
			throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR);
		}

		return parseUserInfo(body);
	}

	private String getAccessToken(String code) {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

		MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
		params.add("grant_type", "authorization_code");
		params.add("client_id", clientId);
		params.add("redirect_uri", redirectUri);
		params.add("code", code);

		HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);

		try {
			ResponseEntity<Map> response = restTemplate.postForEntity(tokenUrl, request, Map.class);

			Map<String, Object> body = response.getBody();
			if (body != null && body.containsKey("access_token")) {
				return (String) body.get("access_token");
			}
		} catch (org.springframework.web.client.HttpClientErrorException e) {
			// 카카오가 보낸 진짜 에러 원인 출력 (예: {"error":"invalid_grant","error_code":"KOE320"...})
			System.err.println("=== 카카오 토큰 발급 에러 상세 ===");
			System.err.println(e.getResponseBodyAsString());
			System.err.println("=================================");
			throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR);
		}

		throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR);
	}

	// 필요한 정보만 추출
	@SuppressWarnings("unchecked")
	private OAuthUserInfoDto parseUserInfo(Map<String, Object> attributes) {
		String providerId = String.valueOf(attributes.get("id"));
		Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");

		String email = null;
		String nickname = null;
		String profileImage = null;

		if (kakaoAccount != null) {
			if (kakaoAccount.get("email") != null) {
				email = (String) kakaoAccount.get("email");
			}
			Map<String, Object> profile = (Map<String, Object>) kakaoAccount.get("profile");
			if (profile != null) {
				if (profile.get("nickname") != null) {
					nickname = (String) profile.get("nickname");
				}
				if (profile.get("profile_image_url") != null) {
					profileImage = (String) profile.get("profile_image_url");
				}
			}
		}

		if (email == null) {
			email = "kakao_" + providerId + "@kpass.com";
		}
		if (nickname == null) {
			nickname = "카카오유저";
		}

		return OAuthUserInfoDto.builder()
				.provider("KAKAO")
				.providerId(providerId)
				.email(email)
				.nickname(nickname)
				.profileImage(profileImage).build();
	}
}
