package com.example.kpass.domain.performance.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.kpass.domain.performance.dto.MainPageResponseDto;
import com.example.kpass.domain.performance.repository.PerformanceRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)	
public class PerformanceService {
	
	// 조회하기
	
	private final PerformanceRepository repository;
	
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
	
	
	
}
