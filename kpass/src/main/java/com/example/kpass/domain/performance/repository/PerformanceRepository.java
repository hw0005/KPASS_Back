package com.example.kpass.domain.performance.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.kpass.domain.performance.entity.Performance;

@Repository
public interface PerformanceRepository extends JpaRepository<Performance, Long>, PerformanceRepositoryCustom {
	
	// API 데이터 재수집 시 중복 삽입 방지
	boolean existsByExternalId(String externalId);
	
	// 단건 조회 수집
	Optional<Performance> findByExternalId(String externalId);

}
