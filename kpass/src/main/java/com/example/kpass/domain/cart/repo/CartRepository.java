package com.example.kpass.domain.cart.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.kpass.domain.cart.entity.Cart;

@Repository
public interface CartRepository extends JpaRepository<Cart, Long>, CartRepositoryCustom {

}
