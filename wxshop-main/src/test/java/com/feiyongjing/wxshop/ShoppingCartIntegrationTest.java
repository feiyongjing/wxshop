package com.feiyongjing.wxshop;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.feiyongjing.wxshop.controller.ShoppingCartContriller;
import com.feiyongjing.wxshop.entity.GoodsWithNumber;
import com.feiyongjing.wxshop.api.data.PageResponse;
import com.feiyongjing.wxshop.entity.Response;
import com.feiyongjing.wxshop.entity.ShoppingCartData;
import com.feiyongjing.wxshop.generate.Goods;
import com.feiyongjing.wxshop.generate.Shop;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = WxshopApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = {"spring.config.location=classpath:test-application.yml"})
public class ShoppingCartIntegrationTest extends AbstractIntegrationTest {

    @Test
    public void createShoppingCartTest() throws JsonProcessingException {
        UserLoginResponse userLoginResponse = loginAndGetCookie();

        ShoppingCartContriller.AddShoppingCartRequest body = new ShoppingCartContriller.AddShoppingCartRequest();
        ShoppingCartContriller.AddShoppingCartItem item = new ShoppingCartContriller.AddShoppingCartItem();

        item.setId(2L);
        item.setNumber(2);
        body.setGoods(Collections.singletonList(item));

        HttpResponse httpResponse = doHttpResponse("/api/shoppingCart", "POST", body, userLoginResponse.cookie);

        Response<ShoppingCartData> response = objectMapper.readValue(httpResponse.body, new TypeReference<Response<ShoppingCartData>>() {
        });
        ShoppingCartData responseShopData = response.getData();
        List<GoodsWithNumber> responseGoodsWithNumberData = response.getData().getGoods();
        Assertions.assertEquals(200, httpResponse.code);
        Assertions.assertNull(response.getMassage());

        Assertions.assertEquals(1, responseShopData.getShop().getId());
        Assertions.assertEquals("shop1", responseShopData.getShop().getName());
        Assertions.assertEquals("desc1", responseShopData.getShop().getDescription());
        Assertions.assertEquals("url1", responseShopData.getShop().getImgUrl());
        Assertions.assertEquals(1L, responseShopData.getShop().getOwnerUserId());
        Assertions.assertEquals("ok", responseShopData.getShop().getStatus());

        Assertions.assertEquals(Arrays.asList(1L, 2L),
                responseGoodsWithNumberData.stream().map(Goods::getId).collect(Collectors.toList()));
        Assertions.assertEquals(Arrays.asList(100, 2),
                responseGoodsWithNumberData.stream().map(GoodsWithNumber::getNumber).collect(Collectors.toList()));
        Assertions.assertTrue(responseGoodsWithNumberData.stream().allMatch(
                goodsWithNumber -> goodsWithNumber.getShopId() == 1
        ));
        Assertions.assertEquals(Arrays.asList(new BigDecimal(100), new BigDecimal(100)),
                responseGoodsWithNumberData.stream().map(GoodsWithNumber::getPrice).collect(Collectors.toList()));
        Assertions.assertEquals(Arrays.asList("goods1", "goods2"),
                responseGoodsWithNumberData.stream().map(GoodsWithNumber::getName).collect(Collectors.toList()));
        Assertions.assertEquals(Arrays.asList("desc1", "desc2"),
                responseGoodsWithNumberData.stream().map(GoodsWithNumber::getDescription).collect(Collectors.toList()));
        Assertions.assertEquals(Arrays.asList("url1", "url2"),
                responseGoodsWithNumberData.stream().map(GoodsWithNumber::getImgUrl).collect(Collectors.toList()));
        Assertions.assertEquals(Arrays.asList("details1", "details2"),
                responseGoodsWithNumberData.stream().map(GoodsWithNumber::getDetails).collect(Collectors.toList()));
        Assertions.assertTrue(responseGoodsWithNumberData.stream().allMatch(
                goodsWithNumber -> goodsWithNumber.getStatus().equals("ok")
        ));

    }

