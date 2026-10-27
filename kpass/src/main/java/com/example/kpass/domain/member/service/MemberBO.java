package com.example.kpass.domain.member.service;

import java.util.Optional;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.kpass.domain.member.entity.Member;
import com.example.kpass.domain.member.repository.MemberRepository;
import com.example.kpass.domain.member.validator.MemberValidator;
import com.example.kpass.global.error.BusinessException;
import com.example.kpass.global.error.ErrorCode;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MemberBO {

	private final MemberRepository memberRepository;
	private final MemberValidator memberValidator;
	private final PasswordEncoder passwordEncoder;
	
	
	@Transactional(readOnly = true)
	public Member login(String email, String password) {
		Optional<Member> memberOpt = memberRepository.findByEmail(email);
		
		if(memberOpt.isEmpty()) {
			throw new BusinessException(ErrorCode.MEMBER_NOT_FOUND);
		}
		
		Member member = memberOpt.get();
		//(M004)
		boolean isPasswordMatch = passwordEncoder.matches(password, member.getPassword());
		
		if (!isPasswordMatch) {
			throw new BusinessException(ErrorCode.INVALID_PASSWORD);
		}
		
		return member;
	}
	

	/**
	 * 로컬 회원가입
	 * @param email
	 * @param password
	 * @param nickname
	 * @param accountNumber
	 * @return
	 */
	@Transactional
	public Member addLocalUser(String email, String password, String nickname, String accountNumber) {
		// db 검증
		memberValidator.validateDuplicateForRegister(email, nickname);
		
		// 비밀번호 해싱
		String hashedPassword = passwordEncoder.encode(password);

		// member에서 builder 이용한 삽입
		Member member = Member.createLocal(email, hashedPassword, nickname, accountNumber);

		// 저장
		return memberRepository.save(member);
	}

	/**
	 * OAuth 회원가입
	 * @param email
	 * @param originalNickname
	 * @param provider
	 * @param providerId
	 * @param profileImage
	 * @return
	 */
	@Transactional
	public Member addOAuthUser(String email, String originalNickname, String provider, String providerId,
			String profileImage) {
		memberValidator.validateEmailDuplicate(email);
		
		//우선 uniqueNickName==닉네임 중복 시
		String uniqueNickname = generateUniqueNickname(originalNickname);		
		
		Member member = Member.createOAuth(email, uniqueNickname, provider, providerId, profileImage);
	
		memberRepository.save(member);
		return member;
	}
	/**
	 * OAuth회원가입 때 닉네임 중복 시 난수 활용
	 * @param originalNickname
	 * @return
	 */
	private String generateUniqueNickname(String originalNickname) {
		String nickname = originalNickname;

		while (memberRepository.existsByNickname(nickname)) {
			int randomNumber = (int) (Math.random() * 900) + 100;
			nickname = originalNickname + randomNumber;
		}

		return nickname;
	}
}
