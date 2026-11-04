package com.example.kpass.domain.performance;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.kpass.domain.performance.dto.MainPageResponseDto;
import com.example.kpass.domain.performance.service.PerformanceBO;
import com.example.kpass.domain.performance.service.PerformanceService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/performances")
@RequiredArgsConstructor
public class PerformanceController {
	
	private final PerformanceService performanceService;
	
	private final PerformanceBO performanceBO;
	
	@PostMapping("/sync/kopis/")
	public ResponseEntity<String> syncKopisData(
			@RequestParam(defaultValue = "20260101") String stdate,
			@RequestParam(defaultValue = "20261231") String eddate){
		performanceBO.fetchAndSaveKopisData(stdate, eddate);
		
		return ResponseEntity.ok("KOPIS 데이터 동기화 작업이 성공적으로 실행되었습니다");
	}
	
	
	@GetMapping("/main")
	public ResponseEntity<MainPageResponseDto> getMainPage(
			@RequestParam(name = "region", required = false, defaultValue = "SEOUL") String region) {
		MainPageResponseDto result = performanceService.getMainPageData(region);
		
		return ResponseEntity.ok(result);
	}
	
	
	
}
