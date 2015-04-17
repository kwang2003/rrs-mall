package com.aixforce.rrs.grid.manager;

import com.aixforce.common.utils.JsonMapper;
import com.aixforce.item.model.Brand;
import com.aixforce.rrs.presale.dao.BrandsSellersDao;
import com.aixforce.rrs.grid.dao.ShopAuthorizeInfoDao;
import com.aixforce.rrs.grid.dto.AuthorizeInfo;
import com.aixforce.rrs.grid.model.BrandsSellers;
import com.aixforce.rrs.grid.model.ShopAuthorizeInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Author:  <a href="mailto:i@terminus.io">jlchen</a>
 * Date: 2014-06-06
 */
@Component
public class ShopAuthorizeInfoManager {

    @Autowired
    private ShopAuthorizeInfoDao shopAuthorizeInfoDao;

    @Autowired
    private BrandsSellersDao brandsSellersDao;

    @Transactional
    public Long create(ShopAuthorizeInfo shopAuthorizeInfo, Long sellerId, String sellerName) {
        final AuthorizeInfo authorizeInfo = JsonMapper.nonEmptyMapper().fromJson(shopAuthorizeInfo.getJsonAuthorize(), AuthorizeInfo.class);
        shopAuthorizeInfoDao.create(shopAuthorizeInfo);
        for (Brand brand : authorizeInfo.getBrands()) {
            BrandsSellers brandsSellers = new BrandsSellers(brand.getId(), brand.getName(), sellerId, sellerName, shopAuthorizeInfo.getShopId());
            brandsSellersDao.create(brandsSellers);
        }
        return shopAuthorizeInfo.getId();
    }

    public ShopAuthorizeInfo findById(Long id) {
        return shopAuthorizeInfoDao.findById(id);
    }

    @Transactional
    public void update(ShopAuthorizeInfo exist, ShopAuthorizeInfo shopAuthorizeInfo, Long sellerId, String sellerName) {

        //先删掉既有的brand_seller 关联关系
        final AuthorizeInfo existAuthorizeInfo = JsonMapper.nonDefaultMapper().fromJson(exist.getJsonAuthorize(), AuthorizeInfo.class);
        for (Brand brand : existAuthorizeInfo.getBrands()) {
            brandsSellersDao.deleteByBrandIdAndSellerId(brand.getId(), sellerId);
        }

        //更新授权
        final AuthorizeInfo authorizeInfo = JsonMapper.nonEmptyMapper().fromJson(shopAuthorizeInfo.getJsonAuthorize(), AuthorizeInfo.class);
        shopAuthorizeInfoDao.update(shopAuthorizeInfo);

        //重建新的brand_seller 关联关系
        for (Brand brand : authorizeInfo.getBrands()) {
            BrandsSellers brandsSellers = new BrandsSellers(brand.getId(), brand.getName(), sellerId, sellerName, exist.getShopId());
            brandsSellersDao.create(brandsSellers);
        }
    }

    @Transactional
    public void delete(ShopAuthorizeInfo exist, Long sellerId) {
        //先删掉既有的brand_seller 关联关系
        final AuthorizeInfo existAuthorizeInfo = JsonMapper.nonDefaultMapper().fromJson(exist.getJsonAuthorize(), AuthorizeInfo.class);
        for (Brand brand : existAuthorizeInfo.getBrands()) {
            brandsSellersDao.deleteByBrandIdAndSellerId(brand.getId(), sellerId);
        }
        //再删除授权
        shopAuthorizeInfoDao.delete(exist.getId());
    }

    public List<ShopAuthorizeInfo> findByShopId(Long shopId) {
        return shopAuthorizeInfoDao.findByShopId(shopId);
    }
}
