package com.feiyongjing.wxshop.dao;

import com.feiyongjing.wxshop.entity.ShoppingCartData;
import com.feiyongjing.wxshop.entity.ShoppingCartGoods;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ShoppingCartQueryMapper {
    int countHowManyShopsInUserShoppingCart(long userId);
    List<ShoppingCartData> getShoppingCart(@Param("userId") long userId,
                                                              @Param("limit") int limit,
                                                              @Param("offset") int offset);

    List<ShoppingCartData> selectShoppingCartDateByUserIdShopId(@Param("userId")Long userId,
                                                                @Param("shopId")Long shopId);

    void deleteGoodsInShoppingCart(@Param("userId") Long userId,
                                   @Param("goodsId") Long goodsId);
}
