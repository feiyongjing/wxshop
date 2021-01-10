package com.feiyongjing.wxshop.api.data;

import com.feiyongjing.wxshop.api.generate.Order;

import java.io.Serializable;
import java.util.List;

public class RpcOrderGoods implements Serializable {
    private Order order;
    private List<GoodsInfo> goodsInfos;

    public Order getOrder() {
        return order;
    }

    public void setOrder(Order order) {
        this.order = order;
    }

    public List<GoodsInfo> getGoodsInfos() {
        return goodsInfos;
    }

    public void setGoodsInfos(List<GoodsInfo> goodsInfos) {
        this.goodsInfos = goodsInfos;
    }
}
