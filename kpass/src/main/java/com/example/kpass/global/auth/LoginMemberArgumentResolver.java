package com.example.kpass.global.auth;

import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import com.example.kpass.global.error.BusinessException;
import com.example.kpass.global.error.ErrorCode;

import jakarta.servlet.http.HttpServletRequest;

@Component
//HandlerMethodArgumentResolver: 컨트롤러의 파라미터 값을 동적으로 생성해 주입해 주는 스프링 인터페이스
public class LoginMemberArgumentResolver implements HandlerMethodArgumentResolver {

	// 이름표를 보고 값을 꽂아주는 배달부

	/**
	 * [1단계 질문] 스프링이 컨트롤러를 실행하기 전에 모든 파라미터를 하나씩 가리키며 물어봄: "이 파라미터를 네가 처리할 수 있니?"
	 */
	@Override
	public boolean supportsParameter(MethodParameter parameter) {
		// 조건 1: 파라미터 앞에 @LoginMember 어노테이션이 붙어 있는가?
		boolean hasAnnotation = parameter.hasParameterAnnotation(LoginMember.class);
		// 조건 2: 파라미터의 타입이 Long 인가?
		boolean isLongType = parameter.getParameterType().equals(Long.class);

		// 두 조건 모두 true일 때만 아래 resolveArgument()를 실행함
		return hasAnnotation && isLongType;
	}

	/**
	 * [2단계 실행] 위에서 true를 반환한 파라미터에 실제로 어떤 값을 넣어줄지 결정하는 메서드
	 */
	@Override
	public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
			NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {

		// 현재 요청의 HttpServletRequest 객체를 꺼냄
		HttpServletRequest request = (HttpServletRequest) webRequest.getNativeRequest();

		// 앞서 JwtAuthenticationFilter(5번 주석)가 붙여두었던 "loginMemberId" 값을 꺼냄
		Object memberId = request.getAttribute("loginMemberId");

		// 파라미터에 붙은 @LoginMember 어노테이션의 옵션(required) 확인
		LoginMember annotation = parameter.getParameterAnnotation(LoginMember.class);

		// 토큰을 안 보냈거나 비로그인 상태라서 memberId가 null인 경우
		if (memberId == null) {
			// @LoginMember(required = false)로 설정된 API라면 에러를 내지 않고 null을 그대로 넘겨줌
			if (annotation != null && !annotation.required()) {
				return null;
			}
			// 기본값(required = true)인 API라면 로그인이 필수이므로 401 UNAUTHORIZED 예외 발생
			// -> GlobalExceptionHandler가 이 예외를 잡아서 프론트에 깔끔한 JSON 에러 응답을 내려줌
			throw new BusinessException(ErrorCode.UNAUTHORIZED);
		}

		// 정상 로그인 상태라면 꺼낸 회원 번호(예: 1L)를 컨트롤러의 memberId 파라미터에 꽂아줌
		return (Long) memberId;
	}
}
