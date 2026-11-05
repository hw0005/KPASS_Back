package com.example.kpass.domain.auth.service;

import com.example.kpass.domain.auth.dto.OAuthUserInfoDto;

public interface OAuthService {
	String getProvider(); // KAKAO NAVER GOOGLE
	
	OAuthUserInfoDto getUserInfo(String code);
}
