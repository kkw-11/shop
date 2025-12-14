package com.shop.controller;

import com.shop.dto.OrderHistDto;
import com.shop.service.OrderService;
import io.swagger.v3.oas.annotations.Hidden;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@Hidden
@RequestMapping("/test")
@RequiredArgsConstructor
public class TestController {

    private final OrderService orderService;

    @GetMapping("/orders")
    public Map<String, Object> testOrders() {
        Pageable pageable = PageRequest.of(0, 4);
        Page<OrderHistDto> orders = orderService
                .getOrderList("admin@test.com", pageable);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("count", orders.getTotalElements());
        response.put("orders", orders.getContent());

        return response;  // JSON 응답
    }
}
