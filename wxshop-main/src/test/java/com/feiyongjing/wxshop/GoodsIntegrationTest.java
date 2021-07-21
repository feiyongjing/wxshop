package com.feiyongjing.wxshop;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.feiyongjing.wxshop.entity.Response;
import com.feiyongjing.wxshop.generate.Goods;
import com.feiyongjing.wxshop.generate.Shop;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.math.BigDecimal;

import static javax.servlet.http.HttpServletResponse.SC_CREATED;
import static javax.servlet.http.HttpServletResponse.SC_OK;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = WxshopApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = {"spring.config.location=classpath:test-application.yml"})
public class GoodsIntegrationTest extends AbstractIntegrationTest {
    /**
     * 测试添加店铺接口
     * @param userLoginResponse 登录状态信息
     * @return 新添加的店铺ID
     * @throws JsonProcessingException
     */
    public Long testCreateShop(UserLoginResponse userLoginResponse) throws JsonProcessingException {

        Shop shop = new Shop();
        shop.setName("我的店铺");
        shop.setDescription("我的苹果专卖店");
        shop.setImgUrl("https://img.url");
        shop.setOwnerUserId(userLoginResponse.user.getId());

        HttpResponse shopResponse = doHttpResponse("/api/shop", "POST", shop, userLoginResponse.cookie);
        Response<Shop> shopInResponse = objectMapper.readValue(shopResponse.body, new TypeReference<Response<Shop>>() {
        });
        Assertions.assertEquals(201, shopResponse.code);
        Assertions.assertNull(shopInResponse.getMassage());
        Assertions.assertEquals("ok", shopInResponse.getData().getStatus());
        Assertions.assertEquals("我的店铺", shopInResponse.getData().getName());
        Assertions.assertEquals("我的苹果专卖店", shopInResponse.getData().getDescription());
        Assertions.assertEquals("https://img.url", shopInResponse.getData().getImgUrl());
        Assertions.assertEquals(userLoginResponse.user.getId(), shopInResponse.getData().getOwnerUserId());
        return shopInResponse.getData().getId();
    }

    /**
     * 测试添加商品接口
     * @param userLoginResponse 登录状态信息
     * @param shopId 商品所在店铺ID
     * @return 新添加的商品ID
     * @throws JsonProcessingException
     */
    public Long testCreateGoods(UserLoginResponse userLoginResponse, Long shopId) throws JsonProcessingException {
        Goods goods = new Goods();
        goods.setName("肥皂");
        goods.setDescription("纯天然无污染肥皂");
        goods.setDetails("这是一块好肥皂");
        goods.setImgUrl("https://img.url");
        goods.setPrice(new BigDecimal(1000));
        goods.setStock(10);
        goods.setShopId(shopId);

        HttpResponse goodsResponse = doHttpResponse("/api/goods", "POST", goods, userLoginResponse.cookie);
        Response<Goods> goodsInResponse = objectMapper.readValue(goodsResponse.body, new TypeReference<Response<Goods>>() {
        });
        Goods goodsResponseData = goodsInResponse.getData();
        Assertions.assertEquals(SC_CREATED, goodsResponse.code);
        Assertions.assertNull(goodsInResponse.getMassage());
        Assertions.assertEquals("ok", goodsResponseData.getStatus());
        Assertions.assertEquals("肥皂", goodsResponseData.getName());
        Assertions.assertEquals("纯天然无污染肥皂", goodsResponseData.getDescription());
        Assertions.assertEquals("这是一块好肥皂", goodsResponseData.getDetails());
        Assertions.assertEquals("https://img.url", goodsResponseData.getImgUrl());
        Assertions.assertEquals(new BigDecimal(1000), goodsResponseData.getPrice());
        Assertions.assertEquals(10, goodsResponseData.getStock());
        Assertions.assertEquals(shopId, goodsResponseData.getShopId());
        return goodsResponseData.getId();
    }

