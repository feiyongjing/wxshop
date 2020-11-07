package com.feiyongjing.wxshop.controller;

import com.feiyongjing.wxshop.entity.HttpException;
import com.feiyongjing.wxshop.entity.PageResponse;
import com.feiyongjing.wxshop.entity.Response;
import com.feiyongjing.wxshop.generate.Goods;
import com.feiyongjing.wxshop.service.GoodsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.Date;

import static javax.servlet.http.HttpServletResponse.*;

@RestController
@RequestMapping("/api")
public class GoodsController {
    private GoodsService goodsService;

    @Autowired
    public GoodsController(GoodsService goodsService) {
        this.goodsService = goodsService;
    }

    @PostMapping("/goods")
    public Response<Goods> createGoods(@RequestBody Goods goods, HttpServletResponse response) {
        clean(goods);
        try {
            response.setStatus(SC_CREATED);
            return Response.of(goodsService.create(goods));
        } catch (HttpException e) {
            response.setStatus(e.getStatusCode());
            return Response.of(e.getMessage(), null);
        }
    }

    @DeleteMapping("/goods/{id}")
    public Response<Goods> deleteGoods(@PathVariable("id") Long goodsId, HttpServletResponse response) {
        try {
            response.setStatus(SC_NO_CONTENT);
            return Response.of(goodsService.deleteGoodsById(goodsId));
        } catch (HttpException e) {
            response.setStatus(e.getStatusCode());
            return Response.of(e.getMessage(), null);
        }
    }

    //    @PatchMapping("/goods/{id}")
//    @PostMapping("/goods/{id}")
    @RequestMapping(value = "/goods/{id}", method = {RequestMethod.POST, RequestMethod.PATCH})
    public Response<Goods> updateGoods(@PathVariable("id") Long goodsId, @RequestBody Goods goods, HttpServletResponse response) {
        clean(goods);
        try {
            response.setStatus(SC_OK);
            return Response.of(goodsService.updateGoodsById(goodsId, goods));
        } catch (HttpException e) {
            response.setStatus(e.getStatusCode());
            return Response.of(e.getMessage(), null);
        }
    }

    @GetMapping("/goods")
    @ResponseBody
    public Response<PageResponse<Goods>> getGoods(@RequestParam("pageNum") Integer pageNum,
                                        @RequestParam("pageSize") Integer pageSize,
                                        @RequestParam(value = "shopId", required = false) Long shopId) {
        try {
            return Response.of(goodsService.getGoods(pageNum, pageSize, shopId));
        } catch (HttpException e) {
            return Response.of(e.getMessage(), null);
        }
    }

    @GetMapping("/goods/{id}")
    @ResponseBody
    public Response<Goods> getGoodsById(@PathVariable("id") Long id, HttpServletResponse response) {
        try {
            return Response.of(goodsService.getGoodsById(id));
        } catch (HttpException e) {
            response.setStatus(e.getStatusCode());
            return Response.of(e.getMessage(), null);
        }
    }

    private void clean(Goods goods) {
        goods.setId(null);
        goods.setCreatedAt(new Date());
        goods.setUpdatedAt(new Date());
    }
}
