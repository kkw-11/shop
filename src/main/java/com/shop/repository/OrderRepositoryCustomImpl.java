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
        return queryFactory
                .selectFrom(order)
                .where(order.member.email.eq(email))
                .orderBy(order.orderDate.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
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