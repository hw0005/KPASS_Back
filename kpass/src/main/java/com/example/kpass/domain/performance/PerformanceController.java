package com.example.kpass.domain.performance;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.kpass.domain.performance.dto.mainpage.MainPageResponseDto;
import com.example.kpass.domain.performance.dto.searchpage.SearchPageResponseDto;
import com.example.kpass.domain.performance.dto.venue.PerformanceMapResponse;
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
	
	/**
	 * 메인페이지
	 * @param region
	 * @return
	 */
	@GetMapping("/main")
	public ResponseEntity<Map<String, Object>> getMainPage(
			@RequestParam(name = "region", required = false, defaultValue = "SEOUL") String region) {
		
		MainPageResponseDto mainData = performanceService.getMainPageData(region);
		
		Map<String, Object> result = new HashMap<>();
		result.put("success", true);
		result.put("message", "메인페이지공연 데이터 조회 성공");
		result.put("data", mainData);
		
		return ResponseEntity.ok(result);
	}
	
	
	/**
	 * 검색페이지1 : 들어오는 순간 뿌리기
	 */
	@PostMapping("/closing-soon")
	public ResponseEntity<Map<String, Object>> getSearchPage(
			@PageableDefault(size = 30) Pageable pageable) {
		SearchPageResponseDto searchData = performanceService.getAllDataListBySearchPage(pageable);
		
		Map<String, Object> result = new HashMap<>();
		result.put("code", 200);
		result.put("message", "검색페이지 기본 조회 완료");
		result.put("data", searchData);
		
		return ResponseEntity.ok(result);
	}
	
	@PostMapping("/search")
	public ResponseEntity<Map<String, Object>> search(
			@RequestParam(name= "title", required = false) String title,
			@RequestParam(name = "category", required = false) String category,
			@PageableDefault(size = 30) Pageable pageable) {
		
		// 백엔드 콘솔에 이 값이 어떻게 들어오는지 출력해보세요!
	    System.out.println(">>> [SEARCH PARAM] title = " + title + ", category = " + category);
		
		SearchPageResponseDto searchData = performanceService.getDataListBySearchThings(title, category, pageable);
		
		Map<String, Object> result = new HashMap<>();
		result.put("code", 200);
		result.put("message", "검색 필터 조회 완료");
		result.put("data", searchData);
		
		return ResponseEntity.ok(result);
	}
	
	
	
	/**
	 * 지도 및 상세페이지용 전체 공연장 좌표 일괄 동기화
	 * POST /api/v1/performances/map/sync
	 */
	@PostMapping("/map/sync")
	public ResponseEntity<Map<String, Object>> syncMapCoordinates() {
		Map<String, Long> syncResult = performanceBO.syncAllVenueCoordinates();
		
		Map<String, Object> result = new HashMap<>();
		
		result.put("code", 200);
		result.put("message", "공연장 좌표 동기화 완료");
		result.put("data", syncResult);
		
		return ResponseEntity.ok(result);
	}
	
	
	/**
	 * 지도 페이지 마커 조회 (오늘 기준 1개월 이내 + 카테고리 필터)
	 * GET /api/v1/performances/map?category=MUSICAL
	 */
	@GetMapping("/map")
	public ResponseEntity<Map<String, Object>> getPerformanceForMap(
			@RequestParam(name = "category", required = false, defaultValue = "ALL") String category) {
		
		List<PerformanceMapResponse> mapList = performanceBO.getPerformanceForMap(category);
		
		Map<String, Object> result = new HashMap<>();
		
		result.put("code", 200);
		result.put("message", "지도 공연 목록 조회 성공");
		result.put("data", mapList);
		
		return ResponseEntity.ok(result);
	}
}
