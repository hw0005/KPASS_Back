package com.example.kpass.domain.performance.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "performance")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Performance {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@Column(unique = true, length = 100)
	private String externalId;
	
	@Column(nullable = false, length = 200)
	private String title;
	
	@Column(nullable = false, length = 50)
	private String category;
	
	@Column(nullable = false, length=50)
	private String region;
	
	@Column(nullable = false, length = 100)
	private String venue; // 행사 장소
	
	@Column(length = 255)
	private String address;
	
	private Double latitude; // 위도 (Latitude)
    
    private Double longitude; // 경도 (Longitude)
    
	@Column(length = 500) 
	private String posterUrl;
	
	@Column(nullable = false)
	private Integer price;
	
	@OneToMany(mappedBy = "performance", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PerformanceSeatPrice> seatPrices = new ArrayList<>();
	
	@Column(nullable = false)
	private Integer stockQuantity; // 좌석 수
	
	@Column(nullable = false)
	private LocalDate startDate;
	
	@Column(nullable = false)
	private LocalDate endDate;

	@Column(nullable = false)
	private Integer viewCount = 0;
	
	@Column(nullable = false)
	private Integer likeCount = 0;
	
	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;
	
	@PrePersist
	public void prePersist() {
		this.createdAt = LocalDateTime.now();
		this.updatedAt = LocalDateTime.now();
	}
	
	@PreUpdate
	public void preUpdate() {
		this.updatedAt = LocalDateTime.now();
	}

	@Builder
	public Performance(Long id, String externalId, String title, String category, String region, String venue,
			String address, Double latitude, Double longitude, String posterUrl, Integer price, Integer stockQuantity,
			LocalDate startDate, LocalDate endDate, Integer viewCount, Integer likeCount, LocalDateTime createdAt,
			LocalDateTime updatedAt) {
		this.id = id;
		this.externalId = externalId;
		this.title = title;
		this.category = category;
		this.region = region;
		this.venue = venue;
		this.address = address;
		this.latitude = latitude;
		this.longitude = longitude;
		this.posterUrl = posterUrl;
		this.price = price;
		this.stockQuantity = stockQuantity != null ? stockQuantity : 100;
		this.startDate = startDate;
		this.endDate = endDate;
		this.viewCount = viewCount != null ? viewCount : 0;
		this.likeCount = likeCount != null ? likeCount : 0;
	}
	
	public static Performance createPerformance(
			String externalId,
			String title,
			String category,
			String region,
			String venue,
			String posterUrl,
			Integer price,
			List<PerformanceSeatPrice> seatPrices,
			LocalDate startDate,
			LocalDate endDate) {
		
		Performance performance = Performance.builder()
				.externalId(externalId)
				.title(title)
				.category(category)
				.region(region)
				.venue(venue)
				.posterUrl(posterUrl)
				.price(price)
				.startDate(startDate)
				.endDate(endDate)
				.viewCount(0)
				.likeCount(0)
				.build();
		
		if (seatPrices != null) {
            for (PerformanceSeatPrice seatPrice : seatPrices) {
                performance.addSeatPrice(seatPrice);
            }
        }
		return performance;
	}
	
	// 연관관계 편의 메서드 (두 줄의 관계 설정을 하나로 캡슐화)
	public void addSeatPrice(PerformanceSeatPrice seatPrice) {
	    this.seatPrices.add(seatPrice);   // 1. 부모 -> 자식 연결
	    seatPrice.setPerformance(this);   // 2. 자식 -> 부모 연결 (FK 관리)
	}
	
	
	
	
}
