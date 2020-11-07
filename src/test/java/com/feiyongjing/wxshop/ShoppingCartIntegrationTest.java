package com.feiyongjing.wxshop;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.feiyongjing.wxshop.controller.ShoppingCartContriller;
import com.feiyongjing.wxshop.entity.PageResponse;
import com.feiyongjing.wxshop.entity.Response;
import com.feiyongjing.wxshop.entity.ShoppingCartData;
import com.feiyongjing.wxshop.entity.ShoppingCartGoods;
import com.feiyongjing.wxshop.generate.Goods;
import com.feiyongjing.wxshop.generate.Shop;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

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
        List<ShoppingCartGoods> responseShoppingCartGoodsData = response.getData().getGoods();
        Assertions.assertEquals(200, httpResponse.code);
        Assertions.assertNull(response.getMassage());

        Assertions.assertEquals(1, responseShopData.getShop().getId());
        Assertions.assertEquals("shop1", responseShopData.getShop().getName());
        Assertions.assertEquals("desc1", responseShopData.getShop().getDescription());
        Assertions.assertEquals("url1", responseShopData.getShop().getImgUrl());
        Assertions.assertEquals(1L, responseShopData.getShop().getOwnerUserId());
        Assertions.assertEquals("ok", responseShopData.getShop().getStatus());

        Assertions.assertEquals(Arrays.asList(1L,2L),
                responseShoppingCartGoodsData.stream().map(Goods::getId).collect(Collectors.toList()));
        Assertions.assertEquals(Arrays.asList(100,2),
                responseShoppingCartGoodsData.stream().map(ShoppingCartGoods::getNumber).collect(Collectors.toList()));
        Assertions.assertTrue(responseShoppingCartGoodsData.stream().allMatch(
                shoppingCartGoods -> shoppingCartGoods.getShopId()==1
        ));
        Assertions.assertEquals(Arrays.asList(100L,100L),
                responseShoppingCartGoodsData.stream().map(ShoppingCartGoods::getPrice).collect(Collectors.toList()));
        Assertions.assertEquals(Arrays.asList("goods1","goods2"),
                responseShoppingCartGoodsData.stream().map(ShoppingCartGoods::getName).collect(Collectors.toList()));
        Assertions.assertEquals(Arrays.asList("desc1","desc2"),
                responseShoppingCartGoodsData.stream().map(ShoppingCartGoods::getDescription).collect(Collectors.toList()));
        Assertions.assertEquals(Arrays.asList("url1","url2"),
                responseShoppingCartGoodsData.stream().map(ShoppingCartGoods::getImgUrl).collect(Collectors.toList()));
        Assertions.assertEquals(Arrays.asList("details1","details2"),
                responseShoppingCartGoodsData.stream().map(ShoppingCartGoods::getDetails).collect(Collectors.toList()));
        Assertions.assertTrue(responseShoppingCartGoodsData.stream().allMatch(
                shoppingCartGoods -> shoppingCartGoods.getStatus().equals("ok")
        ));

    }

    @Test
    public void getShoppingCartTest() throws JsonProcessingException {
        UserLoginResponse userLoginResponse = loginAndGetCookie();
        HttpResponse httpResponse = doHttpResponse("/api/shoppingCart?pageNum=1&pageSize=3", "GET", null, userLoginResponse.cookie);
        Response<PageResponse<ShoppingCartData>> shoppingCartDataInResponse = objectMapper.readValue(httpResponse.body, new TypeReference<Response<PageResponse<ShoppingCartData>>>() {
        });
        PageResponse<ShoppingCartData> response=shoppingCartDataInResponse.getData();
        List<ShoppingCartData> responseData=response.getData();
        Assertions.assertEquals(200, httpResponse.code);

        Assertions.assertEquals(1, response.getPageNum());
        Assertions.assertEquals(3, response.getPageSize());
        Assertions.assertEquals(1, response.getTotalPage());
        Assertions.assertEquals(2,responseData.size());
        Assertions.assertEquals(Arrays.asList(1L,2L),
                responseData.stream().map(ShoppingCartData::getShop).map(Shop::getId).collect(Collectors.toList()));
        Assertions.assertEquals(Arrays.asList(4L,5L),
                responseData.get(1).getGoods().stream().map(ShoppingCartGoods::getId).collect(Collectors.toList()));
        Assertions.assertEquals(Arrays.asList(200,300),
                responseData.get(1).getGoods().stream().map(ShoppingCartGoods::getNumber).collect(Collectors.toList()));
        Assertions.assertEquals(Arrays.asList(100L,200L),
                responseData.get(1).getGoods().stream().map(ShoppingCartGoods::getPrice).collect(Collectors.toList()));
        Assertions.assertTrue(responseData.get(1).getGoods().stream().allMatch(
                shoppingCartGoods -> shoppingCartGoods.getShopId()==2L
        ));
        Assertions.assertTrue(responseData.get(1).getGoods().stream().allMatch(
                shoppingCartGoods -> shoppingCartGoods.getStatus().equals("ok")
        ));
    }

    @Test
    public void deleteGoodsInShoppingCartTest() throws JsonProcessingException {
        UserLoginResponse userLoginResponse = loginAndGetCookie();
        HttpResponse httpResponse = doHttpResponse("/api/shoppingCart/4", "DELETE", null, userLoginResponse.cookie);
        Response<ShoppingCartData> shoppingCartDataInResponse = objectMapper.readValue(httpResponse.body, new TypeReference<Response<ShoppingCartData>>() {
        });
        ShoppingCartData responseData=shoppingCartDataInResponse.getData();
        Assertions.assertEquals(200, httpResponse.code);

        Assertions.assertEquals(2L, responseData.getShop().getId());
        Assertions.assertEquals("shop2", responseData.getShop().getName());
        Assertions.assertEquals("desc2", responseData.getShop().getDescription());
        Assertions.assertEquals("url2", responseData.getShop().getImgUrl());
        Assertions.assertEquals(1L, responseData.getShop().getOwnerUserId());
        Assertions.assertEquals("ok", responseData.getShop().getStatus());
        Assertions.assertEquals(1,responseData.getGoods().size());
        Assertions.assertEquals(2L,
                responseData.getShop().getId());
        Assertions.assertEquals(Collections.singletonList(5L),
                responseData.getGoods().stream().map(ShoppingCartGoods::getId).collect(Collectors.toList()));
        Assertions.assertEquals(Collections.singletonList(300),
                responseData.getGoods().stream().map(ShoppingCartGoods::getNumber).collect(Collectors.toList()));
        Assertions.assertEquals(Collections.singletonList(200L),
                responseData.getGoods().stream().map(ShoppingCartGoods::getPrice).collect(Collectors.toList()));
    }

}