    /**
     * 查询店铺信息接口
     * @param userLoginResponse 登录状态信息
     * @param shopId 指定查询的店铺ID
     * @throws JsonProcessingException
     */
    public void testGetShopById(UserLoginResponse userLoginResponse, Long shopId) throws JsonProcessingException {
        HttpResponse shopResponse = doHttpResponse("/api/shop/" + shopId, "GET", null, userLoginResponse.cookie);
        Response<Shop> shopInResponse = objectMapper.readValue(shopResponse.body, new TypeReference<Response<Shop>>() {
        });
        Assertions.assertEquals(200, shopResponse.code);
        Assertions.assertNull(shopInResponse.getMassage());
        Assertions.assertEquals("ok", shopInResponse.getData().getStatus());
        Assertions.assertEquals("我的店铺", shopInResponse.getData().getName());
        Assertions.assertEquals("我的苹果专卖店", shopInResponse.getData().getDescription());
        Assertions.assertEquals("https://img.url", shopInResponse.getData().getImgUrl());
        Assertions.assertEquals(shopId, shopInResponse.getData().getId());
        Assertions.assertEquals(userLoginResponse.user.getId(), shopInResponse.getData().getOwnerUserId());
    }

    /**
     * 查询商品信息接口
     * @param userLoginResponse 登录状态信息
     * @param goodsId 指定查询的商品ID
     * @throws JsonProcessingException
     */
    public void testGetGoodsById(UserLoginResponse userLoginResponse, Long goodsId) throws JsonProcessingException {

        HttpResponse goodsResponse = doHttpResponse("/api/goods/" + goodsId, "GET", null, userLoginResponse.cookie);
        Response<Goods> goodsInResponse = objectMapper.readValue(goodsResponse.body, new TypeReference<Response<Goods>>() {
        });
        Goods goodsResponseData = goodsInResponse.getData();
        Assertions.assertEquals(200, goodsResponse.code);
        Assertions.assertNull(goodsInResponse.getMassage());
        Assertions.assertEquals("ok", goodsResponseData.getStatus());
        Assertions.assertEquals("肥皂", goodsResponseData.getName());
        Assertions.assertEquals("纯天然无污染肥皂", goodsResponseData.getDescription());
        Assertions.assertEquals("这是一块好肥皂", goodsResponseData.getDetails());
        Assertions.assertEquals("https://img.url", goodsResponseData.getImgUrl());
        Assertions.assertEquals(new BigDecimal(1000), goodsResponseData.getPrice());
        Assertions.assertEquals(10, goodsResponseData.getStock());
//        Assertions.assertEquals(1L, goodsResponseData.getShopId());
    }

    /**
     * 修改店铺信息接口
     * @param userLoginResponse 登录状态信息
     * @param shopId 指定店铺ID修改
     * @throws JsonProcessingException
     */
    public void testUpdateShop(UserLoginResponse userLoginResponse, Long shopId) throws JsonProcessingException {
        Shop shop = new Shop();
        shop.setId(shopId);
        shop.setOwnerUserId(userLoginResponse.user.getId());
        shop.setName("我的店铺");
        shop.setDescription("我的西瓜专卖店");
        shop.setImgUrl("https://img.url");

        HttpResponse shopResponse = doHttpResponse("/api/shop/" + shopId, "PATCH", shop, userLoginResponse.cookie);
        Response<Shop> shopInResponse = objectMapper.readValue(shopResponse.body, new TypeReference<Response<Shop>>() {
        });
        Assertions.assertEquals(200, shopResponse.code);
        Assertions.assertNull(shopInResponse.getMassage());
        Assertions.assertEquals(shopId, shopInResponse.getData().getId());
        Assertions.assertEquals("ok", shopInResponse.getData().getStatus());
        Assertions.assertEquals("我的店铺", shopInResponse.getData().getName());
        Assertions.assertEquals("我的西瓜专卖店", shopInResponse.getData().getDescription());
        Assertions.assertEquals("https://img.url", shopInResponse.getData().getImgUrl());
        Assertions.assertEquals(userLoginResponse.user.getId(), shopInResponse.getData().getOwnerUserId());
    }

