package com.shop.service;

import com.shop.contant.ItemSellStatus;
import com.shop.dto.ItemFormDto;
import com.shop.dto.ItemImgDto;
import com.shop.entity.Item;
import com.shop.entity.ItemImg;
import com.shop.repository.ItemImgRepository;
import com.shop.repository.ItemRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.PersistenceContext;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
@ActiveProfiles("test")
@Slf4j
class ItemServiceTest {
    @Autowired
    private ItemService itemService;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private ItemImgRepository itemImgRepository;

    @PersistenceContext
    private EntityManager em;

    private List<MultipartFile> createMultipartFiles() throws  Exception {
        List<MultipartFile> multipartFileList = new ArrayList<>();

        for(int i=0;i<5;i++){
            String path = "itemImgFile";
            String imageName = "image" + i + ".jpg";
            MultipartFile multipartFile = new MockMultipartFile(
                    path,
                    imageName,
                    "image/jpeg",  // "img/jpg"를 "image/jpeg"로 수정
                    new byte[]{1,2,3,4}
            );
            multipartFileList.add(multipartFile);
        }

        return multipartFileList;
    }


    private ItemFormDto registerItem() throws Exception{
        //상품, 상품 이미지 등록
        ItemFormDto itemFormDto = new ItemFormDto();

        itemFormDto.setItemNm("테스트상품");
        itemFormDto.setPrice(10000);
        itemFormDto.setStockNumber(100);
        itemFormDto.setItemDetail("상품 설명");
        itemFormDto.setItemSellStatus(ItemSellStatus.SELL);

        List<MultipartFile> multipartFiles = createMultipartFiles();
        Long savedItemId = itemService.saveItem(itemFormDto, multipartFiles);
        itemFormDto.setId(savedItemId);

        //상품 이미지 조회 응답
        List<ItemImg> itemImgList = itemImgRepository.findByItemIdOrderByIdAsc(savedItemId);
        List<ItemImgDto> itemImgDtoList = itemImgList.stream().map(itemImg -> ItemImgDto.of(itemImg)).collect(Collectors.toList());
        List<Long> itemImgIds = itemImgDtoList.stream().map(itemImgDto -> itemImgDto.getId()).collect(Collectors.toList());

        itemFormDto.setItemImgDtoList(itemImgDtoList);
        itemFormDto.setItemImgIds(itemImgIds);
        log.info("itemFormDto:{}", itemFormDto);

        return itemFormDto;

    }

    @Test
    @DisplayName("상품 등록 테스트")
    @WithMockUser(username = "admin", roles = "ADMIN")
    void saveItem() throws Exception{
        ItemFormDto itemFormDto = new ItemFormDto();
        itemFormDto.setItemNm("테스트상품");
        itemFormDto.setItemSellStatus(ItemSellStatus.SELL);
        itemFormDto.setItemDetail("테스트 상품입니다.");
        itemFormDto.setPrice(1000);
        itemFormDto.setStockNumber(100);

        List<MultipartFile> multipartFileList = createMultipartFiles();
        Long itemId = itemService.saveItem(itemFormDto, multipartFileList);

        List<ItemImg> itemImgList = itemImgRepository.findByItemIdOrderByIdAsc(itemId);
        Item item = itemRepository.findById(itemId).orElseThrow(EntityNotFoundException::new);

        assertEquals(itemFormDto.getItemNm(),item.getItemNm());
        assertEquals(itemFormDto.getItemSellStatus(),item.getItemSellStatus());
        assertEquals(itemFormDto.getItemDetail(),item.getItemDetail());
        assertEquals(itemFormDto.getPrice(), item.getPrice());
        assertEquals(itemFormDto.getStockNumber(), item.getStockNumber());
        assertEquals(multipartFileList.get(0).getOriginalFilename(), itemImgList.get(0).getOriImgName());

    }
}