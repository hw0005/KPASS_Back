package com.example.kpass.domain.performance.runner;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import com.example.kpass.domain.performance.service.PerformanceBO;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor

public class KopisDataInitializer implements ApplicationRunner {
	private final PerformanceBO performanceBO;

	// yaml의 kopis.init.enabled 설정값을 읽어옴 (기본값 false)
	@Value("${kopis.init.enabled:false}")
	private boolean initEnabled;

	private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");

	
	// 서버 배포 전	
	@Override
	public void run(ApplicationArguments args) {
		// 옵션이 true일 때만 서버 기동 시 자동 수집 실행
		if (!initEnabled) {
			log.info("[KOPIS Ingestion] kopis.init.enabled가 false이므로 초기 수집을 건너뜁니다.");
			return;
		}

		log.info("[KOPIS Ingestion] 🚀 서버 스타트업: KOPIS 초기 데이터 자동 수집을 시작합니다.");

		// 오늘 기준 한 달 치 범위 지정
		LocalDate today = LocalDate.now();
		String stdate = today.format(FORMATTER);
		String eddate = today.plusDays(30).format(FORMATTER);

		try {
			//fetchRecent5YearsData, fetchAndSaveKopisData
			performanceBO.fetchRecent5YearsData();
			log.info("[KOPIS Ingestion] ✅ KOPIS 초기 데이터 자동 수집이 완료되었습니다.");
		} catch (Exception e) {
			log.error("[KOPIS Ingestion] ❌ 데이터 자동 수집 중 오류 발생", e);
		}
	}
}
