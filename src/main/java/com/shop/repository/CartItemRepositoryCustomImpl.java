package com.shop.repository;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.shop.dto.CartDetailDto;
import lombok.RequiredArgsConstructor;

import java.util.List;

import static com.shop.entity.QCartItem.cartItem;
import static com.shop.entity.QItem.item;
import static com.shop.entity.QItemImg.itemImg;

@RequiredArgsConstructor
public class CartItemRepositoryCustomImpl implements CartItemRepositoryCustom {
    private final JPAQueryFactory queryFactory;

    @Override
    public List<CartDetailDto> findCartDetailDtoList(Long cartId) {
        return queryFactory
                .select(Projections.constructor(CartDetailDto.class,
                        cartItem.id,
                        item.itemNm,
                        item.price,
                        cartItem.quantity,
                        itemImg.imgUrl))
                .from(cartItem)
                .join(cartItem.item, item)
                .join(itemImg).on(itemImg.item.id.eq(item.id))
                .where(cartItem.cart.id.eq(cartId)
                        .and(itemImg.repImgYn.eq("Y")))
                .orderBy(cartItem.regTime.desc())
                .fetch();
    }
}
