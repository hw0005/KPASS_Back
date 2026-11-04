package com.example.kpass.domain.performance.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QPerformance is a Querydsl query type for Performance
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QPerformance extends EntityPathBase<Performance> {

    private static final long serialVersionUID = 1867534218L;

    public static final QPerformance performance = new QPerformance("performance");

    public final StringPath address = createString("address");

    public final StringPath category = createString("category");

    public final DateTimePath<java.time.LocalDateTime> createdAt = createDateTime("createdAt", java.time.LocalDateTime.class);

    public final DatePath<java.time.LocalDate> endDate = createDate("endDate", java.time.LocalDate.class);

    public final StringPath externalId = createString("externalId");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final NumberPath<Double> latitude = createNumber("latitude", Double.class);

    public final NumberPath<Integer> likeCount = createNumber("likeCount", Integer.class);

    public final NumberPath<Double> longitude = createNumber("longitude", Double.class);

    public final StringPath posterUrl = createString("posterUrl");

    public final NumberPath<Integer> price = createNumber("price", Integer.class);

    public final StringPath region = createString("region");

    public final ListPath<PerformanceSeatPrice, QPerformanceSeatPrice> seatPrices = this.<PerformanceSeatPrice, QPerformanceSeatPrice>createList("seatPrices", PerformanceSeatPrice.class, QPerformanceSeatPrice.class, PathInits.DIRECT2);

    public final DatePath<java.time.LocalDate> startDate = createDate("startDate", java.time.LocalDate.class);

    public final NumberPath<Integer> stockQuantity = createNumber("stockQuantity", Integer.class);

    public final StringPath title = createString("title");

    public final DateTimePath<java.time.LocalDateTime> updatedAt = createDateTime("updatedAt", java.time.LocalDateTime.class);

    public final StringPath venue = createString("venue");

    public final NumberPath<Integer> viewCount = createNumber("viewCount", Integer.class);

    public QPerformance(String variable) {
        super(Performance.class, forVariable(variable));
    }

    public QPerformance(Path<? extends Performance> path) {
        super(path.getType(), path.getMetadata());
    }

    public QPerformance(PathMetadata metadata) {
        super(Performance.class, metadata);
    }

}

