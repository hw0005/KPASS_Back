package com.example.kpass.domain.performance.dto.venue;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.ConstructorExpression;
import javax.annotation.processing.Generated;

/**
 * com.example.kpass.domain.performance.dto.venue.QVenueTargetDto is a Querydsl Projection type for VenueTargetDto
 */
@Generated("com.querydsl.codegen.DefaultProjectionSerializer")
public class QVenueTargetDto extends ConstructorExpression<VenueTargetDto> {

    private static final long serialVersionUID = -1698774958L;

    public QVenueTargetDto(com.querydsl.core.types.Expression<String> venue, com.querydsl.core.types.Expression<String> region) {
        super(VenueTargetDto.class, new Class<?>[]{String.class, String.class}, venue, region);
    }

}

