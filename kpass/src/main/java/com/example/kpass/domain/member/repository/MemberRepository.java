package com.example.kpass.domain.member.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.kpass.domain.member.entity.Member;

@Repository
public interface MemberRepository extends JpaRepository<Member, Long>, MemberRepositoryCustom{
	// 기본 다 있음
	
	//-------------------- 로그인, 회원가입 시 로컬과 OAuth 로그인 중복-------------------------
	// 로컬, OAuth(이메일, 닉네임 각각)
    boolean existsByEmail(String email);
    boolean existsByNickname(String nickname);
    
    // 로컬 로그인
    Optional<Member> findByEmail(String email);
    
	
	//-----------------------------------------------------------

}
