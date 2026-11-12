package com.example.kpass.domain.cart.repo;

import java.util.List;
import java.util.Optional;

import com.example.kpass.domain.cart.dto.CartItemResponseDto;
import com.example.kpass.domain.cart.dto.QCartItemResponseDto;
import com.example.kpass.domain.cart.entity.Cart;
import com.example.kpass.domain.cart.entity.QCart;
import com.example.kpass.domain.performance.entity.QPerformance;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class CartRepositoryImpl implements CartRepositoryCustom{
	
	private final JPAQueryFactory jpaQueryFactory;
	
	private final QCart c = QCart.cart;
	private final QPerformance p = QPerformance.performance;
	
	// 내 장바구니 조회
	@Override
	public List<CartItemResponseDto> findAllByMemberIdWithPerformance(Long memberId) {
		return jpaQueryFactory
				.select(new QCartItemResponseDto(
						c.id,
						p.id,
						p.externalId,
						p.title,
						p.category,
						p.region,
						p.venue,
						p.posterUrl,
						p.price,
						p.startDate,
						p.endDate,
						c.quantity
				))
				.from(c)
				.join(c.performance, p)
				.where(c.member.id.eq(memberId))
				.orderBy(c.createdAt.desc())
				.fetch();
	}

	@Override
	public Optional<Cart> findByMemberIdAndPerformanceId(Long memberId, Long performanceId) {
		Cart result = jpaQueryFactory
				.selectFrom(c)
				.where(
						c.member.id.eq(memberId),
						c.performance.id.eq(performanceId)
				)
				.fetchOne();
		
		return Optional.ofNullable(result);
	}

	@Override
	public void deleteByIdAndMemberId(Long cartId, Long memberId) {
		jpaQueryFactory
			.delete(c)
			.where(
					c.id.eq(cartId),
					c.member.id.eq(memberId)
			)
			.execute();
	}

	@Override
	public void deleteAllByMemberIdAndIdIn(Long memberId, List<Long> cartIds) {
		jpaQueryFactory
		.delete(c)
		.where(
				c.member.id.eq(memberId),
				c.id.in(cartIds)
				)
		.execute();
	}
}
