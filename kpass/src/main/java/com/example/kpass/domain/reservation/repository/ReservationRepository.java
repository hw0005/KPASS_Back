package com.example.kpass.domain.reservation.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.kpass.domain.reservation.entity.Reservation;

public interface ReservationRepository extends JpaRepository<Reservation, Long>, ReservationRepositoryCustom{

}
