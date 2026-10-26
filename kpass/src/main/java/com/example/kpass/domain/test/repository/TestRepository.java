package com.example.kpass.domain.test.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.kpass.domain.test.entity.TestEntity;

@Repository
public interface TestRepository extends JpaRepository<TestEntity, Long>{
	
	
	
	
}
