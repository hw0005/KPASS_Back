package com.example.kpass.domain.performance;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.kpass.domain.performance.service.PerformanceBO;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/admin/performance")
@RequiredArgsConstructor
public class PerformanceController {
	
	
	private final PerformanceBO performanceBO;
	
	@PostMapping("/sync/kopis/")
	public ResponseEntity<String> syncKopisData(
			@RequestParam(defaultValue = "20260101") String stdate,
			@RequestParam(defaultValue = "20261231") String eddate){
		performanceBO.fetchAndSaveKopisData(stdate, eddate);
		
		return ResponseEntity.ok("KOPIS 데이터 동기화 작업이 성공적으로 실행되었습니다");
	}
}
