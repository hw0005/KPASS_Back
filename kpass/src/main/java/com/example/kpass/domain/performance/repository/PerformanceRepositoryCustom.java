package com.example.kpass.domain.performance.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.example.kpass.domain.performance.dto.PerformanceSummaryDto;
import com.example.kpass.domain.performance.dto.venue.PerformanceMapResponse;
import com.example.kpass.domain.performance.dto.venue.VenueTargetDto;

public interface PerformanceRepositoryCustom {

	// 메인페이지
	List<PerformanceSummaryDto> findHeroBanners();
	List<PerformanceSummaryDto> findHotTop10();
	List<PerformanceSummaryDto> findByRegionCustom(String region, int limit);
	List<PerformanceSummaryDto> findClosingSoon(int limit);
	
	
	
	//검색페이지
	Page<PerformanceSummaryDto> getAllDataListBySearchPage(Pageable pageable);
	Page<PerformanceSummaryDto> getDataListBySearchThings(String title, String category, Pageable pageable);
	
	
	// 공연장 찍기
	// 1. 1개월 이내 활성 공연장(중복 제거) 조회
	List<VenueTargetDto> findAllVenuesWithoutCoordinates();
	// 2. 검증된 좌표를 동일 공연장, 지역 공연에 일괄 반영
	long bulkUpdateCoordinates(String venue, String region, String address, Double latitude, Double longitude);
	// 3. 지도 마커용 공연 목록 조회(카테고리 필터 + 오늘~1개월 내)
	List<PerformanceMapResponse> findPerformanceForMap(LocalDate today, LocalDate oneMonthLater, String category);
	
	
	
}
