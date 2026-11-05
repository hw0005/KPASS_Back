package com.example.kpass.domain.performance.dto.searchpage;

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
public class SearchPageResponseDto {
	
	// 1. 다 가져가서 뿌리기
	private List<PerformanceSummaryDto> getAllDataListBySearchPage;
	private int currentPage; // 지금 페이지
    private int totalPages; // 총 페이지
    private long totalElements; // 총 갯수
    private boolean isLast; // 이거 마지막?

	
}
