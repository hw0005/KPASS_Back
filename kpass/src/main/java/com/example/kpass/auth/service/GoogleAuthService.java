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
public class GoogleAuthService implements OAuthService {
	
	@Value("${oauth.providers.google.client-id}")
    private String clientId;

    @Value("${oauth.providers.google.client-secret}")
    private String clientSecret;

    @Value("${oauth.providers.google.redirect-uri}")
    private String redirectUri;

    @Value("${oauth.providers.google.token-url}")
    private String tokenUrl;

    @Value("${oauth.providers.google.user-info-url}")
    private String userInfoUrl;

    private final RestTemplate restTemplate = new RestTemplate();

    @Override
    public String getProvider() {
        return "google";
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
        params.add("redirect_uri", redirectUri);
        params.add("code", code);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);
        ResponseEntity<Map> response = restTemplate.postForEntity(tokenUrl, request, Map.class);

        Map<String, Object> body = response.getBody();
        if (body != null && body.containsKey("access_token")) {
            return (String) body.get("access_token");
        }

        throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR);
    }
    
    private OAuthUserInfoDto parseUserInfo(Map<String, Object> attributes) {
        String providerId = String.valueOf(attributes.get("id"));
        if (providerId == null || "null".equals(providerId)) {
            providerId = String.valueOf(attributes.get("sub"));
        }

        String email = (String) attributes.get("email");
        String nickname = (String) attributes.get("name");
        String profileImage = (String) attributes.get("picture");

        if (email == null) {
            email = "google_" + providerId + "@kpass.com";
        }
        if (nickname == null) {
            nickname = "구글유저";
        }

        return OAuthUserInfoDto.builder()
                .provider("GOOGLE")
                .providerId(providerId)
                .email(email)
                .nickname(nickname)
                .profileImage(profileImage)
                .build();
    }
	

}
