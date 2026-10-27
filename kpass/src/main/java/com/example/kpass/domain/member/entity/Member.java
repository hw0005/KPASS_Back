package com.example.kpass.domain.member.entity;

import java.time.LocalDateTime;

import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.example.kpass.global.converter.AccountNumberConverter;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "member")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Member {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "email", nullable = false, unique = true, length = 100)
	private String email;

	@Column(name = "password", length = 255)
	private String password;

	@Column(name = "nickname", nullable = false, length = 50)
	private String nickname;

	@Column(name = "profileImage", length = 500)
	private String profileImage;

	@ColumnDefault("'LOCAL'")
	@Column(name = "provider", nullable = false, length = 20)
	private String provider = "LOCAL";

	@Column(name = "providerId", length = 255)
	private String providerId;

	// AttributeConverter를 통해 DB 저장 시 AES-256 자동 암호화, 조회 시 자동 복호화
	@Convert(converter = AccountNumberConverter.class)
	@Column(name = "encryptedAccountNumber", length = 255)
	private String encryptedAccountNumber;

	@ColumnDefault("'USER'")
	@Column(name = "role", nullable = false, length = 20)
	private String role = "USER";
	
    @Version
    private Long version;
    
    
	@CreationTimestamp
	@Column(name = "createdAt", nullable = false, updatable = false)
	private LocalDateTime createdAt;

	@UpdateTimestamp
	@Column(name = "updatedAt", nullable = false)
	private LocalDateTime updatedAt;
	
	@Builder
    public Member(Long id, String email, String password, String nickname, 
                        String profileImage, String provider, String providerId, 
                        String encryptedAccountNumber, String role, 
                        LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.email = email;
        this.password = password;
        this.nickname = nickname;
        this.profileImage = profileImage;
        this.provider = provider != null ? provider : "LOCAL";
        this.providerId = providerId;
        this.encryptedAccountNumber = encryptedAccountNumber;
        this.role = role != null ? role : "USER";
        this.version = version;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }
	
	// --- DDD 도메인 생성 메서드 (Entity 내부로 응집) ---

    // 일반 회원 생성
    public static Member createLocal(String email, String hashedPassword, String nickname, String accountNumber) {
        return Member.builder()
                .email(email)
                .password(hashedPassword)
                .nickname(nickname)
                .encryptedAccountNumber(accountNumber)
                .provider("LOCAL")
                .role("USER")
                .build();
    }

    // OAuth 회원 생성
    public static Member createOAuth(String email, String uniqueNickname, String provider, String providerId, String profileImage) {
        return Member.builder()
                .email(email)
                .nickname(uniqueNickname)
                .profileImage(profileImage)
                .provider(provider)
                .providerId(providerId)
                .role("USER")
                .build();
    }
}