    @Test
    public void getShoppingCartTest() throws JsonProcessingException {
        UserLoginResponse userLoginResponse = loginAndGetCookie();
        HttpResponse httpResponse = doHttpResponse("/api/shoppingCart?pageNum=1&pageSize=3", "GET", null, userLoginResponse.cookie);
        Response<PageResponse<ShoppingCartData>> shoppingCartDataInResponse = objectMapper.readValue(httpResponse.body, new TypeReference<Response<PageResponse<ShoppingCartData>>>() {
        });
        PageResponse<ShoppingCartData> response = shoppingCartDataInResponse.getData();
        List<ShoppingCartData> responseData = response.getData();
        Assertions.assertEquals(200, httpResponse.code);

        Assertions.assertEquals(1, response.getPageNum());
        Assertions.assertEquals(3, response.getPageSize());
        Assertions.assertEquals(1, response.getTotalPage());
        Assertions.assertEquals(2, responseData.size());
        Assertions.assertEquals(Arrays.asList(1L, 2L),
                responseData.stream().map(ShoppingCartData::getShop).map(Shop::getId).collect(Collectors.toList()));
        Assertions.assertEquals(Arrays.asList(4L, 5L),
                responseData.get(1).getGoods().stream().map(GoodsWithNumber::getId).collect(Collectors.toList()));
        Assertions.assertEquals(Arrays.asList(200, 300),
                responseData.get(1).getGoods().stream().map(GoodsWithNumber::getNumber).collect(Collectors.toList()));
        Assertions.assertEquals(Arrays.asList(new BigDecimal(100), new BigDecimal(200)),
                responseData.get(1).getGoods().stream().map(GoodsWithNumber::getPrice).collect(Collectors.toList()));
        Assertions.assertTrue(responseData.get(1).getGoods().stream().allMatch(
                goodsWithNumber -> goodsWithNumber.getShopId() == 2L
        ));
        Assertions.assertTrue(responseData.get(1).getGoods().stream().allMatch(
                goodsWithNumber -> goodsWithNumber.getStatus().equals("ok")
        ));
    }

    @Test
    public void deleteGoodsInShoppingCartTest() throws JsonProcessingException {
        UserLoginResponse userLoginResponse = loginAndGetCookie();
        HttpResponse httpResponse = doHttpResponse("/api/shoppingCart/4", "DELETE", null, userLoginResponse.cookie);
        Response<ShoppingCartData> shoppingCartDataInResponse = objectMapper.readValue(httpResponse.body, new TypeReference<Response<ShoppingCartData>>() {
        });
        ShoppingCartData responseData = shoppingCartDataInResponse.getData();
        Assertions.assertEquals(200, httpResponse.code);

        Assertions.assertEquals(2L, responseData.getShop().getId());
        Assertions.assertEquals("shop2", responseData.getShop().getName());
        Assertions.assertEquals("desc2", responseData.getShop().getDescription());
        Assertions.assertEquals("url2", responseData.getShop().getImgUrl());
        Assertions.assertEquals(1L, responseData.getShop().getOwnerUserId());
        Assertions.assertEquals("ok", responseData.getShop().getStatus());
        Assertions.assertEquals(1, responseData.getGoods().size());
        Assertions.assertEquals(2L,
                responseData.getShop().getId());
        Assertions.assertEquals(Collections.singletonList(5L),
                responseData.getGoods().stream().map(GoodsWithNumber::getId).collect(Collectors.toList()));
        Assertions.assertEquals(Collections.singletonList(300),
                responseData.getGoods().stream().map(GoodsWithNumber::getNumber).collect(Collectors.toList()));
        Assertions.assertEquals(Collections.singletonList(new BigDecimal(200)),
                responseData.getGoods().stream().map(GoodsWithNumber::getPrice).collect(Collectors.toList()));
    }

}
