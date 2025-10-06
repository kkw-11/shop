package com.shop.service;

import com.shop.constant.ItemSellStatus;
import com.shop.dto.CartItemDto;
import com.shop.entity.*;
import com.shop.repository.*;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
@ActiveProfiles("test")
class CartServiceTest {
    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private CartService cartService;

    @Autowired
    private CartItemRepository cartItemRepository;

    @PersistenceContext
    private EntityManager em;
    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    public Item saveItem(){
        Item item = new Item();
        item.setItemNm("테스트 상품");
        item.setPrice(1000);
        item.setItemDetail("테스트 상품 상세 설명");
        item.setItemSellStatus(ItemSellStatus.SELL);
        item.setStockNumber(100);
        return itemRepository.save(item);
    }

    public List<Item> saveItems(){
        List<Item> items = new ArrayList<>();
        for(int i = 0; i < 10; i++){
            Item item = saveItem();
            item.setItemNm(item.getItemNm() + i);
        }
        return items;
    }

    public Member saveMember(){
        Member member = new Member();
        member.setEmail("test@test.com");
        return memberRepository.save(member);
    }

    @Test
    @DisplayName("장바구니 담기 테스트")
    public void addCartTest(){
        Item item = saveItem();
        Member member = saveMember();

        CartItemDto cartItemDto = new CartItemDto();
        cartItemDto.setCount(5);
        cartItemDto.setItemId(item.getId());

        Long cartItemId = cartService.addCart(cartItemDto, member.getEmail());

        CartItem cartItem = cartItemRepository.findById(cartItemId).orElseThrow(RuntimeException::new);

        assertEquals(item.getId(), cartItem.getItem().getId());
        assertEquals(cartItemDto.getCount(), cartItem.getCount());

    }

    @Test
    @DisplayName("장바구니 수량 업데이트 테스트")
    public void updateCartItemCountTest(){
        Item item = saveItem();
        Member member = saveMember();

        CartItemDto cartItemDto = new CartItemDto();
        cartItemDto.setCount(4);
        cartItemDto.setItemId(item.getId());

        Long cartItemId = cartService.addCart(cartItemDto, member.getEmail());

        CartItem cartItem = cartItemRepository.findById(cartItemId).orElseThrow(RuntimeException::new);

        int count = cartItem.getCount() + 1;
        cartService.updateCartItemCount(cartItemId, count);

        assertEquals(count, cartItem.getCount());

    }

    @Test
    @DisplayName("장바구니 상품 주문 테스트")
    public void orderCartItemTest(){
        List<Item> items = saveItems();
        Member member = saveMember();

        List<CartItemDto> cartItemDtoList = items.stream().map(item -> {
            CartItemDto cartItemDto = new CartItemDto();
            cartItemDto.setItemId(item.getId());
            cartItemDto.setCount(1);
            return cartItemDto;
        }).collect(Collectors.toList());


        //장바구니 담기 ,  장바구니 상품 조회
        List<Long> cartItemIds = cartItemDtoList.stream().map(cartItem -> {
            Long itemId = cartService.addCart(cartItem, member.getEmail());
            return itemId;
        }).collect(Collectors.toList());


        //장바구니 상품 주문
        Long orderId = cartService.orderCartItem(cartItemIds, member.getEmail());
        Order order = orderRepository.findById(orderId).orElseThrow(RuntimeException::new);
        List<CartItem> cartItems = cartItemRepository.findAllById((cartItemIds));

        List<Long> cartItemProductIds = cartItems.stream()
                .map(c -> c.getItem().getId())
                .sorted()
                .toList();

        List<Long> orderItemProductIds = order.getOrderItems().stream()
                .map(o -> o.getItem().getId())
                .sorted()
                .toList();


        //장바구니 상품 장바구니에서 제거테스트, 주문 됐는지 테스트
        assertEquals(cartItemIds.size(), order.getOrderItems().size());
        assertEquals(cartItemProductIds, orderItemProductIds);
        assertEquals(member.getEmail(), order.getMember().getEmail());
        assertEquals(0, cartItems.size());
    }
}