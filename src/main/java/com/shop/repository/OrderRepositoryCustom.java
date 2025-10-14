package com.shop.repository;

import com.shop.entity.Order;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface OrderRepositoryCustom {
    List<Order> findOrdersByEmail(String email, Pageable pageable);

    Long countOrdersByEmail(String email);
}
