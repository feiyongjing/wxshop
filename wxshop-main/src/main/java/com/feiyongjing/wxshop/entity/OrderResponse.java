package com.feiyongjing.wxshop.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.feiyongjing.wxshop.api.generate.Order;
import com.feiyongjing.wxshop.generate.Shop;

import java.util.List;

public class OrderResponse extends Order {
    @JsonProperty("shop")
    Shop shop;
    @JsonProperty("goods")
    List<GoodsWithNumber> goods;
    public OrderResponse() {

    }

    public OrderResponse(Order order) {
        this.setId(order.getId());
        this.setUserId(order.getUserId());
        this.setTotalPrice(order.getTotalPrice());
        this.setAddress(order.getAddress());
        this.setExpressCompany(order.getExpressCompany());
        this.setExpressId(order.getExpressId());
        this.setStatus(order.getStatus());
        this.setCreatedAt(order.getCreatedAt());
        this.setUpdatedAt(order.getUpdatedAt());
        this.setShopId(order.getShopId());
    }
    public Shop getShop() {
        return shop;
    }

    public void setShop(Shop shop) {
        this.shop = shop;
    }

    public List<GoodsWithNumber> getGoods() {
        return goods;
    }

    public void setGoods(List<GoodsWithNumber> goods) {
        this.goods = goods;
    }
}
