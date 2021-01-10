package com.feiyongjing.wxshop.api.rpc;

import com.feiyongjing.wxshop.api.DataStatus;
import com.feiyongjing.wxshop.api.data.OrderInfo;
import com.feiyongjing.wxshop.api.data.PageResponse;
import com.feiyongjing.wxshop.api.data.RpcOrderGoods;
import com.feiyongjing.wxshop.api.generate.Order;

public interface OrderRpcService {
    Order createOrder(OrderInfo orderInfo,Order order);

    RpcOrderGoods deleteOrder(long orderId, long userId);

    PageResponse<RpcOrderGoods> getOrder(int pageNum, int pageSize, DataStatus datastatus, long userId);

    Order getOrderByOrderId(long orderId);

    RpcOrderGoods updateOrder(Order order);
}
