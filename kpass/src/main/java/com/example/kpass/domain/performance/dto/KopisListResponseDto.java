package com.example.kpass.domain.performance.dto;

import java.util.List;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * [KOPIS 오픈 API 목록 조회 XML 응답 DTO]
 * 
 * OAuth2 vs KOPIS 오픈 API 방식 차이점:
 * 
 * OAuth2 인증 방식:
 *    - 사용자의 개인정보 접근 권한(Authorization)을 얻기 위한 프로토콜.
 *    - [Auth Code 발급] -> [Access Token 교환] -> [Header에 Bearer 토큰 얹어서 사용자 정보 요청] -> [OAuth2User.getAttributes() Map 구조로 처리]
 * 
 * KOPIS (공공데이터 오픈 API) 방식:
 *    - 특정 사용자의 인증이 필요 없는 누구나 접근 가능한 공개 데이터(Open Data).
 *    - 토큰 발급 단계가 없으며, 정부/기관에서 발급해 준 Service Key(API Key)를 HTTP GET URL 쿼리 파라미터(?service=키값)에 직접 태워 요청.
 *    - 수신받은 XML 텍스트 데이터를 Jackson XML Mapper가 DTO 객체 필드값으로 직접 역직렬화(Parsing)함.
 */

@Getter
@NoArgsConstructor
@JacksonXmlRootElement(localName = "dbs") // 1. 제일 큰 게 이렇게 들어오고
public class KopisListResponseDto {
	@JacksonXmlProperty(localName = "db") // 2. 그 다음이 db로 감싸져있고
	@JacksonXmlElementWrapper(useWrapping = false)
	private List<KopisItem> itemList;
	
	// 공연 목록 API /pblprfr -> mt20id(공연 고유id)전체 긁어옴
	// 공연 상세 API /pblprfr/{mt20id} -> 해당공연의 가격과 공연시설 ID가져옴
	//공연 시설 상세 API /prfplc/{fcltyId} - 상세주소, 위도, 경도
	
	// 3. 그 다음이 이제 진짜 변수들
	@Getter
	@NoArgsConstructor
	public static class KopisItem {
		@JacksonXmlProperty(localName = "mt20id")
		private String mt20id; // 공연 고유 ID == externalId 저장용
		
		@JacksonXmlProperty(localName = "prfnm")
		private String prfnm; // 공연 제목 === title
		
		@JacksonXmlProperty(localName = "genrenm")
		private String genrenm; // 장르명 == category
	}
	
	

}
