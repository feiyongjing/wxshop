package com.feiyongjing.wxshop.entity;

import com.feiyongjing.wxshop.generate.Goods;

public class ShoppingCartGoods extends Goods {
    int number;

    public ShoppingCartGoods() {
    }

    public ShoppingCartGoods(Goods goods) {
        this.setId(goods.getId());
        this.setShopId(goods.getShopId());
        this.setName(goods.getName());
        this.setDescription(goods.getDescription());
        this.setImgUrl(goods.getImgUrl());
        this.setPrice(goods.getPrice());
        this.setStock(goods.getStock());
        this.setStatus(goods.getStatus());
        this.setCreatedAt(goods.getCreatedAt());
        this.setUpdatedAt(goods.getUpdatedAt());
        this.setDetails(goods.getDetails());

    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }
}
