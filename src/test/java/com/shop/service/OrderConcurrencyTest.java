package com.shop.service;

import com.shop.constant.ItemSellStatus;
import com.shop.dto.OrderDto;
import com.shop.entity.Item;
import com.shop.entity.Member;
import com.shop.repository.ItemRepository;
import com.shop.repository.MemberRepository;
import com.shop.repository.OrderRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;


@SpringBootTest
@ActiveProfiles("test")
class OrderConcurrencyTest {

    @Autowired
    private OrderService orderService;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Test
    @DisplayName("Lost Update 문제 재현: 10명이 동시에 같은 상품 주문")
    void lostUpdateProblemTest() throws InterruptedException {
        // given: 재고가 충분한 상품
        Item item = new Item();
        item.setItemNm("인기 상품");
        item.setPrice(10000);
        item.setItemDetail("재고가 충분한 상품");
        item.setItemSellStatus(ItemSellStatus.SELL);
        item.setStockNumber(100); // 충분한 재고
        item = itemRepository.save(item);

        // 10명의 회원 생성
        int memberCount = 10;
        for (int i = 0; i < memberCount; i++) {
            Member member = new Member();
            member.setEmail("user" + i + "@test.com");
            member.setName("사용자" + i);
            memberRepository.save(member);
        }

        final Long itemId = item.getId();

        // when: 10명이 동시에 2개씩 주문 (총 20개 주문)
        int threadCount = 10;
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);
        
        AtomicInteger successCount = new AtomicInteger(0);

        for (int i = 0; i < threadCount; i++) {
            final int userIndex = i;
            executorService.execute(() -> {
                try {
                    OrderDto orderDto = new OrderDto();
                    orderDto.setItemId(itemId);
                    orderDto.setQuantity(2);
                    
                    orderService.order(orderDto, "user" + userIndex + "@test.com");
                    successCount.incrementAndGet();
                } catch (Exception e) {
                    System.err.println("주문 실패: " + e.getMessage());
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executorService.shutdown();

        // then: Lost Update 문제 확인
        Item resultItem = itemRepository.findById(itemId).orElseThrow();
        
        int expectedStock = 100 - (successCount.get() * 2);
        int actualStock = resultItem.getStockNumber();
        
        System.out.println("=== Lost Update 문제 재현 결과 ===");
        System.out.println("성공한 주문 수: " + successCount.get() + "명");
        System.out.println("주문한 총 수량: " + (successCount.get() * 2) + "개");
        System.out.println("예상 재고: " + expectedStock + "개");
        System.out.println("실제 재고: " + actualStock + "개");
        System.out.println("재고 차이: " + (actualStock - expectedStock) + "개");
        
        // Lost Update가 발생하면 실제 재고가 예상보다 많이 남음
        // 예: 10명이 각각 차감했지만, 업데이트가 덮어씌워져서 마치 1~2명만 차감한 것처럼 보임
        assertThat(actualStock).isEqualTo(expectedStock);
        System.out.println("Lost Update 문제 발생! 동시성 제어 필요");
    }

    @Test
    @DisplayName("동시성 제어 없이 재고 부족 상황")
    void outOfStockWithoutConcurrencyControl() throws InterruptedException {
        // given: 재고가 부족한 상품
        Item item = new Item();
        item.setItemNm("한정판 상품");
        item.setPrice(10000);
        item.setItemDetail("재고가 부족한 인기 상품");
        item.setItemSellStatus(ItemSellStatus.SELL);
        item.setStockNumber(10); // 부족한 재고
        item = itemRepository.save(item);

        int memberCount = 10;
        for (int i = 0; i < memberCount; i++) {
            Member member = new Member();
            member.setEmail("buyer" + i + "@test.com");
            member.setName("구매자" + i);
            memberRepository.save(member);
        }

        final Long itemId = item.getId();

        // when: 10명이 동시에 2개씩 주문 시도 (총 20개 주문, 재고는 10개)
        int threadCount = 10;
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);
        
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);

        for (int i = 0; i < threadCount; i++) {
            final int userIndex = i;
            executorService.execute(() -> {
                try {
                    OrderDto orderDto = new OrderDto();
                    orderDto.setItemId(itemId);
                    orderDto.setQuantity(2);
                    
                    orderService.order(orderDto, "buyer" + userIndex + "@test.com");
                    successCount.incrementAndGet();
                } catch (Exception e) {
                    failCount.incrementAndGet();
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executorService.shutdown();

        // then
        Item resultItem = itemRepository.findById(itemId).orElseThrow();
        
        System.out.println("=== 재고 부족 상황 (동시성 제어 없음) ===");
        System.out.println("성공한 주문: " + successCount.get() + "명");
        System.out.println("실패한 주문: " + failCount.get() + "명");
        System.out.println("최종 재고: " + resultItem.getStockNumber() + "개");
        
        // 동시성 제어가 없으면 재고 10개에 20개 주문이 모두 성공할 수 있음
        System.out.println("재고 부족 상황에서도 Lost Update 문제 발생 가능");
    }

    @Test
    @DisplayName("순차 주문 (정상 케이스)")
    void sequentialOrderTest() {
        // given
        Item item = new Item();
        item.setItemNm("순차 테스트 상품");
        item.setPrice(10000);
        item.setItemDetail("순차 주문 테스트");
        item.setItemSellStatus(ItemSellStatus.SELL);
        item.setStockNumber(100);
        item = itemRepository.save(item);

        Member member = new Member();
        member.setEmail("sequential@test.com");
        member = memberRepository.save(member);

        final Long itemId = item.getId();
        final String email = member.getEmail();

        // when: 순차적으로 10번 주문 (각 2개씩 = 총 20개)
        for (int i = 0; i < 10; i++) {
            OrderDto orderDto = new OrderDto();
            orderDto.setItemId(itemId);
            orderDto.setQuantity(2);
            orderService.order(orderDto, email);
        }

        // then
        Item resultItem = itemRepository.findById(itemId).orElseThrow();
        
        System.out.println("=== 순차 주문 테스트 결과 ===");
        System.out.println("최종 재고: " + resultItem.getStockNumber() + "개");
        
        // 순차 처리에서는 항상 정확한 재고 계산
        assertThat(resultItem.getStockNumber()).isEqualTo(80);
        System.out.println("순차 처리는 정상 작동");
    }
}
