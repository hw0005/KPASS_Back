package com.example.kpass.domain.performance.service;

import java.net.URI;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
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

import com.example.kpass.domain.performance.dto.kopis.KopisDetailResponseDto;
import com.example.kpass.domain.performance.dto.kopis.KopisListResponseDto;
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
	/**
	 * 역할: KOPIS 방화벽(WAF) 차단을 피하기 위한 HTTP 요청 헤더 생성기
		핵심 로직
		API 서버가 자동화 스크립트(봇)로 인식하지 않도록 Chrome 브라우저의 User-Agent 정보를 탑재
		KOPIS 서버가 선호하는 XML 응답 형식(APPLICATION_XML_VALUE)을 Accept 헤더에 명시
		
	 * @return
	 */
	private HttpEntity<Void> createHeaderEntity() {
		HttpHeaders headers = new HttpHeaders();
		headers.add("User-Agent",
				"Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36");
		headers.add("Accept", MediaType.APPLICATION_XML_VALUE + ", " + MediaType.TEXT_XML_VALUE + ", */*");
		return new HttpEntity<>(headers);
	}

	/**
	 * 
		역할: 시작일~종료일 기간 동안의 공연 목록 API를 페이징(cpage)하면서 끝까지 수집하는 메인 루프
		
		핵심 로직:
		목록 API WAF 자동 재시도: 400 에러로 목록 요청이 막히면 1.5초 쿨다운 후 최대 3회까지 재시도
		종료 조건: 목록 API 응답이 없거나 데이터 배열이 비어있으면 전체 수집을 정상 완료 처리
		Fast-Skip 중복 검사: existsByExternalId 사전 조회를 통해 이미 DB에 있는 공연은 상세 API 호출 없이 넘기기
		속도 제어: 상세 수집 호출 직후 Thread.sleep(300)을 부여해 KOPIS WAF 안전선(초당 ~3회)을 유지
		
	 * @param stdate
	 * @param eddate
	 */
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
							Thread.sleep(3000 * attempt); // 3초 대기 후 재시도
						} catch (InterruptedException ie) {
							Thread.currentThread().interrupt();
						}
					} else {
						log.error("[목록 수집 실패] WAF차단으로 인한 수집 실패 cpage: {}", cpage, e);
					}
				}
			}

			// 3회 다 실패했을 때는 정상 종료(완료) 로그를 안 찍고 break 하도록 구분
			if (listResponse == null) {
			    log.error("=== [KOPIS 수집 일시 중단] {} 페이지에서 WAF 차단으로 멈춤. 서버 재시작 필요 ===", cpage);
			    break;
			}

			// 실제 목록 아이템이 비어있을 때만 수집 완료 처리
			if (listResponse.getItemList() == null || listResponse.getItemList().isEmpty()) {
			    log.info("=== [KOPIS 수집 완료] 총 {} 페이지 처리 완료 ===", cpage - 1);
			    break;
			}

			log.info("[KOPIS 수집] {} 페이지 진행 중 (목록 수: {}건)", cpage, listResponse.getItemList().size());

			int skipCount = 0;
			
			for (KopisListResponseDto.KopisItem item : listResponse.getItemList()) {
				String externalId = "KOPIS_" + item.getMt20id();

				// 1. 이미 존재하면 건당 로그 출력 없이 카운트만 올리고 즉시 통과
				if (performanceRepository.existsByExternalId(externalId)) {
//					log.info("이미 존재하는 공연 Skip: {}", externalId); // 로그찍혀서 빠르게 못 넘어가
					skipCount++;
					continue;
				}

				//2. 신규 데이터 상세API호출
				fetchDetailAndSave(item.getMt20id(), externalId);

				try {
					Thread.sleep(500);
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
				}
			}
			
			// 3. 페이지가 끝날 때 딱 1줄로 요약 로그만 출력
			if (skipCount == listResponse.getItemList().size()) {
			    log.info("⏩ [Fast-Skip] {} 페이지 30건 전체 이미 존재 (스킵 완료)", cpage);
			} else {
			    log.info("✅ [KOPIS 적재] {} 페이지 처리 완료 (스킵: {}건, 신규 저장: {}건)", 
			            cpage, skipCount, listResponse.getItemList().size() - skipCount);
			}
			
			try {
			    Thread.sleep(200); // 목록 API 연사로 인한 WAF 차단 방지용 (0.2초 쉼)
			} catch (InterruptedException e) {
			    Thread.currentThread().interrupt();
			}

			cpage++;
		}
	}

	/**
	 * 
		역할: 5년 치 데이터 전수 수집(Cold Start)용 편의 메서드 (Initializer가 호출)
	
		핵심 로직:
		LocalDate.now().minusYears(5)로 5년 전 오늘부터 plusMonths(6)로 6개월 뒤까지의 날짜 문자열(YYYYMMDD)을 자동 계산합니다.
		동적으로 계산된 날짜 범위를 담아 fetchAndSaveKopisData()를 실행합니다.
	
	 * 5년 치 전수 수집용 편의 메서드
	 */
	public void fetchRecent5YearsData() {
		LocalDate now = LocalDate.now();
		String stdate = now.minusYears(5).format(KOPIS_PARAM_FORMATTER);
		String eddate = now.plusMonths(6).format(KOPIS_PARAM_FORMATTER);
		fetchAndSaveKopisData(stdate, eddate);
	}

	/**
	 * 
		역할: 공연 1건에 대해 단건 상세 API를 호출하고, 최저가 계산 및 DB 엔티티 저장을 수행
	
		핵심 로직:
		상세 API WAF 자동 재시도: 상세 요청 시 차단되면 1초 쿨다운을 거치며 최대 3회 재시도.
		최저가 계산: parseSeatPrices()로 추출한 좌석 목록을 순수 for문으로 순회하여 최저 가격(minPrice)을 추출.
		트랜잭션 저장: 날짜 파싱 및 카테고리/지역 변환 후 Performance.createPerformance()로 엔티티를 만들어 DB에 저장.
		예외 안전망: DB 동시 저장으로 인한 중복 키 발생 시 DataIntegrityViolationException을 catch하여 서버가 뻗지 않고 무시하도록 처리.
		
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
						Thread.sleep(2000 * attempt); // 2, 4초 대기 후 재시도
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
