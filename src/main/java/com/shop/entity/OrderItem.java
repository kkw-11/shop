package com.shop.entity;

import com.shop.dto.OrderDto;
import com.shop.repository.ItemRepository;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Map;

@Entity
@Table(name = "order_item")
@Getter @Setter
public class OrderItem extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_item_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id")
    private Item item;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private Order order;

    private int orderPrice;

    private int count;

    public static  OrderItem createOrderItem(Item item, int count) {
        OrderItem orderItem = new OrderItem();
        orderItem.setItem(item);
        orderItem.setCount(count);
        orderItem.setOrderPrice(item.getPrice());

        item.removeStock(count);
        return orderItem;
    }

    // 정적 팩토리 메서드 (Setter 대신 생성자 사용)
    public static OrderItem createOrderItem(OrderDto orderDto, ItemRepository itemRepository) {
        OrderItem orderItem = new OrderItem();
        Item item = itemRepository.findById(orderDto.getItemId())
                .orElseThrow(EntityNotFoundException::new);

        orderItem.setItem(item);
        orderItem.setCount(orderDto.getCount());
        return orderItem;
    }


    public static OrderItem createOrderItem(OrderDto orderDto, Map<Long, Item> itemMap) {
        OrderItem orderItem = new OrderItem();
        // DB 호출 대신 Map에서 O(1)의 속도로 Item을 조회
        Item item = itemMap.get(orderDto.getItemId());

        if (item == null) {
            // DTO에 포함된 Item ID가 DB에 없는 경우 처리 (필수 예외 처리)
            throw new EntityNotFoundException("요청된 상품 ID " + orderDto.getItemId() + "를 찾을 수 없습니다.");
        }

        orderItem.setItem(item);
        orderItem.setCount(orderDto.getCount());
        return orderItem;
    }

    public int getTotalPrice() {
        return orderPrice * count;
    }

    public void cancel() {
        this.getItem().addStock(count);
    }
}
