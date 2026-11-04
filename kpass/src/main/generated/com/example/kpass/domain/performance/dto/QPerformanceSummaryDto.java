package com.example.kpass.domain.performance.dto;

import javax.annotation.processing.Generated;

import com.querydsl.core.types.ConstructorExpression;

/**
 * com.example.kpass.domain.performance.dto.QPerformanceSummaryDto is a Querydsl
 * Projection type for PerformanceSummaryDto
 */
@Generated("com.querydsl.codegen.DefaultProjectionSerializer")
public class QPerformanceSummaryDto extends ConstructorExpression<PerformanceSummaryDto> {

	private static final long serialVersionUID = 1927024917L;

	public QPerformanceSummaryDto(com.querydsl.core.types.Expression<String> externalId,
			com.querydsl.core.types.Expression<String> title, com.querydsl.core.types.Expression<String> posterUrl,
			com.querydsl.core.types.Expression<String> venue, com.querydsl.core.types.Expression<String> region,
			com.querydsl.core.types.Expression<String> category, com.querydsl.core.types.Expression<Integer> price,
			com.querydsl.core.types.Expression<java.time.LocalDate> startDate,
			com.querydsl.core.types.Expression<java.time.LocalDate> endDate) {
		super(PerformanceSummaryDto.class,
				new Class<?>[] { String.class, String.class, String.class, String.class, String.class, String.class,
						Integer.class, java.time.LocalDate.class, java.time.LocalDate.class },
				externalId, title, posterUrl, venue, region, category, price, startDate, endDate);
	}

}
