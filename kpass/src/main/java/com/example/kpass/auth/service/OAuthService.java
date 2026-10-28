package com.example.kpass.auth.service;

import com.example.kpass.auth.dto.OAuthUserInfoDto;

public interface OAuthService {
	String getProvider(); // KAKAO NAVER GOOGLE
	
	OAuthUserInfoDto getUserInfo(String code);
}
