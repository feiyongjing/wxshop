package com.feiyongjing.wxshop.controller;

import com.feiyongjing.wxshop.api.data.PageResponse;
import com.feiyongjing.wxshop.entity.Response;
import com.feiyongjing.wxshop.generate.Goods;
import com.feiyongjing.wxshop.service.GoodsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.Date;

import static javax.servlet.http.HttpServletResponse.*;

@RestController
@RequestMapping("/api/v1")
public class GoodsController {
    private GoodsService goodsService;

    @Autowired
    public GoodsController(GoodsService goodsService) {
        this.goodsService = goodsService;
    }

    /**
     * 添加商品
     * @param goods
     * @param response
     * @return {}
     */
    @PostMapping("/goods")
    public Response<Goods> createGoods(@RequestBody Goods goods, HttpServletResponse response) {
        clean(goods);
        response.setStatus(SC_CREATED);
        return Response.of(goodsService.create(goods));
    }

    /**
     * 删除商品
     * @param goodsId
     * @param response
     * @return {}
     */
    @DeleteMapping("/goods/{id}")
    public Response<Goods> deleteGoods(@PathVariable("id") Long goodsId, HttpServletResponse response) {
        response.setStatus(SC_NO_CONTENT);
        return Response.of(goodsService.deleteGoodsById(goodsId));

    }

    /**
     * 修改商品
     *
     * @param goodsId
     * @param goods
     * @param response
     * @return {}
     */
    @RequestMapping(value = "/goods/{id}", method = {RequestMethod.POST, RequestMethod.PATCH})
    public Response<Goods> updateGoods(@PathVariable("id") Long goodsId, @RequestBody Goods goods, HttpServletResponse response) {
        clean(goods);
        response.setStatus(SC_OK);
        return Response.of(goodsService.updateGoodsById(goodsId, goods));

    }

    /**
     * 分页查询商品
     * @param pageNum
     * @param pageSize
     * @param shopId
     * @return {}
     */
    @GetMapping("/goods")
    @ResponseBody
    public PageResponse<Goods> getGoods(@RequestParam("pageNum") Integer pageNum,
                                                  @RequestParam("pageSize") Integer pageSize,
                                                  @RequestParam(value = "shopId", required = false) Long shopId) {
        return goodsService.getGoods(pageNum, pageSize, shopId);
    }

    /**
     * 获取商品
     * @param id
     * @return {}
     */
    @GetMapping("/goods/{id}")
    @ResponseBody
    public Response<Goods> getGoodsById(@PathVariable("id") Long id) {
        return Response.of(goodsService.getGoodsById(id));
    }

    private void clean(Goods goods) {
        goods.setId(null);
        goods.setCreatedAt(new Date());
        goods.setUpdatedAt(new Date());
    }
}
