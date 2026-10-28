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
public class NaveAuthService implements OAuthService {

	@Value("${oauth.providers.naver.client-id}")
	private String clientId;

	@Value("${oauth.providers.naver.client-secret}")
	private String clientSecret;

	@Value("${oauth.providers.naver.redirect-uri}")
	private String redirectUri;

	@Value("${oauth.providers.naver.token-url}")
	private String tokenUrl;

	@Value("${oauth.providers.naver.user-info-url}")
	private String userInfoUrl;

	private final RestTemplate restTemplate = new RestTemplate();

	// 어 형 네이버야
	@Override
	public String getProvider() {
		return "naver";
	}

	@Override
	public OAuthUserInfoDto getUserInfo(String code) {
		String accessToken = getAccessToken(code);

		HttpHeaders headers = new HttpHeaders();
		headers.setBearerAuth(accessToken);

		HttpEntity<Void> request = new HttpEntity<>(headers);
		ResponseEntity<Map> response = restTemplate.exchange(userInfoUrl, HttpMethod.GET, request, Map.class);

		Map<String, Object> body = response.getBody();
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
		params.add("client_secret", clientSecret);
		params.add("code", code);
		params.add("state", "naver_login_state"); // 네이버는 state 파라미터가 필수

		HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);
		ResponseEntity<Map> response = restTemplate.postForEntity(tokenUrl, request, Map.class);

		Map<String, Object> body = response.getBody();
		if (body != null && body.containsKey("access_token")) {
			return (String) body.get("access_token");
		}

		throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR);
	}

	@SuppressWarnings("unchecked")
	private OAuthUserInfoDto parseUserInfo(Map<String, Object> attributes) {
		// 네이버는 유저 데이터가 'response' 객체 내부에 존재함
		Map<String, Object> naverResponse = (Map<String, Object>) attributes.get("response");

		if (naverResponse == null) {
			throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR);
		}

		String providerId = String.valueOf(naverResponse.get("id"));
		String email = (String) naverResponse.get("email");
		String nickname = (String) naverResponse.get("name"); // 또는 naverResponse.get("nickname")
		String profileImage = (String) naverResponse.get("profile_image");

		if (email == null) {
			email = "naver_" + providerId + "@kpass.com";
		}
		if (nickname == null) {
			nickname = "네이버유저";
		}

		return OAuthUserInfoDto.builder()
				.provider("NAVER")
				.providerId(providerId)
				.email(email)
				.nickname(nickname)
				.profileImage(profileImage).build();
	}

}
