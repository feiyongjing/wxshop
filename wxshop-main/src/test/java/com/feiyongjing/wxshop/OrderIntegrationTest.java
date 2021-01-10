package com.feiyongjing.wxshop;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.feiyongjing.wxshop.api.DataStatus;
import com.feiyongjing.wxshop.api.data.GoodsInfo;
import com.feiyongjing.wxshop.api.data.OrderInfo;
import com.feiyongjing.wxshop.api.generate.Order;
import com.feiyongjing.wxshop.entity.GoodsWithNumber;
import com.feiyongjing.wxshop.entity.OrderResponse;
import com.feiyongjing.wxshop.entity.Response;
import com.feiyongjing.wxshop.generate.Shop;
import com.feiyongjing.wxshop.mock.MockOrderRpcService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.mockito.ArgumentMatchers.any;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = WxshopApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = {"spring.config.location=classpath:test-application.yml"})
public class OrderIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    MockOrderRpcService mockOrderRpcService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(mockOrderRpcService);

        Mockito.when(mockOrderRpcService.orderRpcService.createOrder(any(), any())).thenAnswer(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                Order order=invocation.getArgument(1);
                order.setId(1234L);
                return order;
            }
        });
    }

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

        HttpResponse httpResponse = doHttpResponse("/api/order", "POST", orderInfo, userLoginResponse.cookie);
        Response<OrderResponse> orderInResponse = objectMapper.readValue(httpResponse.body, new TypeReference<Response<OrderResponse>>() {
        });

        Shop returnShop = orderInResponse.getData().getShop();
        List<GoodsWithNumber> returnGoodsList = orderInResponse.getData().getGoods();

        Assertions.assertEquals(200, httpResponse.code);
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
    @Test
    public void canDeleteOrder() throws JsonProcessingException {
        UserLoginResponse userLoginResponse = loginAndGetCookie();

        HttpResponse httpResponse = doHttpResponse("/api/order/2", "DELETE", null, userLoginResponse.cookie);
        Response<OrderResponse> orderInResponse = objectMapper.readValue(httpResponse.body, new TypeReference<Response<OrderResponse>>() {
        });
    }
}
