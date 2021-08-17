package com.feiyongjing.wxshop.controller;

import com.feiyongjing.wxshop.api.data.PageResponse;
import com.feiyongjing.wxshop.entity.*;
import com.feiyongjing.wxshop.service.ShoppingCartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
public class ShoppingCartContriller {
    private ShoppingCartService shoppingCartService;

    @Autowired
    public ShoppingCartContriller(ShoppingCartService shoppingCartService) {
        this.shoppingCartService = shoppingCartService;
    }

    /**
     * 购物车添加商品
     * @param goods 添加购物车商品信息
     * @return {}
     */
    @PostMapping("/shoppingCart")
    public Response<ShoppingCartData> createShoppingCart(@RequestBody AddShoppingCartRequest goods) {
        return Response.of(shoppingCartService.createShoppingCart(goods));
    }

    /**
     * 分页获取购物车商品信息
     * @param pageNum 页码数
     * @param pageSize 页大小
     * @return {}
     */
    @GetMapping("/shoppingCart")
    public Response<PageResponse<ShoppingCartData>> getShoppingCart(@RequestParam("pageNum") int pageNum,
                                                                    @RequestParam("pageSize") int pageSize) {
        return Response.of(shoppingCartService.getShoppingCart(pageNum, pageSize));
    }

    /**
     * 清除购物车商品信息
     * @param goodsId 商品id
     * @return {}
     */
    @DeleteMapping("/shoppingCart/{goodsId}")
    public Response<ShoppingCartData> deleteGoodsInShoppingCart(@PathVariable("goodsId") Long goodsId) {
        return Response.of(shoppingCartService.deleteGoodsInShoppingCart(goodsId));
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
