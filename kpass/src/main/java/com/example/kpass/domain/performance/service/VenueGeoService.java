package com.example.kpass.domain.performance.service;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.example.kpass.domain.performance.dto.venue.VenueTargetDto;
import com.example.kpass.domain.performance.repository.PerformanceRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class VenueGeoService {

	private final PerformanceRepository performanceRepository;
	private final RestTemplate restTemplate = new RestTemplate();

	@Value("${oauth.providers.kakao.client-id}")
	private String kakaoRestApiKey;

	private static final String KAKAO_KEYWORD_URL = "https://dapi.kakao.com/v2/local/search/keyword.json";

	private final Map<String, GeoResult> geoCache = new ConcurrentHashMap<>();
	private final Set<String> failedCache = ConcurrentHashMap.newKeySet();
	
	// 1. 클래스 필드에 ObjectMapper 추가
	private final ObjectMapper objectMapper = new ObjectMapper();

	@Getter
	@AllArgsConstructor
	public static class GeoResult {
		private final String address;
		private final Double latitude;
		private final Double longitude;
	}

	/**
	 * DB 내 좌표가 비어 있는 모든 고유 공연장의 주소를 일괄 보강 (상세페이지 + 지도 공용)
	 */
	public Map<String, Long> syncAllVenueCoordinates() {
		List<VenueTargetDto> targets = performanceRepository.findAllVenuesWithoutCoordinates();

		log.info("=== [전체 공연장 좌표 보강 시작] 대상 고유 공연장: {}곳 ===", targets.size());

		long matchedVenues = 0;
		long updatedPerformances = 0;

		for (VenueTargetDto target : targets) {
			GeoResult geo = resolveCoordinates(target.getVenue(), target.getRegion());

			if (geo != null) {
				long updated = performanceRepository.bulkUpdateCoordinates(target.getVenue(), target.getRegion(),
						geo.getAddress(), geo.getLatitude(), geo.getLongitude());
				matchedVenues++;
				updatedPerformances += updated;
			}

			try {
				Thread.sleep(50);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				break;
			}
		}

		log.info("=== [전체 공연장 좌표 보강 완료] 매핑 공연장: {}/{}곳, 반영 공연: {}건 ===", matchedVenues, targets.size(),
				updatedPerformances);

		Map<String, Long> summary = new HashMap<>();
		summary.put("targetVenues", (long) targets.size());
		summary.put("matchedVenues", matchedVenues);
		summary.put("updatedPerformances", updatedPerformances);
		return summary;
	}

	/**
	 * 공연장명 + 지역 기반 좌표 변환 (단건 KOPIS 수집 시에도 재사용)
	 */
	public GeoResult resolveCoordinates(String rawVenue, String region) {
		if (rawVenue == null || rawVenue.isBlank()) {
			return null;
		}

		String cacheKey = region + ":" + rawVenue;
		if (geoCache.containsKey(cacheKey)) {
			return geoCache.get(cacheKey);
		}
		if (failedCache.contains(cacheKey)) {
			return null;
		}

		String regionKo = convertRegionToKorean(region);
		String cleanedVenue = rawVenue.replaceAll("\\s*\\(.*?\\)", "").trim();

		// 1차 시도: 지역명 + 정제된 공연장명 (예: "서울 극장 동국")
		String primaryQuery = regionKo.isEmpty() ? cleanedVenue : regionKo + " " + cleanedVenue;
		GeoResult result = callKakaoApiWithRegionCheck(primaryQuery, regionKo);

		// 2차 폴백: 실패 시 공연장명 단독 검색 후 주소에 해당 지역이 포함되는지 검증
		if (result == null && !regionKo.isEmpty()) {
			result = callKakaoApiWithRegionCheck(cleanedVenue, regionKo);
		}

		if (result != null) {
			geoCache.put(cacheKey, result);
		} else {
			failedCache.add(cacheKey);
		}
		return result;
	}

	private GeoResult callKakaoApiWithRegionCheck(String query, String expectedRegionKo) {
		try {
			URI uri = UriComponentsBuilder.fromUriString(KAKAO_KEYWORD_URL).queryParam("query", query)
					.queryParam("size", 3).build().encode(StandardCharsets.UTF_8).toUri();

			HttpHeaders headers = new HttpHeaders();
			headers.set("Authorization", "KakaoAK " + kakaoRestApiKey);

			
			ResponseEntity<String> response = restTemplate.exchange(uri, HttpMethod.GET, new HttpEntity<>(headers),
					String.class);
			String rawBody = response.getBody();
			if (rawBody == null || rawBody.isBlank()) {
				return null;
			}

			JsonNode body = objectMapper.readTree(rawBody);
			if (!body.has("documents")) {
				return null;
			}

			JsonNode documents = body.get("documents");
			if (!documents.isArray() || documents.isEmpty()) {
				return null;
			}

			for (JsonNode doc : documents) {
				String roadAddress = doc.path("road_address_name").asText("");
				String jibunAddress = doc.path("address_name").asText("");
				String address = !roadAddress.isBlank() ? roadAddress : jibunAddress;

				if (address.isBlank()) {
					continue;
				}

				// 지역 검증 필터: 오매칭 방지
				if (!expectedRegionKo.isEmpty() && !address.contains(expectedRegionKo)) {
					continue;
				}

				String xStr = doc.path("x").asText("");
				String yStr = doc.path("y").asText("");
				if (xStr.isBlank() || yStr.isBlank()) {
					continue;
				}

				Double longitude = Double.parseDouble(xStr);
				Double latitude = Double.parseDouble(yStr);
				return new GeoResult(address, latitude, longitude);
			}
			return null;
		} catch (Exception e) {
			log.warn("[카카오 좌표 검색 실패] query={}, reason={}", query, e.getMessage());
			return null;
		}
	}

	private String convertRegionToKorean(String region) {
		if (region == null) {
			return "";
		}
		if ("SEOUL".equals(region)) {
			return "서울";
		}
		if ("BUSAN".equals(region)) {
			return "부산";
		}
		if ("GYEONGGI".equals(region)) {
			return "경기";
		}
		return "";
	}
}
