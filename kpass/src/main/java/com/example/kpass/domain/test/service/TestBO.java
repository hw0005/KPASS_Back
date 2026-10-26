package com.example.kpass.domain.test.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.kpass.domain.test.entity.QTestEntity;
import com.example.kpass.domain.test.entity.TestEntity;
import com.querydsl.jpa.impl.JPAQueryFactory;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor 
@Transactional(readOnly = true)
public class TestBO {

	private final EntityManager em;
	private final JPAQueryFactory jpaQueryFactory;

	@Transactional
	public String executeTest(String message) {
		TestEntity testEntity = new TestEntity();
		testEntity.setNickname(message != null ? message : "테스트유저");
		testEntity.setRole("USER");
		testEntity.setProvider("LOCAL");
		em.persist(testEntity);

		// 조회
		QTestEntity qTest = QTestEntity.testEntity;
		TestEntity found = jpaQueryFactory.selectFrom(qTest).where(qTest.id.eq(testEntity.getId())).fetchOne();

		return found != null ? "회원 닉네임: " + found.getNickname() + " (DB ID: " + found.getId() + ")" : "조회 실패";
	}
}