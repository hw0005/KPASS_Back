package com.example.kpass.domain.cart.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class CartAddRequestDto {
	private Long performanceId;
	private String externalId;
	private Integer quantity;
}
