package com.example.kpass.domain.reservation.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QReservationItem is a Querydsl query type for ReservationItem
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QReservationItem extends EntityPathBase<ReservationItem> {

    private static final long serialVersionUID = 851110973L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QReservationItem reservationItem = new QReservationItem("reservationItem");

    public final NumberPath<Long> cartId = createNumber("cartId", Long.class);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final com.example.kpass.domain.performance.entity.QPerformance performance;

    public final NumberPath<Integer> quantity = createNumber("quantity", Integer.class);

    public final QReservation reservation;

    public final com.example.kpass.domain.performance.entity.QPerformanceSeatPrice seatPrice;

    public final StringPath seatType = createString("seatType");

    public final NumberPath<Integer> unitPrice = createNumber("unitPrice", Integer.class);

    public QReservationItem(String variable) {
        this(ReservationItem.class, forVariable(variable), INITS);
    }

    public QReservationItem(Path<? extends ReservationItem> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QReservationItem(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QReservationItem(PathMetadata metadata, PathInits inits) {
        this(ReservationItem.class, metadata, inits);
    }

    public QReservationItem(Class<? extends ReservationItem> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.performance = inits.isInitialized("performance") ? new com.example.kpass.domain.performance.entity.QPerformance(forProperty("performance")) : null;
        this.reservation = inits.isInitialized("reservation") ? new QReservation(forProperty("reservation"), inits.get("reservation")) : null;
        this.seatPrice = inits.isInitialized("seatPrice") ? new com.example.kpass.domain.performance.entity.QPerformanceSeatPrice(forProperty("seatPrice"), inits.get("seatPrice")) : null;
    }

}

