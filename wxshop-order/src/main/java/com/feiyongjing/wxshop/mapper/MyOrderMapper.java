package com.feiyongjing.wxshop.mapper;

import com.feiyongjing.wxshop.api.data.GoodsInfo;
import com.feiyongjing.wxshop.api.data.OrderInfo;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface MyOrderMapper {
    void insertOrders(OrderInfo orderInfo);

    List<GoodsInfo> getGoodsInfoOfOrder(long orderId);
}
