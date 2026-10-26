package com.example.kpass.domain.test.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "member")
@Getter
@Setter
@NoArgsConstructor
public class TestEntity {
	
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String nickname;

    private String password;
    private String profileImage;

    @Column(nullable = false, length = 20)
    private String provider = "LOCAL";

    private String providerId;
    private String encryptedAccountNumber;

    @Column(nullable = false, length = 20)
    private String role = "USER";
}
