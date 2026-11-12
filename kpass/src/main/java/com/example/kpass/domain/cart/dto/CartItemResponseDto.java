package com.example.kpass.domain.cart.dto;

import java.time.LocalDate;

import com.querydsl.core.annotations.QueryProjection;

import lombok.Getter;

@Getter
public class CartItemResponseDto {
	private final Long cartItemId;
	private final Long id;
	private final String externalId;
	private final String title;
	private final String category;
	private final String region;
	private final String venue;
	private final String posterUrl;
	private final Integer price;
	private final LocalDate startDate;
	private final LocalDate endDate;
	private final int quantity;
	
	@QueryProjection
	public CartItemResponseDto(
			Long cartItemId, Long id,
			String externalId, String title,
			String category, String region,
			String venue, String posterUrl, 
			Integer price, LocalDate startDate,
			LocalDate endDate, int quantity) {
		
		this.cartItemId = cartItemId;
		this.id = id;
		this.externalId = externalId;
		this.title = title;
		this.category = category;
		this.region = region;
		this.venue = venue;
		this.posterUrl = posterUrl;
		this.price = price;
		this.startDate = startDate;
		this.endDate = endDate;
		this.quantity = quantity;
	}
	
	
	
	
}
