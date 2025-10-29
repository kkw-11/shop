package com.shop.entity;

import com.shop.dto.OrderDto;
import jakarta.persistence.*;
import lombok.Getter;
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

    @Column(name = "quantity")
    private Integer quantity;

    public static  OrderItem createOrderItem(Item item, int quantity) {
        OrderItem orderItem = new OrderItem();
        orderItem.setItem(item);
        orderItem.setQuantity(quantity);
        orderItem.setOrderPrice(item.getPrice());

        item.removeStock(quantity);
        return orderItem;
    }

    public static OrderItem createOrderItem(OrderDto orderDto, Map<Long, Item> itemMap) {
        OrderItem orderItem = new OrderItem();
        Item item = itemMap.get(orderDto.getItemId());

        if (item == null) {
            throw new EntityNotFoundException("요청된 상품 ID " + orderDto.getItemId() + "를 찾을 수 없습니다.");
        }

        orderItem.setItem(item);
        orderItem.setOrderPrice(item.getPrice());
        orderItem.setQuantity(orderDto.getQuantity());
        item.removeStock(orderDto.getQuantity());
        return orderItem;
    }

    public int getTotalPrice() {
        return orderPrice * quantity;
    }

    public void cancel() {
        this.getItem().addStock(quantity);
    }
}
