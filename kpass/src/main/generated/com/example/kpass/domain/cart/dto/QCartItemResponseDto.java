package com.example.kpass.domain.cart.dto;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.ConstructorExpression;
import javax.annotation.processing.Generated;

/**
 * com.example.kpass.domain.cart.dto.QCartItemResponseDto is a Querydsl
 * Projection type for CartItemResponseDto
 */
@Generated("com.querydsl.codegen.DefaultProjectionSerializer")
public class QCartItemResponseDto extends ConstructorExpression<CartItemResponseDto> {

	private static final long serialVersionUID = -685432235L;

	public QCartItemResponseDto(com.querydsl.core.types.Expression<Long> cartItemId,
			com.querydsl.core.types.Expression<Long> id, com.querydsl.core.types.Expression<String> externalId,
			com.querydsl.core.types.Expression<String> title, com.querydsl.core.types.Expression<String> category,
			com.querydsl.core.types.Expression<String> region, com.querydsl.core.types.Expression<String> venue,
			com.querydsl.core.types.Expression<String> posterUrl, com.querydsl.core.types.Expression<Integer> price,
			com.querydsl.core.types.Expression<java.time.LocalDate> startDate,
			com.querydsl.core.types.Expression<java.time.LocalDate> endDate,
			com.querydsl.core.types.Expression<Integer> quantity) {
		super(CartItemResponseDto.class,
				new Class<?>[] { long.class, long.class, String.class, String.class, String.class, String.class,
						String.class, String.class, int.class, java.time.LocalDate.class, java.time.LocalDate.class,
						int.class },
				cartItemId, id, externalId, title, category, region, venue, posterUrl, price, startDate, endDate,
				quantity);
	}

}
