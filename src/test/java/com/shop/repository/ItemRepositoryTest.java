package com.shop.repository;

import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.shop.contant.ItemSellStatus;
import com.shop.entity.Item;
import com.shop.entity.QItem;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestPropertySource(locations = "classpath:application-test.yml")
class ItemRepositoryTest {

    @PersistenceContext
    private EntityManager em;

    @Autowired
    private ItemRepository itemRepository;

    @Test
    @DisplayName("상품 저장 테스트")
    public void createItemTest(){
        Item item = new Item();
        item.setItemNm("테스트 상품");
        item.setPrice(10000);
        item.setItemDetail("테스트 상품 상세 설명");
        item.setItemSellStatus(ItemSellStatus.SELL);
        item.setStockNumber(100);
        Item savedItem = itemRepository.save(item);
        System.out.println(savedItem.toString());

    }

    public void createItemList(){
        for (int i = 1; i <= 10; i++) {
            Item item = new Item();
            item.setItemNm("테스트 상품" + i);
            item.setPrice(10000 + i);
            item.setItemDetail("테스트 상품 상세 설명" + i);
            item.setItemSellStatus(ItemSellStatus.SELL);
            item.setStockNumber(100);
            itemRepository.save(item);

        }
    }

    @Test
    @DisplayName("상품명 조회  테스트")
    public void findByItemNmTest(){
        this.createItemList();
        String itemNm = "테스트 상품1";
        List<Item> itemList = itemRepository.findByItemNm(itemNm);

        assertEquals(itemNm, itemList.get(0).getItemNm());

    }

    @Test
    @DisplayName("상품명, 상품상세설명 or 테스트")
    public void findByItemNmOrItemDetailTest(){
        this.createItemList();
        String searchNm = "테스트 상품1";
        String searchDetail = "테스트 상품 상세 설명5";
        List<Item> itemList = itemRepository.findByItemNmOrItemDetail(searchNm, searchDetail);


//        // Then: 결과 검증
        assertNotNull(itemList);
        assertEquals(2, itemList.size());
//
//        // 각각의 결과가 기대한 항목인지 확인
        assertTrue(itemList.stream().anyMatch(i -> i.getItemNm().equals(searchNm)));
        assertTrue(itemList.stream().anyMatch(i -> i.getItemDetail().equals(searchDetail)));

    }

    @Test
    @DisplayName("가격 LessThan 테스트")
    public void findByPriceLessThanTest(){
        this.createItemList();
        Integer price = 10005;
        List<Item> itemList = itemRepository.findByPriceLessThan(price);

        // 스트림으로 모든 상품 가격이 10005 미만인지 확인
        assertTrue(itemList.stream().allMatch(item -> item.getPrice() < price));

        System.out.println(itemList.toString());
    }

    @Test
    @DisplayName("가격 내림차순 조회 테스트")
    public void findByPriceLessThanOrderByPriceDescTest(){
        this.createItemList();
        Integer price = 10005;
        List<Item> itemList = itemRepository.findByPriceLessThanOrderByPriceDesc(price);


        // 모든 상품 가격이 조건(10005 미만)을 만족하는지 확인
        assertTrue(itemList.stream().allMatch(item -> item.getPrice() < price));

        // 내림차순 정렬이 잘 되었는지 확인
        List<Integer> prices = itemList.stream().map(Item::getPrice).toList();

        List<Integer> sortedPrices = prices.stream().sorted(Comparator.reverseOrder()).toList();

        assertEquals(sortedPrices, prices);
    }

    @Test
    @DisplayName("@Query를 이용한 상세설명으로 상품 조회 테스트")
    public void findByItemDetailTest(){
        this.createItemList();
        String itemDetail = "테스트 상품 상세 설명";
        List<Item> itemList = itemRepository.findByItemDetail(itemDetail);

        assertTrue(itemList.stream().allMatch(item -> item.getItemDetail().contains(itemDetail)));
    }

    @Test
    @DisplayName("Querydsl 조회 테스트1")
    public void queryDslTest(){
        this.createItemList();

        String detailNm = "테스트 상품 상세 설명";

        JPAQueryFactory queryFactory = new JPAQueryFactory(em);
        QItem qItem = QItem.item;

        JPAQuery<Item> query = queryFactory.selectFrom(qItem)
                .where(qItem.itemSellStatus.eq(ItemSellStatus.SELL))
                .where(qItem.itemDetail.like("%" + detailNm + "%"))
                .orderBy(qItem.price.desc());

        List<Item> itemList = query.fetch();

        assertTrue(itemList.stream().allMatch(item -> item.getItemDetail().contains(detailNm)));

    }
}