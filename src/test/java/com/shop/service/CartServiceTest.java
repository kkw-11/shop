package com.shop.service;

import com.shop.constant.ItemSellStatus;
import com.shop.dto.CartDetailDto;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.assertj.core.api.Assertions.assertThat;

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
    private ItemImgRepository itemImgRepository;

    @Autowired
    private CartRepository cartRepository;

    public Item saveItem(){
        Item item = new Item();
        item.setItemNm("테스트 상품");
        item.setPrice(1000);
        item.setItemDetail("테스트 상품 상세 설명");
        item.setItemSellStatus(ItemSellStatus.SELL);
        item.setStockNumber(100);
        return itemRepository.save(item);
    }

    private Item createItem(String itemNm, int price) {
        Item item = new Item();
        item.setItemNm(itemNm);
        item.setPrice(price);
        item.setItemDetail("테스트 상품 상세 설명");
        item.setItemSellStatus(ItemSellStatus.SELL);
        item.setStockNumber(100);
        return itemRepository.save(item);
    }

    private ItemImg createItemImg(Item item, String imgUrl) {
        ItemImg itemImg = new ItemImg();
        itemImg.setItem(item);
        itemImg.setImgName("test.jpg");
        itemImg.setOriImgName("test.jpg");
        itemImg.setImgUrl(imgUrl);
        itemImg.setRepImgYn("Y");
        return itemImgRepository.save(itemImg);
    }

    private CartItem createCartItem(Cart cart, Item item, int quantity) {
        CartItem cartItem = new CartItem();
        cartItem.setCart(cart);
        cartItem.setItem(item);
        cartItem.setQuantity(quantity);
        return cartItemRepository.save(cartItem);
    }

    public List<Item> saveItems(){
        List<Item> items = new ArrayList<>();
        for(int i = 0; i < 10; i++){
            Item item = saveItem();
            item.setItemNm(item.getItemNm() + i);
        }
        return items;
    }

    private Member createMember() {
        Member member = new Member();
        member.setEmail("test@test.com");
        member.setName("홍길동");
        member.setAddress("서울시");
        member.setPassword("1234");
        return memberRepository.save(member);
    }

    @Test
    @DisplayName("장바구니 담기 테스트")
    public void addCartTest(){
        Item item = saveItem();
        Member member = createMember();

        CartItemDto cartItemDto = new CartItemDto();
        cartItemDto.setQuantity(5);
        cartItemDto.setItemId(item.getId());

        Long cartItemId = cartService.addCart(cartItemDto, member.getEmail());

        CartItem cartItem = cartItemRepository.findById(cartItemId).orElseThrow(RuntimeException::new);

        assertEquals(item.getId(), cartItem.getItem().getId());
        assertEquals(cartItemDto.getQuantity(), cartItem.getQuantity());

    }

    @Test
    @DisplayName("장바구니 수량 업데이트 테스트")
    public void updateCartItemCountTest(){
        Item item = saveItem();
        Member member = createMember();

        CartItemDto cartItemDto = new CartItemDto();
        cartItemDto.setQuantity(4);
        cartItemDto.setItemId(item.getId());

        Long cartItemId = cartService.addCart(cartItemDto, member.getEmail());

        CartItem cartItem = cartItemRepository.findById(cartItemId).orElseThrow(RuntimeException::new);

        int count = cartItem.getQuantity() + 1;
        cartService.updateCartItemCount(cartItemId, count);

        assertEquals(count, cartItem.getQuantity());

    }

    @Test
    @DisplayName("장바구니 상품 주문 테스트")
    public void orderCartItemTest(){
        List<Item> items = saveItems();
        Member member = createMember();

        List<CartItemDto> cartItemDtoList = items.stream().map(item -> {
            CartItemDto cartItemDto = new CartItemDto();
            cartItemDto.setItemId(item.getId());
            cartItemDto.setQuantity(1);
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

    @Test
    @DisplayName("장바구니 상세 목록 조회 테스트")
    void findCartDetailDtoListTest() {
        // given
        Member member = createMember();

        Cart cart = new Cart();
        cart.setMember(member);
        cartRepository.save(cart);

        Item item1 = createItem("테스트 상품1", 10000);
        Item item2 = createItem("테스트 상품2", 20000);
        Item item3 = createItem("테스트 상품3", 30000);

        createItemImg(item1, "/images/item1.jpg");
        createItemImg(item2, "/images/item2.jpg");
        createItemImg(item3, "/images/item3.jpg");

        createCartItem(cart, item1, 2);
        createCartItem(cart, item2, 1);
        createCartItem(cart, item3, 3);

        em.flush();
        em.clear();

        // when
        List<CartDetailDto> result = cartItemRepository.findCartDetailDtoList(cart.getId());

        // then
        assertThat(result).hasSize(3);
        assertThat(result.get(0).getItemNm()).isNotNull();
        assertThat(result.get(0).getImgUrl()).isNotNull();
    }

    @Test
    @DisplayName("장바구니 상세 목록 - 최신순 정렬 테스트")
    void findCartDetailDtoListOrderByRegTimeDescTest() {
        // given
        Member member = createMember();

        Cart cart = new Cart();
        cart.setMember(member);
        cartRepository.save(cart);

        Item item1 = createItem("상품1", 10000);
        Item item2 = createItem("상품2", 20000);
        Item item3 = createItem("상품3", 30000);

        createItemImg(item1, "/images/item1.jpg");
        createItemImg(item2, "/images/item2.jpg");
        createItemImg(item3, "/images/item3.jpg");

        CartItem cartItem1 = createCartItem(cart, item1, 1);
        CartItem cartItem2 = createCartItem(cart, item2, 1);
        CartItem cartItem3 = createCartItem(cart, item3, 1);

        em.flush();
        em.clear();

        // when
        List<CartDetailDto> result = cartItemRepository.findCartDetailDtoList(cart.getId());

        // then
        assertThat(result).hasSize(3);
        // 최신순 정렬 확인 (마지막에 추가한 것이 먼저)
        assertThat(result.get(0).getCartItemId()).isEqualTo(cartItem3.getId());
        assertThat(result.get(1).getCartItemId()).isEqualTo(cartItem2.getId());
        assertThat(result.get(2).getCartItemId()).isEqualTo(cartItem1.getId());
    }

    @Test
    @DisplayName("장바구니 상세 목록 - 빈 장바구니")
    void findCartDetailDtoListEmptyTest() {
        // given
        Member member = createMember();

        Cart cart = new Cart();
        cart.setMember(member);
        cartRepository.save(cart);

        em.flush();
        em.clear();

        // when
        List<CartDetailDto> result = cartItemRepository.findCartDetailDtoList(cart.getId());

        // then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("장바구니 상세 목록 - 대표 이미지만 조회")
    void findCartDetailDtoListOnlyRepImgTest() {
        // given
        Member member = createMember();

        Cart cart = new Cart();
        cart.setMember(member);
        cartRepository.save(cart);

        Item item = createItem("테스트 상품", 10000);

        // 대표 이미지
        ItemImg repImg = new ItemImg();
        repImg.setItem(item);
        repImg.setImgUrl("/images/rep.jpg");
        repImg.setRepImgYn("Y");
        itemImgRepository.save(repImg);

        // 일반 이미지
        ItemImg normalImg = new ItemImg();
        normalImg.setItem(item);
        normalImg.setImgUrl("/images/normal.jpg");
        normalImg.setRepImgYn("N");
        itemImgRepository.save(normalImg);

        createCartItem(cart, item, 1);

        em.flush();
        em.clear();

        // when
        List<CartDetailDto> result = cartItemRepository.findCartDetailDtoList(cart.getId());

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getImgUrl()).isEqualTo("/images/rep.jpg");  // 대표 이미지만
    }

    @Test
    @DisplayName("장바구니에서 주문 시 재고 감소 테스트")
    void orderCartItemDecreaseStockTest() {
        // given
        Member member = createMember();
        
        Item item1 = createItem("상품1", 10000);
        Item item2 = createItem("상품2", 20000);
        Item item3 = createItem("상품3", 30000);
        
        int initialStock1 = item1.getStockNumber();
        int initialStock2 = item2.getStockNumber();
        int initialStock3 = item3.getStockNumber();

        CartItemDto cartItemDto1 = new CartItemDto();
        cartItemDto1.setItemId(item1.getId());
        cartItemDto1.setQuantity(5);
        
        CartItemDto cartItemDto2 = new CartItemDto();
        cartItemDto2.setItemId(item2.getId());
        cartItemDto2.setQuantity(3);
        
        CartItemDto cartItemDto3 = new CartItemDto();
        cartItemDto3.setItemId(item3.getId());
        cartItemDto3.setQuantity(7);

        Long cartItemId1 = cartService.addCart(cartItemDto1, member.getEmail());
        Long cartItemId2 = cartService.addCart(cartItemDto2, member.getEmail());
        Long cartItemId3 = cartService.addCart(cartItemDto3, member.getEmail());

        List<Long> cartItemIds = List.of(cartItemId1, cartItemId2, cartItemId3);

        // when
        Long orderId = cartService.orderCartItem(cartItemIds, member.getEmail());

        // then
        em.flush();
        em.clear();
        
        Item orderedItem1 = itemRepository.findById(item1.getId()).orElseThrow();
        Item orderedItem2 = itemRepository.findById(item2.getId()).orElseThrow();
        Item orderedItem3 = itemRepository.findById(item3.getId()).orElseThrow();
        
        assertThat(orderedItem1.getStockNumber()).isEqualTo(initialStock1 - 5);
        assertThat(orderedItem2.getStockNumber()).isEqualTo(initialStock2 - 3);
        assertThat(orderedItem3.getStockNumber()).isEqualTo(initialStock3 - 7);
        
        // 주문이 정상적으로 생성되었는지 확인
        Order order = orderRepository.findById(orderId).orElseThrow();
        assertThat(order.getOrderItems()).hasSize(3);
        
        // 장바구니에서 제거되었는지 확인
        List<CartItem> remainingCartItems = cartItemRepository.findAllById(cartItemIds);
        assertThat(remainingCartItems).isEmpty();
    }

    @Test
    @DisplayName("장바구니 주문 시 재고 부족 예외 테스트")
    void orderCartItemOutOfStockExceptionTest() {
        // given
        Member member = createMember();
        
        Item item = createItem("테스트 상품", 10000);
        item.setStockNumber(5); // 재고 5개로 설정
        itemRepository.save(item);
        
        CartItemDto cartItemDto = new CartItemDto();
        cartItemDto.setItemId(item.getId());
        cartItemDto.setQuantity(10); // 10개 장바구니에 담기

        Long cartItemId = cartService.addCart(cartItemDto, member.getEmail());

        // when & then
        assertThrows(com.shop.exception.OutOfStockException.class, () -> {
            cartService.orderCartItem(List.of(cartItemId), member.getEmail());
        });
        
        // 재고는 변경되지 않아야 함
        em.flush();
        em.clear();
        Item unchangedItem = itemRepository.findById(item.getId()).orElseThrow();
        assertThat(unchangedItem.getStockNumber()).isEqualTo(5);
        
        // 장바구니 아이템도 그대로 남아있어야 함
        CartItem cartItem = cartItemRepository.findById(cartItemId).orElseThrow();
        assertThat(cartItem.getQuantity()).isEqualTo(10);
    }
}