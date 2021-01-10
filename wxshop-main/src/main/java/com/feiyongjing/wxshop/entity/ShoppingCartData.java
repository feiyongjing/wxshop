package com.feiyongjing.wxshop.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.feiyongjing.wxshop.generate.Shop;

import java.util.List;

public class ShoppingCartData {
    @JsonProperty("shop")
    Shop shop;
    @JsonProperty("goods")
    List<GoodsWithNumber> goods;

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
