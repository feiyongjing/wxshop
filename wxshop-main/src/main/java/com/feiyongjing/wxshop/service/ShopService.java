package com.feiyongjing.wxshop.service;

import com.feiyongjing.wxshop.api.DataStatus;
import com.feiyongjing.wxshop.api.exception.HttpException;
import com.feiyongjing.wxshop.api.data.PageResponse;
import com.feiyongjing.wxshop.generate.Shop;
import com.feiyongjing.wxshop.generate.ShopExample;
import com.feiyongjing.wxshop.generate.ShopMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
public class ShopService {
    private ShopMapper shopMapper;

    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    @Autowired
    public ShopService(ShopMapper shopMapper) {
        this.shopMapper = shopMapper;
    }

    public PageResponse<Shop> getShop(Integer pageNum, Integer pageSize) {
        Long UserId = UserContext.getCurrentUser().getId();
        int totalNum = countShop(UserId);
        int totalPage = totalNum % pageSize == 0 ? totalNum / pageSize : totalNum / pageSize + 1;
        ShopExample shopExample = new ShopExample();
        shopExample.createCriteria().andOwnerUserIdEqualTo(UserId)
                .andStatusNotEqualTo(DataStatus.DELETED.getName());
        shopExample.setLimit(pageSize);
        shopExample.setOffset((pageNum - 1) * pageSize);
        List<Shop> data = shopMapper.selectByExample(shopExample);
        return PageResponse.of(pageNum, pageSize, totalPage, data);
    }

    private int countShop(Long UserId) {
        ShopExample shopExample = new ShopExample();
        shopExample.createCriteria().andOwnerUserIdEqualTo(UserId);
        return (int) shopMapper.countByExample(shopExample);
    }

    public Shop updateShop(Long id, Shop shop) {
        Shop shop1 = shopMapper.selectByPrimaryKey(id);
        if (shop1 == null) {
            throw HttpException.notFound("店铺未找到!");
        }
        if (Objects.equals(shop.getOwnerUserId(), UserContext.getCurrentUser().getId())) {
            shop.setId(id);
            shop.setStatus(DataStatus.OK.getName());
            shopMapper.updateByPrimaryKey(shop);
        } else {
            throw HttpException.forbidden("无法修改非自己管理的店铺");
        }
        return shopMapper.selectByPrimaryKey(id);
    }

    public Shop createShop(Shop shop) {
        shop.setOwnerUserId(UserContext.getCurrentUser().getId());
        shop.setStatus(DataStatus.OK.getName());
        shopMapper.insertSelective(shop);
        return shopMapper.selectByPrimaryKey(shop.getId());
    }

    public Shop deleteShop(Long id) {
        Shop shop = shopMapper.selectByPrimaryKey(id);
        if (shop == null) {
            throw HttpException.notFound("商品未找到!");
        }
        if (Objects.equals(shop.getOwnerUserId(), UserContext.getCurrentUser().getId())) {
            shop.setStatus(DataStatus.DELETED.getName());
            shopMapper.updateByPrimaryKey(shop);
        } else {
            throw HttpException.forbidden("无法删除非自己管理店铺的商品");
        }
        return shop;
    }

    public Shop getShopById(Long id) {
        Shop shop = shopMapper.selectByPrimaryKey(id);
        if (shop == null) {
            throw HttpException.notFound("未找到指定店铺!");
        }
        return shop;
    }
}
