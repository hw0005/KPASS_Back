package com.example.kpass.domain.performance.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QPerformanceSeatPrice is a Querydsl query type for PerformanceSeatPrice
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QPerformanceSeatPrice extends EntityPathBase<PerformanceSeatPrice> {

    private static final long serialVersionUID = 134322650L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QPerformanceSeatPrice performanceSeatPrice = new QPerformanceSeatPrice("performanceSeatPrice");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final QPerformance performance;

    public final NumberPath<Integer> price = createNumber("price", Integer.class);

    public final StringPath seatType = createString("seatType");

    public QPerformanceSeatPrice(String variable) {
        this(PerformanceSeatPrice.class, forVariable(variable), INITS);
    }

    public QPerformanceSeatPrice(Path<? extends PerformanceSeatPrice> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QPerformanceSeatPrice(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QPerformanceSeatPrice(PathMetadata metadata, PathInits inits) {
        this(PerformanceSeatPrice.class, metadata, inits);
    }

    public QPerformanceSeatPrice(Class<? extends PerformanceSeatPrice> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.performance = inits.isInitialized("performance") ? new QPerformance(forProperty("performance")) : null;
    }

}

