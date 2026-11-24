package com.example.kpass.domain.performance.dto.venue;

import com.querydsl.core.annotations.QueryProjection;

import lombok.Getter;

@Getter
public class VenueTargetDto {
	
	private final String venue;
	private final String region;
	
	@QueryProjection
	public VenueTargetDto(String venue, String region) {
		this.venue = venue;
		this.region = region;
	}
	
}
