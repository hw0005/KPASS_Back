package com.example.kpass.domain.member.repository;

public interface MemberRepositoryCustom {
	
	// QueryDSL용
	boolean existsByEmail(String email);

}
