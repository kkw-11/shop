package com.shop.dto;

import com.querydsl.core.annotations.QueryProjection;
import com.shop.constant.ItemSellStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class ItemMngDto {
    private Long id;
    private String itemNm;
    private ItemSellStatus itemSellStatus;
    private String createdBy;
    private LocalDateTime regTime;

    @QueryProjection
    public ItemMngDto(Long id, String itemNm, ItemSellStatus itemSellStatus,String createdBy, LocalDateTime regTime) {
        this.id = id;
        this.itemNm = itemNm;
        this.itemSellStatus = itemSellStatus;
        this.createdBy = createdBy;
        this.regTime = regTime;
    }
}
