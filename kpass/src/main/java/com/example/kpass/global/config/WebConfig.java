package com.example.kpass.global.config;

import java.util.List;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.example.kpass.global.auth.LoginMemberArgumentResolver;

import lombok.RequiredArgsConstructor;

@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {
	private final LoginMemberArgumentResolver loginMemberArgumentResolver;
	
	/**
     * 스프링 MVC의 기본 파라미터 처리기 목록(resolvers)에
     * 우리가 직접 만든 LoginMemberArgumentResolver를 추가 등록함
     * (이 등록 과정이 없으면 @LoginMember를 붙여도 스프링이 인식하지 못해 null이 들어감)
     */

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(loginMemberArgumentResolver);
    }
}
