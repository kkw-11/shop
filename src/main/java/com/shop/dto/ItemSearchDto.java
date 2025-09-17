package com.shop.dto;

import com.shop.constant.ItemSellStatus;
import lombok.Getter;
import lombok.Setter;

/**
 * 검색 조건 DTO
 */
@Getter @Setter
public class ItemSearchDto {
    private String searchDateType; // 상품 등록일
    private ItemSellStatus searchSellStatus; //상품 판매 상태
    private String searchBy; // 상품 조회 검색 유형(상품명, 상품 등록자 아이디)
    private String searchQuery = ""; // 조회할 검색어
}
