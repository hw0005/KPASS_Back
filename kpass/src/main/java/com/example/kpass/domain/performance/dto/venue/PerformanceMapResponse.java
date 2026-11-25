package com.example.kpass.domain.performance.dto.venue;

import java.time.LocalDate;

import com.querydsl.core.annotations.QueryProjection;

import lombok.Getter;

@Getter
public class PerformanceMapResponse {

	private Long id;
	private String externalId;
	private String title;
	private String category;
	private String venue;
	private String address;
	private Double latitude;
	private Double longitude;
	private String posterUrl;
	private int price;
	private LocalDate startDate;
	private LocalDate endDate;

	@QueryProjection
	public PerformanceMapResponse(Long id, String externalId, String title, String category, String venue,
			String address, Double latitude, Double longitude, String posterUrl, int price, LocalDate startDate,
			LocalDate endDate) {
		this.id = id;
		this.externalId = externalId;
		this.title = title;
		this.category = category;
		this.venue = venue;
		this.address = address;
		this.latitude = latitude;
		this.longitude = longitude;
		this.posterUrl = posterUrl;
		this.price = price;
		this.startDate = startDate;
		this.endDate = endDate;
	}

}
