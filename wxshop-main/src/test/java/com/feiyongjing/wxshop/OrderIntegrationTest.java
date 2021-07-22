package com.feiyongjing.wxshop;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.feiyongjing.wxshop.api.DataStatus;
import com.feiyongjing.wxshop.api.data.GoodsInfo;
import com.feiyongjing.wxshop.api.data.OrderInfo;
import com.feiyongjing.wxshop.api.data.PageResponse;
import com.feiyongjing.wxshop.api.data.RpcOrderGoods;
import com.feiyongjing.wxshop.api.generate.Order;
import com.feiyongjing.wxshop.entity.GoodsWithNumber;
import com.feiyongjing.wxshop.entity.OrderResponse;
import com.feiyongjing.wxshop.entity.Response;
import com.feiyongjing.wxshop.generate.Goods;
import com.feiyongjing.wxshop.generate.Shop;
import com.feiyongjing.wxshop.mock.MockOrderRpcService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = WxshopApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = {"spring.config.location=classpath:test-application.yml"})
public class OrderIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    MockOrderRpcService mockOrderRpcService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(mockOrderRpcService);

        when(mockOrderRpcService.orderRpcService.createOrder(any(), any())).thenAnswer(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                Order order = invocation.getArgument(1);
                order.setId(1234L);
                return order;
            }
        });
    }

    /**
     * 订单添加接口成添加测试
     *
     * @throws JsonProcessingException
     */
    @Test
    public void canCreateOrder() throws JsonProcessingException {
        UserLoginResponse userLoginResponse = loginAndGetCookie();
        OrderInfo orderInfo = new OrderInfo();

        GoodsInfo goodsInfo1 = new GoodsInfo();
        GoodsInfo goodsInfo2 = new GoodsInfo();

        goodsInfo1.setId(1);
        goodsInfo1.setNumber(3);
        goodsInfo2.setId(2);
        goodsInfo2.setNumber(1);
        orderInfo.setGoodsInfos(Arrays.asList(goodsInfo1, goodsInfo2));

        Response<OrderResponse> orderInResponse = doHttpResponse("/api/order", "POST", orderInfo, userLoginResponse.cookie)
                .assertOkStatusCode()
                .asJsonObject(new TypeReference<Response<OrderResponse>>() {});

        Shop returnShop = orderInResponse.getData().getShop();
        List<GoodsWithNumber> returnGoodsList = orderInResponse.getData().getGoods();

        Assertions.assertNull(orderInResponse.getMassage());
        Assertions.assertEquals(1234L, orderInResponse.getData().getId());
        Assertions.assertEquals(DataStatus.PENDING.getName(), orderInResponse.getData().getStatus());
        Assertions.assertEquals("火星", orderInResponse.getData().getAddress());

        Assertions.assertEquals(1L, returnShop.getId());
        Assertions.assertEquals("shop1", returnShop.getName());
        Assertions.assertEquals("ok", returnShop.getStatus());
        Assertions.assertEquals("url1", returnShop.getImgUrl());
        Assertions.assertEquals("desc1", returnShop.getDescription());

        Assertions.assertEquals(Arrays.asList(1L, 2L), returnGoodsList.stream().map(GoodsWithNumber::getId).collect(Collectors.toList()));
        Assertions.assertEquals(Arrays.asList("goods1", "goods2"), returnGoodsList.stream().map(GoodsWithNumber::getName).collect(Collectors.toList()));
        Assertions.assertEquals(Arrays.asList("desc1", "desc2"), returnGoodsList.stream().map(GoodsWithNumber::getDescription).collect(Collectors.toList()));
        Assertions.assertEquals(Arrays.asList("details1", "details2"), returnGoodsList.stream().map(GoodsWithNumber::getDetails).collect(Collectors.toList()));
        Assertions.assertEquals(Arrays.asList("url1", "url2"), returnGoodsList.stream().map(GoodsWithNumber::getImgUrl).collect(Collectors.toList()));
        Assertions.assertEquals(Arrays.asList(3, 1), returnGoodsList.stream().map(GoodsWithNumber::getNumber).collect(Collectors.toList()));
        Assertions.assertEquals(Arrays.asList(new BigDecimal(100), new BigDecimal(100)), returnGoodsList.stream().map(GoodsWithNumber::getPrice).collect(Collectors.toList()));
    }

    /**
     * 订单添加接口失败的数据回滚测试
     *
     * @throws JsonProcessingException
     */
    @Test
    public void canRollBackIfDeductStockFailed() throws JsonProcessingException {
        UserLoginResponse userLoginResponse = loginAndGetCookie();
        OrderInfo orderInfo = new OrderInfo();

        GoodsInfo goodsInfo1 = new GoodsInfo();
        GoodsInfo goodsInfo2 = new GoodsInfo();

        goodsInfo1.setId(1);
        goodsInfo1.setNumber(4);
        goodsInfo2.setId(2);
        goodsInfo2.setNumber(6);
        orderInfo.setGoodsInfos(Arrays.asList(goodsInfo1, goodsInfo2));

        HttpResponse httpResponse = doHttpResponse("/api/order", "POST", orderInfo, userLoginResponse.cookie);
        Response<OrderResponse> orderInResponse = objectMapper.readValue(httpResponse.body, new TypeReference<Response<OrderResponse>>() {
        });
        Assertions.assertEquals(HttpStatus.GONE.value(), httpResponse.code);
        Assertions.assertEquals("扣减库存失败！", orderInResponse.getMassage());

        //确定失败之后的数据库数据回滚
        canCreateOrder();
    }

    /**
     * 同时测试订单的查询和删除接口
     *
     * @throws JsonProcessingException
     */
    @Test
    public void canDeleteOrder() throws JsonProcessingException {
        UserLoginResponse userLoginResponse = loginAndGetCookie();
        QueryBeforeDeletingAnOrder(userLoginResponse);
        deleteOrderTest(userLoginResponse);
        QueryAfterDeletingTheOrder(userLoginResponse);
    }

    /**
     * 删除订单后的查询
     *
     * @param userLoginResponse 登录状态信息
     * @throws JsonProcessingException
     */
    private void QueryAfterDeletingTheOrder(UserLoginResponse userLoginResponse) throws JsonProcessingException {
        when(mockOrderRpcService.orderRpcService.getOrder(anyInt(), anyInt(), any(), anyLong()))
                .thenReturn(PageResponse.of(1, 2, 1,
                        Collections.singletonList(mockRpcOderGoods(101, 1, 4, 2, 3, DataStatus.RECEIVED))));
        PageResponse<OrderResponse> orderInResponse = doHttpResponse("/api/order?pageSize=2&pageNum=1", "GET", null, userLoginResponse.cookie)
                .assertOkStatusCode()
                .asJsonObject(new TypeReference<PageResponse<OrderResponse>>() {
                });
        Assertions.assertEquals(1, orderInResponse.getPageNum());
        Assertions.assertEquals(2, orderInResponse.getPageSize());
        Assertions.assertEquals(1, orderInResponse.getTotalPage());

        List<OrderResponse> returnData = orderInResponse.getData();
        Assertions.assertEquals(Collections.singletonList("顺丰"),
                returnData.stream().map(Order::getExpressCompany).collect(Collectors.toList()));
        Assertions.assertEquals(Collections.singletonList(1L),
                returnData.stream().map(Order::getExpressId).collect(Collectors.toList()));
        Assertions.assertEquals(Collections.singletonList(DataStatus.RECEIVED.getName()),
                returnData.stream().map(Order::getStatus).collect(Collectors.toList()));
        Assertions.assertEquals(Collections.singletonList("xx省xx市xx县xx村xx号"),
                returnData.stream().map(Order::getAddress).collect(Collectors.toList()));

        List<Shop> shopList = returnData.stream().map(OrderResponse::getShop).collect(Collectors.toList());
        Assertions.assertEquals(Collections.singletonList("desc2"),
                shopList.stream().map(Shop::getDescription).collect(Collectors.toList()));
        Assertions.assertEquals(Collections.singletonList("shop2"),
                shopList.stream().map(Shop::getName).collect(Collectors.toList()));
        Assertions.assertEquals(Collections.singletonList("ok"),
                shopList.stream().map(Shop::getStatus).collect(Collectors.toList()));
        Assertions.assertEquals(Collections.singletonList("url2"),
                shopList.stream().map(Shop::getImgUrl).collect(Collectors.toList()));
        Assertions.assertEquals(Collections.singletonList(1L),
                shopList.stream().map(Shop::getOwnerUserId).collect(Collectors.toList()));

        List<List<GoodsWithNumber>> orderGoodsInfoList = returnData.stream()
                .map(OrderResponse::getGoods).collect(Collectors.toList());
        Assertions.assertEquals(Collections.singletonList(Collections.singletonList(4L)),
                orderGoodsInfoList.stream()
                        .map(goodsWithNumbers -> goodsWithNumbers.stream().map(Goods::getId).collect(Collectors.toList()))
                        .collect(Collectors.toList()));
        Assertions.assertEquals(Collections.singletonList(Collections.singletonList("goods4")),
                orderGoodsInfoList.stream()
                        .map(goodsWithNumbers -> goodsWithNumbers.stream().map(Goods::getName).collect(Collectors.toList()))
                        .collect(Collectors.toList()));
        Assertions.assertEquals(Collections.singletonList(Collections.singletonList("desc2")),
                orderGoodsInfoList.stream()
                        .map(goodsWithNumbers -> goodsWithNumbers.stream().map(Goods::getDescription).collect(Collectors.toList()))
                        .collect(Collectors.toList()));
        Assertions.assertEquals(Collections.singletonList(Collections.singletonList("details4")),
                orderGoodsInfoList.stream()
                        .map(goodsWithNumbers -> goodsWithNumbers.stream().map(Goods::getDetails).collect(Collectors.toList()))
                        .collect(Collectors.toList()));
        Assertions.assertEquals(Collections.singletonList(Collections.singletonList("url4")),
                orderGoodsInfoList.stream()
                        .map(goodsWithNumbers -> goodsWithNumbers.stream().map(Goods::getImgUrl).collect(Collectors.toList()))
                        .collect(Collectors.toList()));
        Assertions.assertEquals(Collections.singletonList(Collections.singletonList(new BigDecimal(100))),
                orderGoodsInfoList.stream()
                        .map(goodsWithNumbers -> goodsWithNumbers.stream().map(Goods::getPrice).collect(Collectors.toList()))
                        .collect(Collectors.toList()));
        Assertions.assertEquals(Collections.singletonList(Collections.singletonList(3)),
                orderGoodsInfoList.stream()
                        .map(goodsWithNumbers -> goodsWithNumbers.stream().map(GoodsWithNumber::getNumber).collect(Collectors.toList()))
                        .collect(Collectors.toList()));

    }

    /**
     * 删除订单测试
     *
     * @param userLoginResponse 登录状态信息
     * @throws JsonProcessingException
     */
    private void deleteOrderTest(UserLoginResponse userLoginResponse) throws JsonProcessingException {
        when(mockOrderRpcService.orderRpcService.deleteOrder(100L, 1L))
                .thenReturn(mockRpcOderGoods(100L, 1L, 3L, 2L, 5, DataStatus.DELIVERED));
        Response<OrderResponse> orderInResponse = doHttpResponse("/api/order/100", "DELETE", null, userLoginResponse.cookie)
                .assertOkStatusCode()
                .asJsonObject(new TypeReference<Response<OrderResponse>>() {
                });
        Assertions.assertNull(orderInResponse.getMassage());

        OrderResponse returnData = orderInResponse.getData();
        Assertions.assertEquals(100L, returnData.getId());
        Assertions.assertEquals(1L, returnData.getUserId());
        Assertions.assertEquals("xx省xx市xx县xx村xx号", returnData.getAddress());
        Assertions.assertEquals("顺丰", returnData.getExpressCompany());
        Assertions.assertEquals(1L, returnData.getExpressId());
        Assertions.assertEquals(2L, returnData.getShopId());
        Assertions.assertEquals(DataStatus.DELIVERED.getName(), returnData.getStatus());

        Shop shop = returnData.getShop();
        Assertions.assertEquals("shop2", shop.getName());
        Assertions.assertEquals(1, shop.getOwnerUserId());
        Assertions.assertEquals(2L, shop.getId());
        Assertions.assertEquals("ok", shop.getStatus());
        Assertions.assertEquals("desc2", shop.getDescription());
        Assertions.assertEquals("url2", shop.getImgUrl());

        List<GoodsWithNumber> goodsWithNumbers = returnData.getGoods();
        Assertions.assertEquals(Collections.singletonList(5),
                goodsWithNumbers.stream().map(GoodsWithNumber::getNumber).collect(Collectors.toList()));
        Assertions.assertEquals(Collections.singletonList(3L),
                goodsWithNumbers.stream().map(GoodsWithNumber::getId).collect(Collectors.toList()));
        Assertions.assertEquals(Collections.singletonList(2L),
                goodsWithNumbers.stream().map(GoodsWithNumber::getShopId).collect(Collectors.toList()));
        Assertions.assertEquals(Collections.singletonList(new BigDecimal(100)),
                goodsWithNumbers.stream().map(GoodsWithNumber::getPrice).collect(Collectors.toList()));
        Assertions.assertEquals(Collections.singletonList("desc2"),
                goodsWithNumbers.stream().map(GoodsWithNumber::getDescription).collect(Collectors.toList()));
        Assertions.assertEquals(Collections.singletonList("details3"),
                goodsWithNumbers.stream().map(GoodsWithNumber::getDetails).collect(Collectors.toList()));
        Assertions.assertEquals(Collections.singletonList("url3"),
                goodsWithNumbers.stream().map(GoodsWithNumber::getImgUrl).collect(Collectors.toList()));
        Assertions.assertEquals(Collections.singletonList("goods3"),
                goodsWithNumbers.stream().map(GoodsWithNumber::getName).collect(Collectors.toList()));
        Assertions.assertEquals(Collections.singletonList("ok"),
                goodsWithNumbers.stream().map(GoodsWithNumber::getStatus).collect(Collectors.toList()));
        Assertions.assertEquals(Collections.singletonList(5),
                goodsWithNumbers.stream().map(GoodsWithNumber::getStock).collect(Collectors.toList()));
    }

    /**
     * 删除订单前的查询
     *
     * @param userLoginResponse 登录状态信息
     * @throws JsonProcessingException
     */
    private void QueryBeforeDeletingAnOrder(UserLoginResponse userLoginResponse) throws JsonProcessingException {
        when(mockOrderRpcService.orderRpcService.getOrder(anyInt(), anyInt(), any(), anyLong()))
                .thenReturn(mockResponse());
        PageResponse<OrderResponse> orderInResponse = doHttpResponse("/api/order?pageSize=2&pageNum=1", "GET", null, userLoginResponse.cookie)
                .assertOkStatusCode()
                .asJsonObject(new TypeReference<PageResponse<OrderResponse>>() {
                });
        Assertions.assertEquals(1, orderInResponse.getPageNum());
        Assertions.assertEquals(2, orderInResponse.getPageSize());
        Assertions.assertEquals(1, orderInResponse.getTotalPage());

        List<OrderResponse> returnData = orderInResponse.getData();
        Assertions.assertEquals(Arrays.asList("顺丰", "顺丰"),
                returnData.stream().map(Order::getExpressCompany).collect(Collectors.toList()));
        Assertions.assertEquals(Arrays.asList(1L, 1L),
                returnData.stream().map(Order::getExpressId).collect(Collectors.toList()));
        Assertions.assertEquals(Arrays.asList(DataStatus.DELIVERED.getName(), DataStatus.RECEIVED.getName()),
                returnData.stream().map(Order::getStatus).collect(Collectors.toList()));
        Assertions.assertEquals(Arrays.asList("xx省xx市xx县xx村xx号", "xx省xx市xx县xx村xx号"),
                returnData.stream().map(Order::getAddress).collect(Collectors.toList()));

        List<Shop> shopList = returnData.stream().map(OrderResponse::getShop).collect(Collectors.toList());
        Assertions.assertEquals(Arrays.asList("desc2", "desc2"),
                shopList.stream().map(Shop::getDescription).collect(Collectors.toList()));
        Assertions.assertEquals(Arrays.asList("shop2", "shop2"),
                shopList.stream().map(Shop::getName).collect(Collectors.toList()));
        Assertions.assertEquals(Arrays.asList("ok", "ok"),
                shopList.stream().map(Shop::getStatus).collect(Collectors.toList()));
        Assertions.assertEquals(Arrays.asList("url2", "url2"),
                shopList.stream().map(Shop::getImgUrl).collect(Collectors.toList()));
        Assertions.assertEquals(Arrays.asList(1L, 1L),
                shopList.stream().map(Shop::getOwnerUserId).collect(Collectors.toList()));

        List<List<GoodsWithNumber>> orderGoodsInfoList = returnData.stream()
                .map(OrderResponse::getGoods).collect(Collectors.toList());
        Assertions.assertEquals(Arrays.asList(Collections.singletonList(3L), Collections.singletonList(4L)),
                orderGoodsInfoList.stream()
                        .map(goodsWithNumbers -> goodsWithNumbers.stream().map(Goods::getId).collect(Collectors.toList()))
                        .collect(Collectors.toList()));
        Assertions.assertEquals(Arrays.asList(Collections.singletonList("goods3"), Collections.singletonList("goods4")),
                orderGoodsInfoList.stream()
                        .map(goodsWithNumbers -> goodsWithNumbers.stream().map(Goods::getName).collect(Collectors.toList()))
                        .collect(Collectors.toList()));
        Assertions.assertEquals(Arrays.asList(Collections.singletonList("desc2"), Collections.singletonList("desc2")),
                orderGoodsInfoList.stream()
                        .map(goodsWithNumbers -> goodsWithNumbers.stream().map(Goods::getDescription).collect(Collectors.toList()))
                        .collect(Collectors.toList()));
        Assertions.assertEquals(Arrays.asList(Collections.singletonList("details3"), Collections.singletonList("details4")),
                orderGoodsInfoList.stream()
                        .map(goodsWithNumbers -> goodsWithNumbers.stream().map(Goods::getDetails).collect(Collectors.toList()))
                        .collect(Collectors.toList()));
        Assertions.assertEquals(Arrays.asList(Collections.singletonList("url3"), Collections.singletonList("url4")),
                orderGoodsInfoList.stream()
                        .map(goodsWithNumbers -> goodsWithNumbers.stream().map(Goods::getImgUrl).collect(Collectors.toList()))
                        .collect(Collectors.toList()));
        Assertions.assertEquals(Arrays.asList(Collections.singletonList(new BigDecimal(100)), Collections.singletonList(new BigDecimal(100))),
                orderGoodsInfoList.stream()
                        .map(goodsWithNumbers -> goodsWithNumbers.stream().map(Goods::getPrice).collect(Collectors.toList()))
                        .collect(Collectors.toList()));
        Assertions.assertEquals(Arrays.asList(Collections.singletonList(5), Collections.singletonList(3)),
                orderGoodsInfoList.stream()
                        .map(goodsWithNumbers -> goodsWithNumbers.stream().map(GoodsWithNumber::getNumber).collect(Collectors.toList()))
                        .collect(Collectors.toList()));
    }

    /**
     * 修改订单接口
     * 订单ID参数错误，找不到订单
     * @throws Exception
     */
    @Test
    public void return404IfOrderNotFound() throws Exception {
        UserLoginResponse loginResponse = loginAndGetCookie();

        Order order = new Order();
        order.setId(12345L);
        Assertions.assertEquals(HttpURLConnection.HTTP_NOT_FOUND,
                doHttpResponse("/api/order/1234567", "PATCH", order, loginResponse.cookie).code);
    }

    /**
     * 修改订单接口
     * 修改订单快递信息
     * @throws Exception
     */
    @Test
    public void canUpdateOrderExpressInfomation() throws Exception {
        UserLoginResponse loginResponse = loginAndGetCookie();

        Order orderUpdateRequest = new Order();
        orderUpdateRequest.setId(12345L);
        orderUpdateRequest.setShopId(2L);
        orderUpdateRequest.setExpressCompany("顺丰");
        orderUpdateRequest.setExpressId(12345678L);


        Order orderInDB = new Order();
        orderInDB.setId(12345L);
        orderInDB.setShopId(2L);

        when(mockOrderRpcService.orderRpcService.getOrderByOrderId(12345L)).thenReturn(orderInDB);
        when(mockOrderRpcService.orderRpcService.updateOrder(any())).thenReturn(
                mockRpcOderGoods(12345L, 1L, 3L, 2L, 10, DataStatus.DELIVERED)
        );

        Response<OrderResponse> response = doHttpResponse("/api/order/12345", "PATCH", orderUpdateRequest, loginResponse.cookie)
                .assertOkStatusCode()
                .asJsonObject(new TypeReference<Response<OrderResponse>>() {
                });

        Assertions.assertEquals(2L, response.getData().getShop().getId());
        Assertions.assertEquals("shop2", response.getData().getShop().getName());
        Assertions.assertEquals(DataStatus.DELIVERED.getName(), response.getData().getStatus());
        Assertions.assertEquals(1, response.getData().getGoods().size());
        Assertions.assertEquals(3, response.getData().getGoods().get(0).getId());
        Assertions.assertEquals(10, response.getData().getGoods().get(0).getNumber());
    }

    /**
     * 修改订单接口
     * 修改订单状态
     * @throws Exception
     */
    @Test
    public void canUpdateOrderStatus() throws Exception {
        UserLoginResponse loginResponse = loginAndGetCookie();

        Order orderUpdateRequest = new Order();
        orderUpdateRequest.setId(12345L);
        orderUpdateRequest.setStatus(DataStatus.RECEIVED.getName());


        Order orderInDB = new Order();
        orderInDB.setId(12345L);
        orderInDB.setUserId(1L);
        orderInDB.setShopId(2L);

        when(mockOrderRpcService.orderRpcService.getOrderByOrderId(12345L)).thenReturn(orderInDB);
        when(mockOrderRpcService.orderRpcService.updateOrder(any())).thenReturn(
                mockRpcOderGoods(12345L, 1L, 3L, 2L, 10, DataStatus.RECEIVED)
        );

        Response<OrderResponse> response = doHttpResponse("/api/order/12345", "PATCH", orderUpdateRequest, loginResponse.cookie)
                .assertOkStatusCode()
                .asJsonObject(new TypeReference<Response<OrderResponse>>() {
                });

        Assertions.assertEquals(2L, response.getData().getShop().getId());
        Assertions.assertEquals("shop2", response.getData().getShop().getName());
        Assertions.assertEquals(DataStatus.RECEIVED.getName(), response.getData().getStatus());
        Assertions.assertEquals(1, response.getData().getGoods().size());
        Assertions.assertEquals(3, response.getData().getGoods().get(0).getId());
        Assertions.assertEquals(10, response.getData().getGoods().get(0).getNumber());
    }
    /**
     * @return 假造的数据进行分页组装
     */
    private PageResponse<RpcOrderGoods> mockResponse() {
        RpcOrderGoods order1 = mockRpcOderGoods(100, 1, 3, 2, 5, DataStatus.DELIVERED);
        RpcOrderGoods order2 = mockRpcOderGoods(101, 1, 4, 2, 3, DataStatus.RECEIVED);
        return PageResponse.of(1, 2, 1, Arrays.asList(order1, order2));
    }

    /**
     * @param orderId 订单id
     * @param userId  用户id
     * @param goodsId 商品id
     * @param shopId  店铺id
     * @param number  订单商品数量
     * @param status  订单物流状态
     * @return 假造的数据
     */
    private RpcOrderGoods mockRpcOderGoods(long orderId, long userId, long goodsId, long shopId, int number, DataStatus status) {
        RpcOrderGoods orderGoods = new RpcOrderGoods();
        Order order = new Order();
        GoodsInfo goodsInfo = new GoodsInfo();

        goodsInfo.setId(goodsId);
        goodsInfo.setNumber(number);

        order.setId(orderId);
        order.setUserId(userId);
        order.setShopId(shopId);
        order.setStatus(status.getName());
        order.setExpressCompany("顺丰");
        order.setExpressId(1L);
        order.setAddress("xx省xx市xx县xx村xx号");

        orderGoods.setOrder(order);
        orderGoods.setGoodsInfos(Arrays.asList(goodsInfo));
        return orderGoods;
    }
}
