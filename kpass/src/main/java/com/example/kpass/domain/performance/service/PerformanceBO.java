package com.example.kpass.domain.performance.service;

import java.net.URI;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import com.example.kpass.domain.performance.dto.KopisDetailResponseDto;
import com.example.kpass.domain.performance.dto.KopisListResponseDto;
import com.example.kpass.domain.performance.entity.Performance;
import com.example.kpass.domain.performance.entity.PerformanceSeatPrice;
import com.example.kpass.domain.performance.repository.PerformanceRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class PerformanceBO {

	private final PerformanceRepository performanceRepository;
	private final RestTemplate restTemplate = new RestTemplate();

	@Value("${kopis.api.key}")
	private String serviceKey;

	private static final String BASE_URL = "http://www.kopis.or.kr/openApi/restful/pblprfr";
	private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy.MM.dd");
	private static final DateTimeFormatter KOPIS_PARAM_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");

	// [좌석명 + 가격] 추출 정규식 (예: "R석 15,000원", "일반석 10000원")
	private static final Pattern SEAT_PRICE_PATTERN = Pattern
			.compile("([가-힣A-Za-z0-9\\s]+?)\\s*(\\d{1,3}(?:,\\d{3})*|\\d+)\\s*원");

	// KOPIS WAF 방화벽 통과용 완벽한 HTTP Header 설정
	private HttpEntity<Void> createHeaderEntity() {
		HttpHeaders headers = new HttpHeaders();
		headers.add("User-Agent",
				"Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36");
		headers.add("Accept", MediaType.APPLICATION_XML_VALUE + ", " + MediaType.TEXT_XML_VALUE + ", */*");
		return new HttpEntity<>(headers);
	}

	public void fetchAndSaveKopisData(String stdate, String eddate) {
		int cpage = 1;
		int rows = 30;

		log.info("=== [KOPIS 수집 시작] 기간: {} ~ {} ===", stdate, eddate);

		while (true) {
			String listUrlStr = String.format("%s?service=%s&stdate=%s&eddate=%s&cpage=%d&rows=%d", BASE_URL,
					serviceKey, stdate, eddate, cpage, rows);

			KopisListResponseDto listResponse = null;

			// 목록 API 호출 시 WAF 차단 대비 최대 3회 재시도
			for (int attempt = 1; attempt <= 3; attempt++) {
				try {
					ResponseEntity<KopisListResponseDto> response = restTemplate.exchange(URI.create(listUrlStr),
							HttpMethod.GET, createHeaderEntity(), KopisListResponseDto.class);
					listResponse = response.getBody();
					break; // 성공 시 루프 탈출
				} catch (Exception e) {
					if (attempt < 3) {
						log.warn("[목록 WAF 차단 감지] {}페이지 {}회차 대기 중...", cpage, attempt);
						try {
							Thread.sleep(1500 * attempt); // 1.5초 대기 후 재시도
						} catch (InterruptedException ie) {
							Thread.currentThread().interrupt();
						}
					} else {
						log.error("[목록 수집 실패] cpage: {}", cpage, e);
					}
				}
			}

			// 더 이상 가져올 목록이 없거나 3회 연속 실패 시 루프 종료
			if (listResponse == null || listResponse.getItemList() == null || listResponse.getItemList().isEmpty()) {
				log.info("=== [KOPIS 수집 완료] 총 {} 페이지 처리 완료 ===", cpage - 1);
				break;
			}

			log.info("[KOPIS 수집] {} 페이지 진행 중 (목록 수: {}건)", cpage, listResponse.getItemList().size());

			for (KopisListResponseDto.KopisItem item : listResponse.getItemList()) {
				String externalId = "KOPIS_" + item.getMt20id();

				if (performanceRepository.existsByExternalId(externalId)) {
					log.info("이미 존재하는 공연 Skip: {}", externalId);
					continue;
				}

				fetchDetailAndSave(item.getMt20id(), externalId);

				try {
					Thread.sleep(300);
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
				}
			}

			cpage++;
		}
	}

	/**
	 * 5년 치 전수 수집용 편의 메서드
	 */
	public void fetchRecent5YearsData() {
		LocalDate now = LocalDate.now();
		String stdate = now.minusYears(5).format(KOPIS_PARAM_FORMATTER);
		String eddate = now.plusMonths(6).format(KOPIS_PARAM_FORMATTER);
		fetchAndSaveKopisData(stdate, eddate);
	}

	/**
	 * 단건 상세 조회 및 DB 저장 (리턴 타입을 void로 지정하여 반환 에러 해결)
	 */
	@Transactional
	public void fetchDetailAndSave(String mt20id, String externalId) {
		String detailUrlStr = String.format("%s/%s?service=%s", BASE_URL, mt20id, serviceKey);

		//  상세 API 호출 시 WAF 차단 대비 최대 3회 재시도
		for (int attempt = 1; attempt <= 3; attempt++) {
			try {
				ResponseEntity<KopisDetailResponseDto> response = restTemplate.exchange(URI.create(detailUrlStr),
						HttpMethod.GET, createHeaderEntity(), KopisDetailResponseDto.class);

				KopisDetailResponseDto detailResponse = response.getBody();

				if (detailResponse == null || detailResponse.getDetailList() == null
						|| detailResponse.getDetailList().isEmpty()) {
					return;
				}

				KopisDetailResponseDto.KopisDetailItem detail = detailResponse.getDetailList().get(0);

				if (performanceRepository.existsByExternalId(externalId)) {
					log.info("상세 조회 중 중복 발견 Skip: {}", externalId);
					return;
				}

				List<PerformanceSeatPrice> seatPrices = parseSeatPrices(detail.getPcseguidance());

				int minPrice = 0;
				if (!seatPrices.isEmpty()) {
					minPrice = seatPrices.get(0).getPrice();
					for (int i = 1; i < seatPrices.size(); i++) {
						if (seatPrices.get(i).getPrice() < minPrice) {
							minPrice = seatPrices.get(i).getPrice();
						}
					}
				}

				LocalDate startDate = LocalDate.parse(detail.getPrfpdfrom().trim(), DATE_FORMATTER);
				LocalDate endDate = LocalDate.parse(detail.getPrfpdto().trim(), DATE_FORMATTER);

				Performance performance = Performance.createPerformance(externalId, detail.getPrfnm(),
						convertCategory(detail.getGenrenm()), convertRegion(detail.getArea()), detail.getFcltynm(),
						detail.getPoster(), minPrice, seatPrices, startDate, endDate);

				try {
					performanceRepository.save(performance);
					log.info("저장 성공 [ID: {}, 제목: {}, 최저가: {}원]", externalId, detail.getPrfnm(), minPrice);
				} catch (DataIntegrityViolationException e) {
					log.warn("저장 중 중복 예외 무시 [ID: {}]", externalId);
				}

				return; // 성공 시 종료

			} catch (Exception e) {
				if (attempt < 3) {
					try {
						Thread.sleep(1000 * attempt); // 1초 대기 후 재시도
					} catch (InterruptedException ie) {
						Thread.currentThread().interrupt();
						return;
					}
				} else {
					log.error("KOPIS 상세 데이터 3회 재시도 실패 [ID: {}]", mt20id, e);
				}
			}
		}
	}

	/**
	 * pcseguidance 텍스트 파싱 ➔ List<PerformanceSeatPrice> 생성
	 */
	private List<PerformanceSeatPrice> parseSeatPrices(String priceGuidance) {
		List<PerformanceSeatPrice> result = new ArrayList<>();

		if (priceGuidance == null || priceGuidance.isBlank() || priceGuidance.contains("무료")) {
			return result;
		}

		Matcher matcher = SEAT_PRICE_PATTERN.matcher(priceGuidance);

		while (matcher.find()) {
			String rawSeatType = matcher.group(1).replaceAll("^[^가-힣A-Za-z0-9]+", "").trim();
			String priceStr = matcher.group(2).replaceAll(",", "");

			if (rawSeatType.isBlank()) {
				rawSeatType = "일반석";
			}

			try {
				int price = Integer.parseInt(priceStr);

				// 좌석 가격 엔티티 생성
				result.add(PerformanceSeatPrice.createPrice(rawSeatType, price));

			} catch (NumberFormatException ignored) {
			}
		}

		return result;
	}

	private String convertCategory(String genre) {
		if (genre == null) {
			return "CONCERT";
		}
		if (genre.contains("뮤지컬"))
			return "MUSICAL";
		if (genre.contains("연극"))
			return "THEATER";
		if (genre.contains("클래식"))
			return "CLASSIC";
		if (genre.contains("국악"))
			return "KOREAN_MUSIC";
		return "CONCERT";
	}

	private String convertRegion(String area) {
		if (area == null) {
			return "SEOUL";
		}
		if (area.contains("서울"))
			return "SEOUL";
		if (area.contains("부산"))
			return "BUSAN";
		if (area.contains("경기"))
			return "GYEONGGI";
		return "OTHER";
	}

}
