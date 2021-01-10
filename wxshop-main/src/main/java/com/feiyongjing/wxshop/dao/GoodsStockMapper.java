package com.feiyongjing.wxshop.dao;

import com.feiyongjing.wxshop.api.data.GoodsInfo;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface GoodsStockMapper {
   int deductStock(GoodsInfo goodsInfo);
}
