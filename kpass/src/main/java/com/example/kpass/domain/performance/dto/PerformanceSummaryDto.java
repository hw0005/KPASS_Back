package com.example.kpass.domain.performance.dto;

import java.time.LocalDate;

import com.querydsl.core.annotations.QueryProjection;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Builder
public class PerformanceSummaryDto {
	// 메인페이지 조회 QueryDSL로 조회하기 -> 프로젝션 매핑 DTO
	private String externalId;
	private String title;
	private String posterUrl;
	private String venue;
	private String region;
	private String category;
	private Integer price;
	private LocalDate startDate;
	private LocalDate endDate;
	
	@QueryProjection
	public PerformanceSummaryDto(String externalId, String title, String posterUrl, 
			String venue, String region, String category, Integer price, 
			LocalDate startDate, LocalDate endDate) {
		this.externalId = externalId;
		this.title = title;
		this.posterUrl = posterUrl;
		this.venue = venue;
		this.region = region;
		this.category = category;
		this.price = (price != null) ? price : 0; // null이면 0으로 안전하게 대입
		this.startDate = startDate;
		this.endDate = endDate;
	}
	
	

}
