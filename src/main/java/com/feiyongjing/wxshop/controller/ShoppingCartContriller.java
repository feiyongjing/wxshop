package com.feiyongjing.wxshop.controller;

import com.feiyongjing.wxshop.entity.*;
import com.feiyongjing.wxshop.service.ShoppingCartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class ShoppingCartContriller {
    private ShoppingCartService shoppingCartService;

    @Autowired
    public ShoppingCartContriller(ShoppingCartService shoppingCartService) {
        this.shoppingCartService = shoppingCartService;
    }

    @PostMapping("/shoppingCart")
    public Response<ShoppingCartData> createShoppingCart(@RequestBody AddShoppingCartRequest goods) {
        try {
            return Response.of(shoppingCartService.createShoppingCart(goods));
        } catch (HttpException e) {
            return Response.of(e.getMessage(), null);
        }
    }

    @GetMapping("/shoppingCart")
    public Response<PageResponse<ShoppingCartData>> getShoppingCart(@RequestParam("pageNum") int pageNum,
                                                                                       @RequestParam("pageSize") int pageSize) {
        try {
            return Response.of(shoppingCartService.getShoppingCart(pageNum, pageSize));
        } catch (HttpException e) {
            return Response.of(e.getMessage(), null);
        }
    }
    @DeleteMapping("/shoppingCart/{goodsId}")
    public Response<ShoppingCartData> deleteGoodsInShoppingCart(@PathVariable("goodsId") Long goodsId) {
        try {
            return Response.of(shoppingCartService.deleteGoodsInShoppingCart(goodsId));
        } catch (HttpException e) {
            return Response.of(e.getMessage(), null);
        }
    }

    public static class AddShoppingCartRequest {
        List<AddShoppingCartItem> goods;

        public List<AddShoppingCartItem> getGoods() {
            return goods;
        }

        public void setGoods(List<AddShoppingCartItem> goods) {
            this.goods = goods;
        }
    }

    public static class AddShoppingCartItem {
        long id;
        int number;

        public long getId() {
            return id;
        }

        public void setId(long id) {
            this.id = id;
        }

        public int getNumber() {
            return number;
        }

        public void setNumber(int number) {
            this.number = number;
        }
    }
}