    /**
     * 修改商品信息接口
     * @param userLoginResponse 登录状态信息
     * @param goodsId 指定商品ID修改
     * @param shopId 商品所属店铺ID
     * @throws JsonProcessingException
     */
    public void testUpdateGoods(UserLoginResponse userLoginResponse, Long goodsId, Long shopId) throws JsonProcessingException {
        Goods goods = new Goods();
        goods.setName("肥皂");
        goods.setDescription("纯天然无污染舒肤佳肥皂");
        goods.setDetails("这是一块好肥皂");
        goods.setImgUrl("https://img.url");
        goods.setPrice(new BigDecimal(500L));
        goods.setStock(20);
        goods.setShopId(shopId);

        HttpResponse goodsResponse = doHttpResponse("/api/goods/" + goodsId, "PATCH", goods, userLoginResponse.cookie);
        Response<Goods> goodsInResponse = objectMapper.readValue(goodsResponse.body, new TypeReference<Response<Goods>>() {
        });
        Goods goodsResponseData = goodsInResponse.getData();
        Assertions.assertEquals(SC_OK, goodsResponse.code);
        Assertions.assertNull(goodsInResponse.getMassage());
        Assertions.assertEquals("ok", goodsResponseData.getStatus());
        Assertions.assertEquals("肥皂", goodsResponseData.getName());
        Assertions.assertEquals("纯天然无污染舒肤佳肥皂", goodsResponseData.getDescription());
        Assertions.assertEquals("这是一块好肥皂", goodsResponseData.getDetails());
        Assertions.assertEquals("https://img.url", goodsResponseData.getImgUrl());
        Assertions.assertEquals(new BigDecimal(500), goodsResponseData.getPrice());
        Assertions.assertEquals(20, goodsResponseData.getStock());
        Assertions.assertEquals(shopId, goodsResponseData.getShopId());
    }

    /**
     * 删除商品信息接口
     * @param userLoginResponse 登录状态信息
     * @param goodsId 指定商品ID删除
     * @param shopId 商品所属店铺ID
     * @throws JsonProcessingException
     */
    public void testDeleteGoods(UserLoginResponse userLoginResponse, Long goodsId, Long shopId) throws JsonProcessingException {
        HttpResponse goodsResponse = doHttpResponse("/api/goods/" + goodsId, "DELETE", null, userLoginResponse.cookie);
        Assertions.assertEquals(204, goodsResponse.code);

        goodsResponse = doHttpResponse("/api/goods/" + goodsId, "GET", null, userLoginResponse.cookie);
        Response<Goods> goodsInResponse = objectMapper.readValue(goodsResponse.body, new TypeReference<Response<Goods>>() {
        });
        Assertions.assertEquals(200, goodsResponse.code);
        Assertions.assertEquals(goodsId, goodsInResponse.getData().getId());
        Assertions.assertEquals(shopId, goodsInResponse.getData().getShopId());
        Assertions.assertEquals("deleted", goodsInResponse.getData().getStatus());

    }

    /**
     * 删除店铺信息接口
     * @param userLoginResponse 登录状态信息
     * @param shopId 指定店铺ID删除
     * @throws JsonProcessingException
     */
    public void testDeleteShop(UserLoginResponse userLoginResponse, Long shopId) throws JsonProcessingException {
        HttpResponse shopResponse = doHttpResponse("/api/shop/" + shopId, "DELETE", null, userLoginResponse.cookie);
        Assertions.assertEquals(204, shopResponse.code);

        shopResponse = doHttpResponse("/api/shop/" + shopId, "GET", null, userLoginResponse.cookie);
        Response<Shop> shopInResponse = objectMapper.readValue(shopResponse.body, new TypeReference<Response<Shop>>() {
        });
        Assertions.assertEquals(200, shopResponse.code);
        Assertions.assertEquals(shopId, shopInResponse.getData().getId());
        Assertions.assertEquals(userLoginResponse.user.getId(), shopInResponse.getData().getOwnerUserId());
        Assertions.assertEquals("deleted", shopInResponse.getData().getStatus());

    }

    @Test
    public void testShopAndGoodsModule() throws JsonProcessingException {
        UserLoginResponse userLoginResponse = loginAndGetCookie();

        Long shopId = testCreateShop(userLoginResponse);
        Long goodsId = testCreateGoods(userLoginResponse, shopId);

        testGetShopById(userLoginResponse, shopId);
        testGetGoodsById(userLoginResponse, goodsId);

        testUpdateShop(userLoginResponse, shopId);
        testUpdateGoods(userLoginResponse, goodsId, shopId);

        testDeleteGoods(userLoginResponse, goodsId, shopId);
        testDeleteShop(userLoginResponse, shopId);
    }


}
