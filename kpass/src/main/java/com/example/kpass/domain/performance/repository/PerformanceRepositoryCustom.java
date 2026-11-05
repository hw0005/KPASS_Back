package com.example.kpass.domain.performance.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.example.kpass.domain.performance.dto.PerformanceSummaryDto;

public interface PerformanceRepositoryCustom {

	// 메인페이지
	List<PerformanceSummaryDto> findHeroBanners();
	List<PerformanceSummaryDto> findHotTop10();
	List<PerformanceSummaryDto> findByRegionCustom(String region, int limit);
	List<PerformanceSummaryDto> findClosingSoon(int limit);
	
	
	
	//검색페이지
	Page<PerformanceSummaryDto> getAllDataListBySearchPage(Pageable pageable);
}
