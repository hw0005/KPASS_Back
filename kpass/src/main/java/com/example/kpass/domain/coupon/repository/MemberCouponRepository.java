package com.example.kpass.domain.coupon.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.kpass.domain.coupon.entity.MemberCoupon;

public interface MemberCouponRepository extends JpaRepository<MemberCoupon, Long>, MemberCouponRepositoryCustom {

}
