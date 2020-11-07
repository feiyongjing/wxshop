package com.feiyongjing.wxshop.service;

import com.feiyongjing.wxshop.entity.DataStatus;
import com.feiyongjing.wxshop.entity.HttpException;
import com.feiyongjing.wxshop.entity.PageResponse;
import com.feiyongjing.wxshop.generate.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import static java.util.stream.Collectors.toMap;

@Service
public class GoodsService {
    private GoodsMapper goodsMapper;
    private ShopMapper shopMapper;

    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    @Autowired
    public GoodsService(GoodsMapper goodsMapper, ShopMapper shopMapper) {
        this.goodsMapper = goodsMapper;
        this.shopMapper = shopMapper;
    }

    public Map<Long, Goods> getIdToGoodsMap(List<Long> goodsId) {
        GoodsExample example = new GoodsExample();
        example.createCriteria().andIdIn(goodsId);
        List<Goods> goods = goodsMapper.selectByExample(example);
        return goods.stream().collect(toMap(Goods::getId, x -> x));
    }

    public Goods create(Goods goods) {
        Shop shop = shopMapper.selectByPrimaryKey(goods.getShopId());
        if (Objects.equals(shop.getOwnerUserId(), UserContext.getCurrentUser().getId())) {
            goods.setStatus(DataStatus.OK.getName());
            long goodsId = goodsMapper.insertSelective(goods);
//            goods.setId(goodsId);
            return goods;
        } else {
            throw HttpException.forbidden("无法创建非自己管理店铺的商品");
        }
    }

    public Goods deleteGoodsById(Long goodsId) {
        Goods goods = goodsMapper.selectByPrimaryKey(goodsId);
        if (goods == null) {
            throw HttpException.notFound("商品未找到!");
        }
        Shop shop = shopMapper.selectByPrimaryKey(goods.getShopId());
        if (Objects.equals(shop.getOwnerUserId(), UserContext.getCurrentUser().getId())) {
            goods.setStatus(DataStatus.DELETE.getName());
            goodsMapper.updateByPrimaryKey(goods);
            return goodsMapper.selectByPrimaryKey(goodsId);
        } else {
            throw HttpException.forbidden("无法创建非自己管理店铺的商品");
        }

    }

    public Goods updateGoodsById(Long goodsId, Goods goods) {
        Goods goods1 = goodsMapper.selectByPrimaryKey(goodsId);
        if (goods1 == null) {
            throw HttpException.notFound("商品未找到!");
        }
        Shop shop = shopMapper.selectByPrimaryKey(goods.getShopId());
        if (Objects.equals(shop.getOwnerUserId(), UserContext.getCurrentUser().getId())) {
            goods.setId(goodsId);
            goods.setStatus(DataStatus.OK.getName());
            goodsMapper.updateByPrimaryKey(goods);
            return goodsMapper.selectByPrimaryKey(goodsId);
        } else {
            throw HttpException.forbidden("无法创建非自己管理店铺的商品");
        }
    }

    public PageResponse<Goods> getGoods(Integer pageNum, Integer pageSize, Long shopId) {
        int totalNumber = countGoods(shopId);
        int totalPage = totalNumber % pageSize == 0 ? totalNumber / pageSize : totalNumber / pageSize + 1;
        GoodsExample goodsExample = new GoodsExample();
        goodsExample.setLimit(pageSize);
        goodsExample.setOffset((pageNum - 1) * pageSize);
        List<Goods> goodsList = goodsMapper.selectByExample(goodsExample);
        return PageResponse.of(pageNum, pageSize, totalPage, goodsList);
    }

    private int countGoods(Long shopId) {
        if (shopId == null) {
            GoodsExample goodsExample = new GoodsExample();
            goodsExample.createCriteria().andStatusEqualTo(DataStatus.OK.getName());
            return (int) goodsMapper.countByExample(goodsExample);
        } else {
            GoodsExample goodsExample = new GoodsExample();
            goodsExample.createCriteria()
                    .andStatusEqualTo(DataStatus.OK.getName())
                    .andShopIdEqualTo(shopId);
            return (int) goodsMapper.countByExample(goodsExample);
        }
    }

    public Goods getGoodsById(Long id) {
        Goods goods = goodsMapper.selectByPrimaryKey(id);
        if (goods == null) {
            throw HttpException.notFound("商品未找到!");
        } else {
            return goods;
        }
    }
}
