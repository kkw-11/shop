package com.shop.service;

import com.shop.constant.ItemSellStatus;
import com.shop.constant.OrderStatus;
import com.shop.dto.OrderDto;
import com.shop.dto.OrderHistDto;
import com.shop.entity.*;
import com.shop.repository.ItemImgRepository;
import com.shop.repository.ItemRepository;
import com.shop.repository.MemberRepository;
import com.shop.repository.OrderRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;


import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
@ActiveProfiles("test")
class OrderServiceTest {
    @Autowired
    private OrderService orderService;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private ItemImgRepository itemImgRepository;

    @Autowired
    private EntityManager em;

    public Item saveItem(){
        Item item = new Item();
        item.setItemNm("테스트 상품");
        item.setPrice(10000);
        item.setItemDetail("테스트 상품 상세 설명");
        item.setItemSellStatus(ItemSellStatus.SELL);
        item.setStockNumber(100);
        itemRepository.save(item);
        return item;
    }

    public Member saveMember(){
        Member member = new Member();
        member.setEmail("test@test.com");
        memberRepository.save(member);
        return member;
    }

    /**
     * 테스트용 상품 이미지 생성
     */
    private ItemImg createItemImg(Item item, String imgUrl) {
        ItemImg itemImg = new ItemImg();
        itemImg.setItem(item);
        itemImg.setImgName("test.jpg");
        itemImg.setOriImgName("test.jpg");
        itemImg.setImgUrl(imgUrl);
        itemImg.setRepImgYn("Y");
        return itemImgRepository.save(itemImg);
    }

    /**
     * 테스트용 주문 생성
     */
    private Order createOrder(Member member, Item item, int count) {
        Order order = new Order();
        order.setMember(member);
        order.setOrderDate(LocalDateTime.now());
        order.setOrderStatus(OrderStatus.ORDER);

        OrderItem orderItem = new OrderItem();
        orderItem.setItem(item);
        orderItem.setQuantity(count);
        orderItem.setOrderPrice(item.getPrice());
        orderItem.setOrder(order);

        order.getOrderItems().add(orderItem);

        return orderRepository.save(order);
    }

    @Test
    @DisplayName("주문 요청 테스트")
    public void order(){
        Item item = saveItem();
        Member member = saveMember();

        OrderDto orderDto = new OrderDto();
        orderDto.setItemId(item.getId());
        orderDto.setCount(10);

        Long orderId = orderService.order(orderDto, member.getEmail());

        Order order = orderRepository.findById(orderId).orElseThrow(EntityNotFoundException::new);

        int totalPrice = orderDto.getCount() * item.getPrice();

        assertEquals(totalPrice, order.getTotalPrice());
    }

    @Test
    @DisplayName("주문 취소 테스트")
    public void cancelOrder(){
        Item item = saveItem();
        Member member = saveMember();

        OrderDto orderDto = new OrderDto();
        orderDto.setItemId(item.getId());
        orderDto.setCount(10);
        Long orderId = orderService.order(orderDto, member.getEmail());

        Order order = orderRepository.findById(orderId).orElseThrow(EntityNotFoundException::new);
        orderService.cancelOrder(orderId);

        assertEquals(OrderStatus.CANCEL, order.getOrderStatus());
        assertEquals(100, item.getStockNumber());
    }

    @Test
    @DisplayName("주문 목록 조회 테스트")
    void getOrderListTest() {
        // given
        Member member = saveMember();

        Item item1 = saveItem();
        Item item2 = saveItem();
        Item item3 = saveItem();

        createItemImg(item1, "/images/item1.jpg");
        createItemImg(item2, "/images/item2.jpg");
        createItemImg(item3, "/images/item3.jpg");

        createOrder(member, item1, 2);
        createOrder(member, item2, 1);
        createOrder(member, item3, 3);

        em.flush();
        em.clear();

        // when
        Pageable pageable = PageRequest.of(0, 10);
        Page<OrderHistDto> orderHistDtoPage = orderService.getOrderList(member.getEmail(), pageable);

        // then
        assertThat(orderHistDtoPage.getTotalElements()).isEqualTo(3);
        assertThat(orderHistDtoPage.getContent()).hasSize(3);
        assertThat(orderHistDtoPage.getContent().get(0).getOrderItemDtoList()).isNotEmpty();
    }

    @Test
    @DisplayName("주문 목록 페이징 테스트")
    void getOrderListPagingTest() {
        // given
        Member member = saveMember();
        Item item = saveItem();
        createItemImg(item, "/images/item.jpg");

        // 주문 5개 생성
        for (int i = 0; i < 5; i++) {
            createOrder(member, item, 1);
        }

        em.flush();
        em.clear();

        // when - 페이지당 2개씩, 첫 번째 페이지 조회
        Pageable pageable = PageRequest.of(0, 2);
        Page<OrderHistDto> orderHistDtoPage = orderService.getOrderList(member.getEmail(), pageable);

        // then
        assertThat(orderHistDtoPage.getTotalElements()).isEqualTo(5);
        assertThat(orderHistDtoPage.getContent()).hasSize(2);
        assertThat(orderHistDtoPage.getTotalPages()).isEqualTo(3);
        assertThat(orderHistDtoPage.getNumber()).isEqualTo(0);
    }

    @Test
    @DisplayName("주문 내역이 없는 경우 테스트")
    void getOrderListEmptyTest() {
        // given
        Member member = saveMember();

        em.flush();
        em.clear();

        // when
        Pageable pageable = PageRequest.of(0, 10);
        Page<OrderHistDto> orderHistDtoPage = orderService.getOrderList(member.getEmail(), pageable);

        // then
        assertThat(orderHistDtoPage.getTotalElements()).isEqualTo(0);
        assertThat(orderHistDtoPage.getContent()).isEmpty();
    }

    @Test
    @DisplayName("주문 최신순 정렬 테스트")
    void getOrderListOrderByDateDescTest() {
        // given
        Member member = saveMember();
        Item item = saveItem();
        createItemImg(item, "/images/item.jpg");

        // 주문 3개 생성
        createOrder(member, item, 1);
        createOrder(member, item, 1);
        createOrder(member, item, 1);

        em.flush();
        em.clear();

        // when
        Pageable pageable = PageRequest.of(0, 10);
        Page<OrderHistDto> orderHistDtoPage = orderService.getOrderList(member.getEmail(), pageable);

        // then
        assertThat(orderHistDtoPage.getContent()).hasSize(3);
        // 최신 주문이 먼저 나와야 함 (desc 정렬)
        assertThat(orderHistDtoPage.getContent().get(0).getOrderId())
                .isGreaterThan(orderHistDtoPage.getContent().get(1).getOrderId());
        assertThat(orderHistDtoPage.getContent().get(1).getOrderId())
                .isGreaterThan(orderHistDtoPage.getContent().get(2).getOrderId());
    }
}