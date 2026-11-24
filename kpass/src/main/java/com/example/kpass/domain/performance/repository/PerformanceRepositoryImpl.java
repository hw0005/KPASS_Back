package com.example.kpass.domain.performance.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import com.example.kpass.domain.performance.dto.PerformanceSummaryDto;
import com.example.kpass.domain.performance.dto.QPerformanceSummaryDto;
import com.example.kpass.domain.performance.dto.venue.PerformanceMapResponse;
import com.example.kpass.domain.performance.dto.venue.QPerformanceMapResponse;
import com.example.kpass.domain.performance.dto.venue.QVenueTargetDto;
import com.example.kpass.domain.performance.dto.venue.VenueTargetDto;
import com.example.kpass.domain.performance.entity.QPerformance;
import com.example.kpass.global.auth.LoginMemberArgumentResolver;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class PerformanceRepositoryImpl implements PerformanceRepositoryCustom{

    private final LoginMemberArgumentResolver loginMemberArgumentResolver;
	
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
		
		List<PerformanceSummaryDto> content = queryFactory
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
		System.out.println("DB 조회 건수: " + content.size());
		System.out.println("==========================================");
		
		// 전체 개수 조회 (Spring Data PageableExecutionUtils)
		JPAQuery<Long> countQuery = queryFactory
				.select(per.count())
				.from(per);
		
		return PageableExecutionUtils.getPage(content, pageable, countQuery :: fetchOne); // 요청 데이터, 페이지 사이즈, 전체 데이터
	}
	
	@Override
	public Page<PerformanceSummaryDto> getDataListBySearchThings(String title, String category, Pageable pageable) {
		List<PerformanceSummaryDto> content = queryFactory
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
				.where(
						eqCategory(category),
						containsTitle(title)
						)
				.offset(pageable.getOffset())
				.limit(pageable.getPageSize())
				.fetch();
		
		JPAQuery<Long> countQuery =  queryFactory
				.select(per.count())
				.from(per)
				.where(
						eqCategory(category),
						containsTitle(title)
				);
		
		return PageableExecutionUtils.getPage(content, pageable, countQuery :: fetchOne);
	}
	
	// 카테고리 조건 (전체선택 ALL이거나 빈값이면 null 반환)
	private BooleanExpression eqCategory(String category) {
		if(!StringUtils.hasText(category) || "ALL".equalsIgnoreCase(category)) {
			return null; // null 리턴 시 where절에서 QueryDSL이 자동 제거
		}
		return per.category.stringValue().eq(category);
	}
	
	// 제목 검색 조건 (빈값이거나 null이면 null 반환)
	private BooleanExpression containsTitle(String title) {
		if(!StringUtils.hasText(title)) {
			return null; // null을 리턴하면 QueryDSL이 where절에서 자동으로 제거함
		}
		// 1. 프론트에서 들어온 검색어의 모든 공백 제거 (예: "BIG BANG" -> "BIGBANG")
	    String cleanTitle = title.replaceAll("\\s+", "");

	    // 2. DB 컬럼(per.title)의 공백도 SQL replace 함수로 제거한 뒤 대소문자 구분 없이 비교
	    // SQL: WHERE LOWER(REPLACE(title, ' ', '')) LIKE '%bigbang%'
	    return Expressions.stringTemplate("replace({0}, ' ', '')", per.title)
	            .containsIgnoreCase(cleanTitle);
	}
	//------------------------------------------------------------
	//------------------------------------------------------------------------지도페이지-------
	
	// 1. 1개월 이내 활성 공연장(중복 제거) 조회
	@Override
	public List<VenueTargetDto> findAllVenuesWithoutCoordinates() {
		return queryFactory
				.select(new QVenueTargetDto(
						per.venue,
						per.region.stringValue()
				))
				.distinct()
				.from(per)
				.where(
						per.latitude.isNull(),
						per.venue.isNotNull()
				)
				.fetch();
	}
	
	// 2. 검증된 좌표를 동일 공연장, 지역 공연에 일괄 반영
	@Override
	@Transactional
	public long bulkUpdateCoordinates(String venue, String region, String address,
			Double latitude, Double longitude) {
		
		return queryFactory
				.update(per)
				.set(per.address, address)
				.set(per.latitude, latitude)
				.set(per.longitude, longitude)
				.where(
						per.venue.eq(venue),
						per.region.stringValue().eq(region),
						per.latitude.isNull()
				)
				.execute();
	}
	
	// 3. 지도 마커용 공연 목록 조회(카테고리 필터 + 오늘~1개월 내)
	public List<PerformanceMapResponse> findPerformanceForMap(LocalDate today, LocalDate oneMonthLater, String category) {
		return queryFactory
				.select(new QPerformanceMapResponse(
						per.id,
						per.externalId,
						per.title,
						per.category.stringValue(),
						per.venue,
						per.address,
						per.latitude,
						per.longitude,
						per.posterUrl,
						per.price,
						per.startDate,
						per.endDate
				))
				.from(per)
				.where(
						per.latitude.isNotNull(),
						per.longitude.isNotNull(),
						per.endDate.goe(today),
						per.startDate.loe(oneMonthLater),
						eqCategory(category)
				)
				.orderBy(per.startDate.asc(), per.id.desc())
				.fetch();
	}
}
