package com.shop.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.shop.entity.Order;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;

import java.util.List;

import static com.shop.entity.QItem.item;
import static com.shop.entity.QOrder.order;
import static com.shop.entity.QOrderItem.orderItem;

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
                .orderBy(order.regTime.desc())
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
                .join(order.orderItems, orderItem).fetchJoin()
                .join(orderItem.item, item).fetchJoin()
                .where(order.id.in(orderIds))
                .orderBy(order.regTime.desc())
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