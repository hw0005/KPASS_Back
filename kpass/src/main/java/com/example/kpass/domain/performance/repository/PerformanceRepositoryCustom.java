package com.example.kpass.domain.performance.repository;

import java.util.List;

import com.example.kpass.domain.performance.dto.PerformanceSummaryDto;

public interface PerformanceRepositoryCustom {

	List<PerformanceSummaryDto> findHeroBanners();
	List<PerformanceSummaryDto> findHotTop10();
	List<PerformanceSummaryDto> findByRegionCustom(String region, int limit);
	List<PerformanceSummaryDto> findClosingSoon(int limit);
}
