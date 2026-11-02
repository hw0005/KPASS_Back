package com.example.kpass.domain.performance.scheduler;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.example.kpass.domain.performance.service.PerformanceBO;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class PerformanceScheduler {
	private final PerformanceBO performanceBO;
	private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");
	
	// 서버 배포시
	/**
     * 매일 새벽 3시 0분 0초에 자동 실행 (Cron 표현식)
     * "초 분 시 일 월 요일"
     */
	@Scheduled(cron = "0 0 3 * * *")
	public void autoSyncKopisDate() {
		log.info("[Batch Scheduler] KOPIS 데이터 자동 수집 배치 시작");
		
		LocalDate today = LocalDate.now();
        // 최근 일주일간의 변동 데이터 반영 + 향후 30일 데이터 수집
        String stdate = today.minusDays(7).format(FORMATTER);
        String eddate = today.plusDays(30).format(FORMATTER);

        try {
            performanceBO.fetchAndSaveKopisData(stdate, eddate);
            log.info("[Batch Scheduler] KOPIS 데이터 자동 수집 완료");
        } catch (Exception e) {
            log.error("[Batch Scheduler] KOPIS 자동 수집 중 오류 발생", e);
        }
		
	}
	
}
