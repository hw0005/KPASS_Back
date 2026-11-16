package com.example.kpass.domain.reservation.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QReservation is a Querydsl query type for Reservation
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QReservation extends EntityPathBase<Reservation> {

    private static final long serialVersionUID = 107221514L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QReservation reservation = new QReservation("reservation");

    public final DateTimePath<java.time.LocalDateTime> createdAt = createDateTime("createdAt", java.time.LocalDateTime.class);

    public final DateTimePath<java.time.LocalDateTime> expiredAt = createDateTime("expiredAt", java.time.LocalDateTime.class);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final com.example.kpass.domain.member.entity.QMember member;

    public final com.example.kpass.domain.coupon.entity.QMemberCoupon memberCoupon;

    public final StringPath orderName = createString("orderName");

    public final StringPath orderUid = createString("orderUid");

    public final com.example.kpass.domain.performance.entity.QPerformance performance;

    public final ListPath<ReservationItem, QReservationItem> reservationItems = this.<ReservationItem, QReservationItem>createList("reservationItems", ReservationItem.class, QReservationItem.class, PathInits.DIRECT2);

    public final EnumPath<ReservationStatus> status = createEnum("status", ReservationStatus.class);

    public final NumberPath<Integer> ticketQuantity = createNumber("ticketQuantity", Integer.class);

    public final NumberPath<Integer> totalPrice = createNumber("totalPrice", Integer.class);

    public QReservation(String variable) {
        this(Reservation.class, forVariable(variable), INITS);
    }

    public QReservation(Path<? extends Reservation> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QReservation(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QReservation(PathMetadata metadata, PathInits inits) {
        this(Reservation.class, metadata, inits);
    }

    public QReservation(Class<? extends Reservation> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.member = inits.isInitialized("member") ? new com.example.kpass.domain.member.entity.QMember(forProperty("member")) : null;
        this.memberCoupon = inits.isInitialized("memberCoupon") ? new com.example.kpass.domain.coupon.entity.QMemberCoupon(forProperty("memberCoupon"), inits.get("memberCoupon")) : null;
        this.performance = inits.isInitialized("performance") ? new com.example.kpass.domain.performance.entity.QPerformance(forProperty("performance")) : null;
    }

}

