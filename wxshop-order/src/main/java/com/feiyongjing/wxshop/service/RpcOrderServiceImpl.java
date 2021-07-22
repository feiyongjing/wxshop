package com.feiyongjing.wxshop.service;

import com.feiyongjing.wxshop.api.DataStatus;
import com.feiyongjing.wxshop.api.data.GoodsInfo;
import com.feiyongjing.wxshop.api.data.OrderInfo;
import com.feiyongjing.wxshop.api.data.PageResponse;
import com.feiyongjing.wxshop.api.data.RpcOrderGoods;
import com.feiyongjing.wxshop.api.exception.HttpException;
import com.feiyongjing.wxshop.api.generate.*;
import com.feiyongjing.wxshop.api.rpc.OrderRpcService;
import com.feiyongjing.wxshop.mapper.MyOrderMapper;
import org.apache.dubbo.config.annotation.Service;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

@Service(version = "${wxshop.orderservice.version}")
public class RpcOrderServiceImpl implements OrderRpcService {

    private OrderMapper orderMapper;

    private MyOrderMapper myOrderMapper;

    private OrderGoodsMapper orderGoodsMapper;
    @Autowired
    public RpcOrderServiceImpl(OrderMapper orderMapper, MyOrderMapper myOrderMapper, OrderGoodsMapper orderGoodsMapper) {
        this.orderMapper = orderMapper;
        this.myOrderMapper = myOrderMapper;
        this.orderGoodsMapper = orderGoodsMapper;
    }

    @Override
    public Order createOrder(OrderInfo orderInfo, Order order) {
        insertOrder(order);
        orderInfo.setOrderId(order.getId());
        myOrderMapper.insertOrders(orderInfo);
        return order;
    }

    private void insertOrder(Order order) {
        order.setStatus(DataStatus.PENDING.getName());

        if (order.getUserId() == null) {
            throw new IllegalArgumentException("UserId不能是空");
        }
        if (order.getTotalPrice() == null || order.getTotalPrice().doubleValue() < 0) {
            throw new IllegalArgumentException("TotalPrice非法");
        }
        if (order.getAddress() == null) {
            throw new IllegalArgumentException("Address不能是空");
        }

        order.setExpressId(null);
        order.setExpressCompany(null);
        order.setCreatedAt(new Date());
        order.setUpdatedAt(new Date());
        orderMapper.insertSelective(order);
    }

    @Override
    public RpcOrderGoods deleteOrder(long orderId, long userId) {
        Order order = orderMapper.selectByPrimaryKey(orderId);
        if (order == null) {
            throw HttpException.notFound("订单未找到");
        }
        if (!Objects.equals(order.getUserId(), userId)) {
            throw HttpException.forbidden("无权删除订单");
        }
        List<GoodsInfo> goodsInfos = myOrderMapper.getGoodsInfoOfOrder(orderId);

        order.setStatus(DataStatus.DELETED.getName());
        order.setUpdatedAt(new Date());
        orderMapper.updateByPrimaryKeySelective(order);

        return getRpcOrderGoods(order, goodsInfos);
    }

    private RpcOrderGoods getRpcOrderGoods(Order order, List<GoodsInfo> goodsInfos) {
        RpcOrderGoods rpcOrderGoods = new RpcOrderGoods();
        rpcOrderGoods.setOrder(order);
        rpcOrderGoods.setGoodsInfos(goodsInfos);
        return rpcOrderGoods;
    }

    @Override
    public PageResponse<RpcOrderGoods> getOrder(int pageNum, int pageSize, DataStatus datastatus, long userId) {
        OrderExample orderExample = new OrderExample();
        orderExample.createCriteria().andUserIdEqualTo(userId);
        if (datastatus == null) {
            orderExample.createCriteria().andStatusEqualTo(DataStatus.DELETED.getName());
        } else {
            orderExample.createCriteria().andStatusEqualTo(datastatus.getName());
        }
        int count = (int) orderMapper.countByExample(orderExample);
        int totalPage = count % pageSize == 0 ? count / pageSize : count / pageSize + 1;
        orderExample.setLimit(pageSize);
        orderExample.setOffset((pageNum - 1) * pageSize);
        List<Order> orderList = orderMapper.selectByExample(orderExample);

        List<RpcOrderGoods> result = new ArrayList<>();
        for (Order order : orderList) {
            List<GoodsInfo> goodsInfos = myOrderMapper.getGoodsInfoOfOrder(order.getId());
            RpcOrderGoods rpcOrderGoods = getRpcOrderGoods(order, goodsInfos);
            result.add(rpcOrderGoods);
        }
        return PageResponse.of(pageNum, pageSize, totalPage, result);
    }

    @Override
    public RpcOrderGoods getOrderById(long orderId) {
        Order currentOrder = orderMapper.selectByPrimaryKey(orderId);
        if (currentOrder == null) {
            throw HttpException.notFound("订单未找到：" + orderId);
        }
        List<GoodsInfo> goodsInfos = myOrderMapper.getGoodsInfoOfOrder(orderId);
        return getRpcOrderGoods(currentOrder, goodsInfos);
    }

    @Override
    public RpcOrderGoods updateOrder(Order order) {
        order.setUpdatedAt(new Date());
        orderMapper.updateByPrimaryKeySelective(order);
        List<GoodsInfo> goodsInfos = myOrderMapper.getGoodsInfoOfOrder(order.getId());

        return getRpcOrderGoods(order, goodsInfos);
    }


}
