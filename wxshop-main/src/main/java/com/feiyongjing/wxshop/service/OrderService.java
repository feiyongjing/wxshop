package com.feiyongjing.wxshop.service;

import com.feiyongjing.wxshop.api.DataStatus;
import com.feiyongjing.wxshop.api.data.GoodsInfo;
import com.feiyongjing.wxshop.api.data.OrderInfo;
import com.feiyongjing.wxshop.api.data.PageResponse;
import com.feiyongjing.wxshop.api.data.RpcOrderGoods;
import com.feiyongjing.wxshop.api.exception.HttpException;
import com.feiyongjing.wxshop.api.generate.Order;
import com.feiyongjing.wxshop.api.rpc.OrderRpcService;
import com.feiyongjing.wxshop.dao.GoodsStockMapper;
import com.feiyongjing.wxshop.entity.GoodsWithNumber;
import com.feiyongjing.wxshop.entity.OrderResponse;
import com.feiyongjing.wxshop.generate.Goods;
import com.feiyongjing.wxshop.generate.Shop;
import com.feiyongjing.wxshop.generate.UserMapper;
import org.apache.dubbo.config.annotation.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class OrderService {
    private static final Logger LOGGER = LoggerFactory.getLogger(OrderService.class);
    @Reference(version = "${wxshop.orderservice.version}", url = "${wxshop.orderservice.url}")
    private OrderRpcService orderRpcService;

    private UserMapper userMapper;
    private GoodsStockMapper goodsStockMapper;
    private GoodsService goodsService;
    private ShopService shopService;

    @Autowired
    public OrderService(UserMapper userMapper, GoodsStockMapper goodsStockMapper, GoodsService goodsService, ShopService shopService) {
        this.userMapper = userMapper;
        this.goodsStockMapper = goodsStockMapper;
        this.goodsService = goodsService;
        this.shopService = shopService;
    }

    public OrderResponse createOrder(OrderInfo orderInfo, long userId) {
        Map<Long, Goods> idToGoodsMap = getIdToGoodsMap(orderInfo.getGoodsInfos());
        Order createOrder = createOrderViaRpc(orderInfo, userId, idToGoodsMap);
        return generateResponse(orderInfo.getGoodsInfos(), idToGoodsMap, createOrder);
    }

    @Transactional
    public void deductStock(OrderInfo orderInfo) throws HttpException {
        for (GoodsInfo goodsInfo : orderInfo.getGoodsInfos()) {
            if (goodsStockMapper.deductStock(goodsInfo) <= 0) {
                LOGGER.error("扣减库存失败, 商品id: " + goodsInfo.getId() + "，数量：" + goodsInfo.getNumber());
                throw HttpException.gone("扣减库存失败！");
            }
        }
    }

    private OrderResponse generateResponse(List<GoodsInfo> goodsInfos, Map<Long, Goods> idToGoodsMap, Order createOrder) {
        OrderResponse orderResponse = new OrderResponse(createOrder);
        orderResponse.setShop(shopService.getShopById(new ArrayList<>(idToGoodsMap.values()).get(0).getShopId()));
        orderResponse.setGoods(getGoodsWithNumber(goodsInfos, idToGoodsMap));
        return orderResponse;
    }

    private Order createOrderViaRpc(OrderInfo orderInfo, long userId, Map<Long, Goods> idToGoodsMap) {
        Order order = new Order();
        order.setUserId(userId);
        order.setStatus(DataStatus.PENDING.getName());
        order.setAddress(userMapper.selectByPrimaryKey(userId).getAddress());
        order.setTotalPrice(calculateTotalPrice(orderInfo, idToGoodsMap));

        return orderRpcService.createOrder(orderInfo, order);
    }

    private Map<Long, Goods> getIdToGoodsMap(List<GoodsInfo> goodsInfos) {
        List<Long> goodsId = goodsInfos
                .stream()
                .map(GoodsInfo::getId)
                .collect(Collectors.toList());
        return goodsService.getIdToGoodsMap(goodsId);
    }

    private List<GoodsWithNumber> getGoodsWithNumber(List<GoodsInfo> goodsInfos, Map<Long, Goods> idToGoodsMap) {
        List<GoodsWithNumber> result = new ArrayList<>();
        goodsInfos.forEach(goodsInfo -> toGoodsWithNumber(goodsInfo, idToGoodsMap, result));
        return result;
    }

    private void toGoodsWithNumber(GoodsInfo goodsInfo, Map<Long, Goods> idToGoodsMap, List<GoodsWithNumber> result) {
        Goods goods = idToGoodsMap.get(goodsInfo.getId());
        GoodsWithNumber goodsWithNumber = new GoodsWithNumber(goods);
        goodsWithNumber.setNumber(goodsInfo.getNumber());
        result.add(goodsWithNumber);
    }

    private BigDecimal calculateTotalPrice(OrderInfo orderInfo, Map<Long, Goods> idToGoodsMap) {
        BigDecimal result = BigDecimal.ZERO;
        for (GoodsInfo goodsInfo : orderInfo.getGoodsInfos()) {
            Goods goods = idToGoodsMap.get(goodsInfo.getId());
            if (goods == null) {
                throw HttpException.badRequest("goods id非法：" + goodsInfo.getId());
            }
            if (goodsInfo.getNumber() <= 0) {
                throw HttpException.badRequest("number非法：" + goodsInfo.getNumber());
            }
            result = result.add(goods.getPrice().multiply(new BigDecimal(goodsInfo.getNumber())));
        }
        return result;
    }

    public OrderResponse deleteOrder(long orderId, long userId) {
        RpcOrderGoods rpcOrderGoods = orderRpcService.deleteOrder(orderId, userId);
        return getOrderResponse(rpcOrderGoods);
    }

    public PageResponse<OrderResponse> getOrder(int pageNum, int pageSize, DataStatus dataStatus, long userId) {
        PageResponse<RpcOrderGoods> pageRpcOrderGoods = orderRpcService.getOrder(pageNum, pageSize, dataStatus, userId);
        List<OrderResponse> data = new ArrayList<>();
        for (RpcOrderGoods rpcOrderGoods : pageRpcOrderGoods.getData()) {
//            Map<Long, Goods> idToGoodsMap = getIdToGoodsMap(rpcOrderGoods.getGoodsInfos());
//            OrderResponse orderResponse = generateResponse(rpcOrderGoods.getGoodsInfos(), idToGoodsMap, rpcOrderGoods.getOrder());
            data.add(getOrderResponse(rpcOrderGoods));
        }
        return PageResponse.of(pageRpcOrderGoods.getPageNum(),
                pageRpcOrderGoods.getPageSize(),
                pageRpcOrderGoods.getTotalPage(),
                data);
    }

    public OrderResponse updateExpressInformation(long orderId, Order order, Long userId) {
        Order currentOrder = orderRpcService.getOrderByOrderId(orderId);
        if (currentOrder == null) {
            throw HttpException.notFound("订单未找到：" + orderId);
        }

        Shop shop = shopService.getShopById(currentOrder.getShopId());
        if (shop == null) {
            throw HttpException.notFound("店铺未找到：" + currentOrder.getShopId());
        }
        if (!Objects.equals(shop.getOwnerUserId(), userId)) {
            throw HttpException.forbidden("无权访问！");
        }
        Order copy = new Order();
        copy.setId(orderId);
        copy.setExpressId(order.getExpressId());
        copy.setExpressCompany(order.getExpressCompany());

        RpcOrderGoods rpcOrderGoods = orderRpcService.updateOrder(copy);
        return getOrderResponse(rpcOrderGoods);
    }

    private OrderResponse getOrderResponse(RpcOrderGoods rpcOrderGoods) {
        Map<Long, Goods> idToGoodsMap = getIdToGoodsMap(rpcOrderGoods.getGoodsInfos());
        return generateResponse(rpcOrderGoods.getGoodsInfos(), idToGoodsMap, rpcOrderGoods.getOrder());
    }

    public OrderResponse updateOrderStatus(long orderId, Order order, Long userId) {
        Order currentOrder = orderRpcService.getOrderByOrderId(orderId);
        if (currentOrder == null) {
            throw HttpException.notFound("订单未找到：" + orderId);
        }
        if (!Objects.equals(currentOrder.getUserId(), userId)) {
            throw HttpException.forbidden("无权访问！");
        }
        Order copy = new Order();
        copy.setId(orderId);
        copy.setStatus(order.getStatus());

        RpcOrderGoods rpcOrderGoods = orderRpcService.updateOrder(copy);
        return getOrderResponse(rpcOrderGoods);
    }
}
