package com.example.kpass.domain.performance.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;

import com.example.kpass.domain.performance.dto.PerformanceSummaryDto;
import com.example.kpass.domain.performance.dto.QPerformanceSummaryDto;
import com.example.kpass.domain.performance.entity.QPerformance;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class PerformanceRepositoryImpl implements PerformanceRepositoryCustom{
	
	private final JPAQueryFactory queryFactory;
	
	private final QPerformance per = QPerformance.performance;
	
	
	//----------------------------------------------------메인페이지---------------------------------------------
	@Override
	public List<PerformanceSummaryDto> findHeroBanners() {
		
		return queryFactory
				.select(new QPerformanceSummaryDto(
						per.externalId,
						per.title,
						per.posterUrl,
						per.venue,
						per.region,
						per.category,
						per.price,
						per.startDate,
						per.endDate
						))
				.from(per)
				.where(per.endDate.goe(LocalDate.now()))
				.orderBy(per.startDate.desc())
				.limit(5)
				.fetch();
	}
	
	@Override
	public List<PerformanceSummaryDto> findHotTop10() {
		return queryFactory
				.select(new QPerformanceSummaryDto(
						per.externalId,
						per.title,
						per.posterUrl,
						per.venue,
						per.region,
						per.category,
						per.price,
						per.startDate,
						per.endDate
						))
				.from(per)
				.orderBy(per.id.desc())
				.limit(10)
				.fetch();
	}
	
	@Override
	public List<PerformanceSummaryDto> findByRegionCustom(String region, int limit) {
		return queryFactory
				.select(new QPerformanceSummaryDto(
						per.externalId,
						per.title,
						per.posterUrl,
						per.venue,
						per.region,
						per.category,
						per.price,
						per.startDate,
						per.endDate
						))
				.from(per)
				.where(per.region.stringValue().eq(region) ,per.endDate.goe(LocalDate.now()))
				.orderBy(per.startDate.asc())
				.limit(limit)
				.fetch();
				
	}
	
	
	@Override
	public List<PerformanceSummaryDto> findClosingSoon(int limit) {
		LocalDate today = LocalDate.now();
        LocalDate thirtyDaysLater = today.plusDays(30);	
		
		return queryFactory
				.select(new QPerformanceSummaryDto(
						per.externalId,
						per.title,
						per.posterUrl,
						per.venue,
						per.region,
						per.category,
						per.price,
						per.startDate,
						per.endDate
				))
				.from(per)
				.where(per.endDate.between(today, thirtyDaysLater))
				.orderBy(per.startDate.asc())
				.limit(limit)
				.fetch();
	}
	//----------------------------------------------------검색페이지---------------------------------------------
	// 페이징 하기
	@Override
	public Page<PerformanceSummaryDto> getAllDataListBySearchPage(Pageable pageable) {
		LocalDate today = LocalDate.now();
		
		List<PerformanceSummaryDto> result = queryFactory
				.select(new QPerformanceSummaryDto(
						per.externalId,
						per.title,
						per.posterUrl,
						per.venue,
						per.region,
						per.category,
						per.price,
						per.startDate,
						per.endDate
				))
				.from(per)
				.orderBy(
						// 마감일 >= 오늘 중에서 진행 중인 것 위로, 마감인 걸 아래로(내림차순)
						// 진행중인 공연 중에서는 마감일 임박한 것 (오름차순)
						new CaseBuilder().when(per.endDate.goe(today)).then(1).otherwise(0).desc(),
		                per.endDate.asc()
				)
				.offset(pageable.getOffset()) // 페이지 번호 위치
				.limit(pageable.getPageSize()) // 1페이지 당 개수 (30개)
				.fetch();
		
		// DB에서 진짜 몇 건이 뽑혀 나왔는지 콘솔 출력
		System.out.println("==========================================");
		System.out.println("DB 조회 건수: " + result.size());
		System.out.println("==========================================");
		
		// 전체 개수 조회 (Spring Data PageableExecutionUtils)
		JPAQuery<Long> countQuery = queryFactory
				.select(per.count())
				.from(per);
		
		return PageableExecutionUtils.getPage(result, pageable, countQuery :: fetchOne); // 요청 데이터, 페이지 사이즈, 전체 데이터
	}
}
