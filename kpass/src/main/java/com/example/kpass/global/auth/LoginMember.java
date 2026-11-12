package com.example.kpass.global.auth;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

// @Target(ElementType.PARAMETER): 이 어노테이션은 클래스나 필드가 아닌 '메서드 파라미터' 앞에만 붙일 수 있음
@Target(ElementType.PARAMETER)

// @Retention(RetentionPolicy.RUNTIME): 컴파일 후에도 지워지지 않고 프로그램 실행 중(Runtime)까지 유지됨
@Retention(RetentionPolicy.RUNTIME)
public @interface LoginMember {
	
	// 로그인 필수 여부 옵션 (기본값 true)
    // - @LoginMember Long memberId          -> 비로그인 시 401 에러 발생
    // - @LoginMember(required = false) Long memberId -> 비로그인 시 에러 대신 null 전달 (공연 상세 조회 등
	boolean required() default true;
}
