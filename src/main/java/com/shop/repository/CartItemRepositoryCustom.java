package com.shop.repository;

import com.shop.dto.CartDetailDto;

import java.util.List;

public interface CartItemRepositoryCustom {
    List<CartDetailDto> findCartDetailDtoList(Long cartId);
}
