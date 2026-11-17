package com.example.kpass.domain.reservation.dto;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.ConstructorExpression;
import javax.annotation.processing.Generated;

/**
 * com.example.kpass.domain.reservation.dto.QReservationDtos_ReservationHistoryResponse is a Querydsl Projection type for ReservationHistoryResponse
 */
@Generated("com.querydsl.codegen.DefaultProjectionSerializer")
public class QReservationDtos_ReservationHistoryResponse extends ConstructorExpression<ReservationDtos.ReservationHistoryResponse> {

    private static final long serialVersionUID = -448082449L;

    public QReservationDtos_ReservationHistoryResponse(com.querydsl.core.types.Expression<Long> reservationId, com.querydsl.core.types.Expression<String> orderUid, com.querydsl.core.types.Expression<String> orderName, com.querydsl.core.types.Expression<Integer> ticketQuantity, com.querydsl.core.types.Expression<Integer> totalPrice, com.querydsl.core.types.Expression<String> reservationStatus, com.querydsl.core.types.Expression<java.time.LocalDateTime> createdAt, com.querydsl.core.types.Expression<String> paymentMethod, com.querydsl.core.types.Expression<java.time.LocalDateTime> approvedAt) {
        super(ReservationDtos.ReservationHistoryResponse.class, new Class<?>[]{long.class, String.class, String.class, int.class, int.class, String.class, java.time.LocalDateTime.class, String.class, java.time.LocalDateTime.class}, reservationId, orderUid, orderName, ticketQuantity, totalPrice, reservationStatus, createdAt, paymentMethod, approvedAt);
    }

}

