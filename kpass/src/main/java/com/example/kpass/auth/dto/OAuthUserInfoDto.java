package com.example.kpass.auth.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class OAuthUserInfoDto {
	private String provider; // 카카오 네이버 구글
	private String providerId; // 소셜고유 id
	private String email; // 이메일
	private String nickname;
	private String profileImage; //이미지URL
	private String accountNumber;
}
