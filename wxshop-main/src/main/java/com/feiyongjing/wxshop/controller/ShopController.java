package com.feiyongjing.wxshop.controller;

import com.feiyongjing.wxshop.api.data.PageResponse;
import com.feiyongjing.wxshop.entity.Response;
import com.feiyongjing.wxshop.generate.Shop;
import com.feiyongjing.wxshop.service.ShopService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.Date;

import static javax.servlet.http.HttpServletResponse.SC_CREATED;
import static javax.servlet.http.HttpServletResponse.SC_NO_CONTENT;

@RestController
@RequestMapping("/api/v1")
public class ShopController {
    private ShopService shopService;

    @Autowired
    public ShopController(ShopService shopService) {
        this.shopService = shopService;
    }

    /**
     * 修改店铺
     * @param id 指定店铺ID
     * @param shop 店铺信息
     * @return {}
     */
    @RequestMapping(value = "/shop/{id}", method = {RequestMethod.POST, RequestMethod.PATCH})
    public Response<Shop> updateShop(@PathVariable("id") Long id, @RequestBody Shop shop) {
        clean(shop);
        return Response.of(shopService.updateShop(id, shop));

    }

    /**
     * 添加店铺
     * @param shop 店铺信息
     * @param response
     * @return {}
     */
    @PostMapping("/shop")
    public Response<Shop> createShop(@RequestBody Shop shop, HttpServletResponse response) {
        clean(shop);
        response.setStatus(SC_CREATED);
        return Response.of(shopService.createShop(shop));
    }

    /**
     * 删除店铺
     * @param id 店铺ID
     * @param response
     * @return {}
     */
    @DeleteMapping("/shop/{id}")
    public Response<Shop> deleteShop(@PathVariable("id") Long id, HttpServletResponse response) {
        response.setStatus(SC_NO_CONTENT);
        return Response.of(shopService.deleteShop(id));
    }

    /**
     * 分页查询店铺
     * @param pageNum 页码数
     * @param pageSize 页码大小
     * @return {}
     */
    @GetMapping("/shop")
    private PageResponse<Shop> getShop(@RequestParam("pageNum") Integer pageNum,
                                       @RequestParam("pageSize") Integer pageSize) {
        return shopService.getShop(pageNum, pageSize);
    }

    /**
     * 查询指定店铺
     * @param id 指定店铺ID
     * @return {}
     */
    @GetMapping("/shop/{id}")
    private Response<Shop> getShopById(@PathVariable("id") Long id) {
        return Response.of(shopService.getShopById(id));
    }

    private void clean(Shop shop) {
        shop.setId(null);
        shop.setCreatedAt(new Date());
        shop.setUpdatedAt(new Date());
    }
}
