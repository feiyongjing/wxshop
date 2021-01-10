package com.feiyongjing.wxshop.service;

import com.feiyongjing.wxshop.api.DataStatus;
import com.feiyongjing.wxshop.api.exception.HttpException;
import com.feiyongjing.wxshop.api.data.PageResponse;
import com.feiyongjing.wxshop.generate.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class GoodsServiceTest {
    @Mock
    private GoodsMapper goodsMapper;
    @Mock
    private ShopMapper shopMapper;
    @Mock
    private Shop shop;
    @Mock
    private Goods goods;
    @InjectMocks
    private GoodsService goodsService;

    @BeforeEach
    public void initUserContext() {
        User user = new User();
        user.setId(1L);
        UserContext.setCurrentUser(user);
    }

    @AfterEach
    public void clearUserContext() {
        UserContext.setCurrentUser(null);
    }

    @Test
    public void createGoodsSucceedIfUserIsOwner() {
        long ownerId = 1;
        when(shopMapper.selectByPrimaryKey(anyLong())).thenReturn(shop);
        when(shop.getOwnerUserId()).thenReturn(ownerId);
//        when(goodsMapper.insert(goods)).thenReturn(123);
        Assertions.assertEquals(goods, goodsService.create(goods));
//        Mockito.verify(goods).setId(123L);

    }

    @Test
    public void createGoodsFailedIfUserNotOwner() {
        when(shopMapper.selectByPrimaryKey(anyLong())).thenReturn(shop);
        when(shop.getOwnerUserId()).thenReturn(2L);
        Assertions.assertThrows(HttpException.class, () -> {
            goodsService.create(goods);
        });
    }

    @Test
    public void deleteThrowExceptionIfGoodsNotFound() {
        when(goodsMapper.selectByPrimaryKey(anyLong())).thenReturn(null);
        Assertions.assertThrows(HttpException.class, () -> {
            goodsService.deleteGoodsById(1L);
        });
    }

    @Test
    public void deleteGoodsThrowExceptionIfUserIsNotFound() {
        when(goodsMapper.selectByPrimaryKey(anyLong())).thenReturn(goods);
        when(shopMapper.selectByPrimaryKey(anyLong())).thenReturn(shop);
        when(shop.getOwnerUserId()).thenReturn(2L);
        Assertions.assertThrows(HttpException.class, () -> {
            goodsService.deleteGoodsById(1L);
        });
    }

    @Test
    public void deleteGoodsSucceed() {
        when(goodsMapper.selectByPrimaryKey(anyLong())).thenReturn(goods);
        when(shopMapper.selectByPrimaryKey(anyLong())).thenReturn(shop);
        when(shop.getOwnerUserId()).thenReturn(1L);
        Assertions.assertEquals(goods, goodsService.deleteGoodsById(1L));
        verify(goods).setStatus(DataStatus.DELETED.getName());
    }

    @Test
    public void updateThrowExceptionIfGoodsNotFound() {
        when(goodsMapper.selectByPrimaryKey(anyLong())).thenReturn(null);
        Assertions.assertThrows(HttpException.class, () -> {
            goodsService.updateGoodsById(1L, goods);
        });
    }

    @Test
    public void updateGoodsThrowExceptionIfUserIsNotFound() {
        when(goodsMapper.selectByPrimaryKey(anyLong())).thenReturn(goods);
        when(shopMapper.selectByPrimaryKey(anyLong())).thenReturn(shop);
        when(shop.getOwnerUserId()).thenReturn(2L);
        Assertions.assertThrows(HttpException.class, () -> {
            goodsService.updateGoodsById(1L, goods);
        });
    }

    @Test
    public void updateGoodsSucceed() {
        when(goodsMapper.selectByPrimaryKey(anyLong())).thenReturn(goods);
        when(shopMapper.selectByPrimaryKey(anyLong())).thenReturn(shop);
        when(shop.getOwnerUserId()).thenReturn(1L);
        Assertions.assertEquals(goods, goodsService.updateGoodsById(1L, goods));
        verify(goods).setId(1L);
    }

    @Test
    public void getGoodsByIdFailure() {
        when(goodsMapper.selectByPrimaryKey(anyLong())).thenReturn(null);
        Assertions.assertThrows(HttpException.class, () -> {
            goodsService.getGoodsById(1L);
        });
    }

    @Test
    public void getGoodsByIdSucceed() {
        when(goodsMapper.selectByPrimaryKey(anyLong())).thenReturn(goods);
        Assertions.assertEquals(goods, goodsService.getGoodsById(1L));
    }

    @Test
    public void getGoodsSucceedWithNullShopId() {
        List<Goods> mockData = Mockito.mock(List.class);
        when(goodsMapper.countByExample(any())).thenReturn(98L);
        when(goodsMapper.selectByExample(any())).thenReturn(mockData);
        PageResponse<Goods> result = goodsService.getGoods(1, 10, null);
        Assertions.assertEquals(10, result.getTotalPage());
        Assertions.assertEquals(1, result.getPageNum());
        Assertions.assertEquals(10, result.getPageSize());
        Assertions.assertEquals(mockData, result.getData());
    }

    @Test
    public void getGoodsSucceedWithNonNullShopId() {
        List<Goods> mockData = Mockito.mock(List.class);
        when(goodsMapper.countByExample(any())).thenReturn(2000L);
        when(goodsMapper.selectByExample(any())).thenReturn(mockData);
        PageResponse<Goods> result = goodsService.getGoods(155, 10, 1000L);
        Assertions.assertEquals(200, result.getTotalPage());
        Assertions.assertEquals(155, result.getPageNum());
        Assertions.assertEquals(10, result.getPageSize());
        Assertions.assertEquals(mockData, result.getData());
    }
}
