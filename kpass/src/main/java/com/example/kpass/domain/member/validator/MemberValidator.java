package com.example.kpass.domain.member.validator;

import java.util.Optional;

import org.springframework.stereotype.Component;

import com.example.kpass.domain.member.entity.Member;
import com.example.kpass.domain.member.repository.MemberRepository;
import com.example.kpass.global.error.BusinessException;
import com.example.kpass.global.error.ErrorCode;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class MemberValidator {
	
	private final MemberRepository memberRepository;
	
	public void validateDuplicateForRegister(String email, String nickname) {
        Optional<Member> existingMemberOpt = memberRepository.findByEmailOrNickname(email, nickname);

        if (existingMemberOpt.isPresent()) {
            Member existingMember = existingMemberOpt.get();

            if (existingMember.getEmail().equals(email)) {
                throw new BusinessException(ErrorCode.DUPLICATE_EMAIL);
            }
            if (existingMember.getNickname().equals(nickname)) {
                throw new BusinessException(ErrorCode.DUPLICATE_NICKNAME);
            }
        }
    }

    public void validateEmailDuplicate(String email) {
        if (memberRepository.existsByEmail(email)) {
            throw new BusinessException(ErrorCode.DUPLICATE_EMAIL);
        }
    }
}
