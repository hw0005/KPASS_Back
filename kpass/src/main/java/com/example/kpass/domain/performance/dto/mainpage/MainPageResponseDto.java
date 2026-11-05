package com.example.kpass.domain.performance.dto.mainpage;

import java.util.List;

import com.example.kpass.domain.performance.dto.PerformanceSummaryDto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MainPageResponseDto {
	// 리스트로 응답받기
	private List<PerformanceSummaryDto> heroBanners; // 히어로배너(초대형 공연) TOP 5(D-Day카운트다운)
	private List<PerformanceSummaryDto> hotPerformances; // 실시간 인기 가장 많은 HOT TOP 10
	private List<PerformanceSummaryDto> regionalPerformances; // 지역별 추천
	private List<PerformanceSummaryDto> closingSoonPerformances; // 마감임박 TOP 5
}
