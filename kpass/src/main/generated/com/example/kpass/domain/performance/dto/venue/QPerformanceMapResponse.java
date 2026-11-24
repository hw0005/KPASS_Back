package com.example.kpass.domain.performance.dto.venue;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.ConstructorExpression;
import javax.annotation.processing.Generated;

/**
 * com.example.kpass.domain.performance.dto.venue.QPerformanceMapResponse is a
 * Querydsl Projection type for PerformanceMapResponse
 */
@Generated("com.querydsl.codegen.DefaultProjectionSerializer")
public class QPerformanceMapResponse extends ConstructorExpression<PerformanceMapResponse> {

	private static final long serialVersionUID = 1883328480L;

	public QPerformanceMapResponse(com.querydsl.core.types.Expression<Long> id, com.querydsl.core.types.Expression<String> externalId,
			com.querydsl.core.types.Expression<String> title, com.querydsl.core.types.Expression<String> category,
			com.querydsl.core.types.Expression<String> venue, com.querydsl.core.types.Expression<String> address,
			com.querydsl.core.types.Expression<Double> latitude, com.querydsl.core.types.Expression<Double> longitude,
			com.querydsl.core.types.Expression<String> posterUrl, com.querydsl.core.types.Expression<Integer> price,
			com.querydsl.core.types.Expression<java.time.LocalDate> startdate,
			com.querydsl.core.types.Expression<java.time.LocalDate> endDate) {
		super(PerformanceMapResponse.class,
				new Class<?>[] { long.class, String.class, String.class, String.class, String.class, String.class, double.class,
						double.class, String.class, int.class, java.time.LocalDate.class, java.time.LocalDate.class },
				id, externalId, title, category, venue, address, latitude, longitude, posterUrl, price, startdate, endDate);
	}

}
