package com.example.kpass.domain.performance.dto;

import java.util.List;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@JacksonXmlRootElement(localName = "dbs")
public class KopisDetailResponseDto {
	
	@JacksonXmlProperty(localName="db")
	@JacksonXmlElementWrapper(useWrapping = false)
	private List<KopisDetailItem> detailList;
	
	@Getter
	@NoArgsConstructor
	public static class KopisDetailItem {
		
//		<mt20id>PF178134</mt20id>
//        <prfnm>반짝반짝 인어공주</prfnm>
//        <genrenm>뮤지컬</genrenm>
//        <area>서울특별시</area>
//        <fcltynm>달밤엔씨어터</fcltynm> 공연장명
//        <poster>http://www.kopis.or.kr/upload/pfmPoster/PF_PF178134_210809_125033.PNG</poster>
//        <prfpdfrom>2021.08.21</prfpdfrom>
//        <prfpdto>2024.09.29</prfpdto>
		
		
		@JacksonXmlProperty(localName = "mt20id")
		private String mt20id; // 고유 id
		
		@JacksonXmlProperty(localName = "prfnm")
		private String prfnm; // 공연명
		
		@JacksonXmlProperty(localName = "genrenm")
		private String genrenm; // 장르명
		
		@JacksonXmlProperty(localName = "area")
		private String area; // 지역명
		
		@JacksonXmlProperty(localName = "fcltynm")
		private String fcltynm; // 공연장명
		
		@JacksonXmlProperty(localName = "poster")
		private String poster; // 포스터 URL
		
		@JacksonXmlProperty(localName = "pcseguidance")
		private String pcseguidance; // 티켓 가격 문자열("R석 50,000원")
		
		@JacksonXmlProperty(localName = "prfpdfrom")
		private String prfpdfrom; // 시작일(yyyy.MM.dd)
		
		@JacksonXmlProperty(localName = "prfpdto")
		private String prfpdto; // 종료일
	}

}
