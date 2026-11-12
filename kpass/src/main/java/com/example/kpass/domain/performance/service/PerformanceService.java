package com.example.kpass.domain.performance.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.kpass.domain.performance.dto.PerformanceSummaryDto;
import com.example.kpass.domain.performance.dto.mainpage.MainPageResponseDto;
import com.example.kpass.domain.performance.dto.searchpage.SearchPageResponseDto;
import com.example.kpass.domain.performance.repository.PerformanceRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)	
public class PerformanceService {
	
	private final PerformanceRepository repository;
	
	/**
	 * 메인페이지 조회하기
	 * @param selectedRegion
	 * @return
	 */
	public MainPageResponseDto getMainPageData(String selectedRegion) {
		String region = (selectedRegion != null && !selectedRegion.isBlank()) ? selectedRegion : "SEOUL";
		
		MainPageResponseDto result = MainPageResponseDto.builder()
				.heroBanners(repository.findHeroBanners())
				.hotPerformances(repository.findHotTop10())
				.regionalPerformances(repository.findByRegionCustom(region, 10))
				.closingSoonPerformances(repository.findClosingSoon(10))
				.build();
		
		return result;
	}
	
	
	// ---------------------------------------------검색페이지
	// 1. 다 가져오기
	public SearchPageResponseDto getAllDataListBySearchPage(Pageable pageable) {
		
		// Impl에서 계산된 Page<PerformanceSummaryDto>를 전달받음
		Page<PerformanceSummaryDto> pageResult = repository.getAllDataListBySearchPage(pageable);
		
		// JSON 껍데기(SearchPageResponseDto) 구조로 포장
		SearchPageResponseDto result = SearchPageResponseDto.builder()
				.getAllDataListBySearchPage(pageResult.getContent()) // 콘텐트 갖와
				.currentPage(pageResult.getNumber()) // 현재 페이지
				.totalPages(pageResult.getTotalPages()) // 총 페이지
				.totalElements(pageResult.getTotalElements()) // 총 요청 갯수
				.isLast(pageResult.isLast()) // 마지막?
				.build();
		
		return result;
	}
	
	
	// 2. 필터검색
	public SearchPageResponseDto getDataListBySearchThings(String title, String category, Pageable pageable) {
		
		Page<PerformanceSummaryDto> pageResult = repository.getDataListBySearchThings(title, category, pageable);
		
		SearchPageResponseDto result = SearchPageResponseDto.builder()
				.getAllDataListBySearchPage(pageResult.getContent())
				.currentPage(pageResult.getNumber())
				.totalPages(pageResult.getTotalPages())
				.totalElements(pageResult.getTotalElements())
				.isLast(pageResult.isLast())
				.build();
		
		return result;
	}
	// ---------------------------------------------검색페이지
	
}
