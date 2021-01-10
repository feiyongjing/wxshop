package com.feiyongjing.wxshop.service;

import com.feiyongjing.wxshop.api.DataStatus;
import com.feiyongjing.wxshop.api.data.PageResponse;
import com.feiyongjing.wxshop.api.exception.HttpException;
import com.feiyongjing.wxshop.controller.ShoppingCartContriller;
import com.feiyongjing.wxshop.dao.ShoppingCartQueryMapper;
import com.feiyongjing.wxshop.entity.*;
import com.feiyongjing.wxshop.generate.*;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class ShoppingCartService {
    private static Logger logger = LoggerFactory.getLogger(ShoppingCartService.class);
    public ShoppingCartQueryMapper shoppingCartQueryMapper;
    private GoodsMapper goodsMapper;
    private GoodsService goodsService;
    private SqlSessionFactory sqlSessionFactory;

    @Autowired
    public ShoppingCartService(ShoppingCartQueryMapper shoppingCartQueryMapper, GoodsMapper goodsMapper, GoodsService goodsService, SqlSessionFactory sqlSessionFactory) {
        this.shoppingCartQueryMapper = shoppingCartQueryMapper;
        this.goodsMapper = goodsMapper;
        this.goodsService = goodsService;
        this.sqlSessionFactory = sqlSessionFactory;
    }


    public PageResponse<ShoppingCartData> getShoppingCart(Integer pageNum, Integer pageSize) {
        Long userId = UserContext.getCurrentUser().getId();
        int totalNum = shoppingCartQueryMapper
                .countHowManyShopsInUserShoppingCart(userId);
        int totalPage = totalNum % pageSize == 0 ? totalNum / pageSize : totalNum / pageSize + 1;
        int limit = (pageNum - 1) * pageSize;
        int offset = pageSize;
        List<ShoppingCartData> pagedData = shoppingCartQueryMapper.getShoppingCart(userId, limit, offset);

        Map<Long, List<ShoppingCartData>> groupShopId = pagedData.stream().collect(
                Collectors.groupingBy(shoppingCartData -> shoppingCartData.getShop().getId())
        );
        List<ShoppingCartData> data = groupShopId.values().stream().map(this::merge).collect(Collectors.toList());
        return PageResponse.of(pageNum, pageSize, totalPage, data);
    }

    public ShoppingCartData merge(List<ShoppingCartData> goodsOfSameShop) {
        if (goodsOfSameShop.isEmpty()) {
            return null;
        }
        ShoppingCartData result = new ShoppingCartData();
        List<GoodsWithNumber> goods = new ArrayList<>();
        result.setShop(goodsOfSameShop.get(0).getShop());
        goodsOfSameShop.stream().map(ShoppingCartData::getGoods).forEach(goods::addAll);
        result.setGoods(goods);
        return result;
    }

    public ShoppingCartData createShoppingCart(ShoppingCartContriller.AddShoppingCartRequest request) {
        List<Long> goodsId = request.getGoods().stream()
                .map(ShoppingCartContriller.AddShoppingCartItem::getId)
                .collect(Collectors.toList());
        if (goodsId.isEmpty()) {
            throw HttpException.badRequest("商品ID为空！");
        }

        Map<Long, Goods> idToGoodsMap = goodsService.getIdToGoodsMap(goodsId);
        if (idToGoodsMap.values().stream().map(Goods::getShopId).collect(Collectors.toSet()).size() != 1) {
            logger.debug("非法请求：{}, {}", goodsId, idToGoodsMap.values());
            throw HttpException.badRequest("商品ID非法！");
        }

        try (SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH)) {
            ShoppingCartMapper shoppingCartMapper = sqlSession.getMapper(ShoppingCartMapper.class);
            request.getGoods().stream()
                    .map(item -> toShoppingCartRow(item, idToGoodsMap))
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList())
                    .forEach(shoppingCartMapper::insertSelective);
            sqlSession.commit();
        }
        return getLatestShoppingCartDateByUserIdShopId(UserContext.getCurrentUser().getId(), new ArrayList<>(idToGoodsMap.values()).get(0).getShopId());
    }

    public ShoppingCartData getLatestShoppingCartDateByUserIdShopId(long userId, long shopId) {
        List<ShoppingCartData> shoppingCartData = shoppingCartQueryMapper.selectShoppingCartDateByUserIdShopId(userId, shopId);
        return merge(shoppingCartData);
    }

    public ShoppingCart toShoppingCartRow(ShoppingCartContriller.AddShoppingCartItem item, Map<Long, Goods> idToGoodsMap) {
        Goods goods = idToGoodsMap.get(item.getId());
        if (goods == null) {
            return null;
        }
        ShoppingCart shoppingCart = new ShoppingCart();

        shoppingCart.setGoodsId(item.getId());
        shoppingCart.setNumber(item.getNumber());
        shoppingCart.setUserId(UserContext.getCurrentUser().getId());
        shoppingCart.setShopId(goods.getShopId());
        shoppingCart.setStatus(DataStatus.OK.getName());
        shoppingCart.setCreatedAt(new Date());
        shoppingCart.setUpdatedAt(new Date());

        return shoppingCart;
    }

    public ShoppingCartData deleteGoodsInShoppingCart(Long goodsId) {
        Goods goods = goodsMapper.selectByPrimaryKey(goodsId);
        if (goods == null) {
            throw HttpException.notFound("商品未找到" + goodsId);
        }
        shoppingCartQueryMapper.deleteGoodsInShoppingCart(UserContext.getCurrentUser().getId(), goodsId);
        return getLatestShoppingCartDateByUserIdShopId(UserContext.getCurrentUser().getId(), goods.getShopId());
    }
}
