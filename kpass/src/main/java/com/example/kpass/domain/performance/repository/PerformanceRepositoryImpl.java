package com.example.kpass.domain.performance.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Repository;

import com.example.kpass.domain.performance.dto.PerformanceSummaryDto;
import com.example.kpass.domain.performance.dto.QPerformanceSummaryDto;
import com.example.kpass.domain.performance.entity.QPerformance;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class PerformanceRepositoryImpl implements PerformanceRepositoryCustom{
	
	private final JPAQueryFactory queryFactory;
	
	private final QPerformance per = QPerformance.performance;
	
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
				.where(regionEq(region),per.endDate.goe(LocalDate.now()))
				.orderBy(per.startDate.asc())
				.limit(limit)
				.fetch();
				
	}
	
	private BooleanExpression regionEq(String region) {
		if (region == null || region.isBlank()) {
	        return null;
	    }
	    return per.region.stringValue().eq(region);
    }
	
	
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
}
