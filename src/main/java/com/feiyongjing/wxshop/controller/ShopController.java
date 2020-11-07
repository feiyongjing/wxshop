package com.feiyongjing.wxshop.controller;

import com.feiyongjing.wxshop.entity.HttpException;
import com.feiyongjing.wxshop.entity.PageResponse;
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
@RequestMapping("/api")
public class ShopController {
    private ShopService shopService;

    @Autowired
    public ShopController(ShopService shopService) {
        this.shopService = shopService;
    }

//    @PatchMapping("/shop/{id}")
    @RequestMapping(value = "/shop/{id}", method = {RequestMethod.POST, RequestMethod.PATCH})
    public Response<Shop> updateShop(@PathVariable("id") Long id, @RequestBody Shop shop, HttpServletResponse response) {
        clean(shop);
        try {
            return Response.of(shopService.updateShop(id, shop));
        } catch (HttpException e) {
            response.setStatus(e.getStatusCode());
            return Response.of(e.getMessage(), null);
        }
    }

    @PostMapping("/shop")
    public Response<Shop> createShop(@RequestBody Shop shop, HttpServletResponse response) {
        clean(shop);
        response.setStatus(SC_CREATED);
        return Response.of(shopService.createShop(shop));
    }

    @DeleteMapping("/shop/{id}")
    public Response<Shop> deleteShop(@PathVariable("id") Long id, HttpServletResponse response) {
        try {
            response.setStatus(SC_NO_CONTENT);
            return Response.of(shopService.deleteShop(id));
        } catch (HttpException e) {
            response.setStatus(e.getStatusCode());
            return Response.of(e.getMessage(), null);
        }
    }

    @GetMapping("/shop")
    private PageResponse<Shop> getShop(@RequestParam("pageNum") Integer pageNum,
                                       @RequestParam("pageSize") Integer pageSize) {
        return shopService.getShop(pageNum, pageSize);
    }

    @GetMapping("/shop/{id}")
    private Response<Shop> getShopById(@PathVariable("id") Long id, HttpServletResponse response) {
        try {
            return Response.of(shopService.getShopById(id));
        } catch (HttpException e) {
            response.setStatus(e.getStatusCode());
            return Response.of(e.getMessage(), null);
        }
    }

    private void clean(Shop shop) {
        shop.setId(null);
        shop.setCreatedAt(new Date());
        shop.setUpdatedAt(new Date());
    }
}
