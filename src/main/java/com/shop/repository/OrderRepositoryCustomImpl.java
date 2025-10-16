package com.shop.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.shop.entity.Order;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;

import java.util.List;

import static com.shop.entity.QOrder.order;

@RequiredArgsConstructor
public class OrderRepositoryCustomImpl implements OrderRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<Order> findOrdersByEmail(String email, Pageable pageable) {
        // 1단계: ID만 페이징 (DB 페이징)
        List<Long> orderIds = queryFactory
                .select(order.id)
                .from(order)
                .where(order.member.email.eq(email))
                .orderBy(order.orderDate.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        if (orderIds.isEmpty()) {
            return List.of();
        }

        // 2단계: Fetch Join (IN 쿼리)
        return queryFactory
                .selectFrom(order)
                .distinct()
                .join(order.orderItems).fetchJoin()
                .join(order.orderItems.any().item).fetchJoin()
                .leftJoin(order.orderItems.any().item.itemImgs).fetchJoin()
                .where(order.id.in(orderIds))
                .orderBy(order.orderDate.desc())
                .fetch();
    }

    @Override
    public Long countOrdersByEmail(String email) {
        return queryFactory
                .select(order.count())
                .from(order)
                .where(order.member.email.eq(email))
                .fetchOne();
    }
}