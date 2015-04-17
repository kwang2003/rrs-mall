package com.aixforce.rrs.grid.service;

import com.aixforce.common.model.Response;
import com.aixforce.rrs.grid.service.ShopAuthorizeInfoService;
import com.aixforce.rrs.grid.manager.ShopAuthorizeInfoManager;
import com.aixforce.rrs.grid.model.ShopAuthorizeInfo;
import com.google.common.base.Throwables;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by yangzefeng on 14-1-16
 */
@Service
public class ShopAuthorizeInfoServiceImpl implements ShopAuthorizeInfoService {

    private static final Logger log = LoggerFactory.getLogger(ShopAuthorizeInfoService.class);

    @Autowired
    private ShopAuthorizeInfoManager shopAuthorizeInfoManager;

    private final LoadingCache<Long, List<ShopAuthorizeInfo>> group = CacheBuilder.newBuilder()
            .expireAfterWrite(5, TimeUnit.MINUTES)
            .build(new CacheLoader<Long, List<ShopAuthorizeInfo>>() {
                @Override
                public List<ShopAuthorizeInfo> load(Long shopId) throws Exception {
                    return shopAuthorizeInfoManager.findByShopId(shopId);
                }
            });


    @Override
    public Response<Long> create(ShopAuthorizeInfo shopAuthorizeInfo, Long sellerId, String sellerName) {
        Response<Long> result = new Response<Long>();
        if (shopAuthorizeInfo.getShopId() == null) {
            log.error("shopId can not be null");
            result.setError("illegal.param");
            return result;
        }
        try {
            shopAuthorizeInfoManager.create(shopAuthorizeInfo, sellerId, sellerName);
            group.invalidate(shopAuthorizeInfo.getShopId());
            result.setResult(shopAuthorizeInfo.getId());
            return result;
        } catch (Exception e) {
            log.error("failed to create shopAuthorizeInfo{}, cause:{}", shopAuthorizeInfo, Throwables.getStackTraceAsString(e));
            result.setError("create.shopAuthorizeInfo.fail");
            return result;
        }
    }

    @Override
    public Response<Boolean> update(ShopAuthorizeInfo exist,ShopAuthorizeInfo toBeUpdated, Long sellerId, String sellerName) {
        Response<Boolean> result = new Response<Boolean>();
        try {
            shopAuthorizeInfoManager.update(exist, toBeUpdated, sellerId, sellerName);
            group.invalidate(exist.getShopId());
            result.setResult(Boolean.TRUE);
            return result;
        } catch (Exception e) {
            log.error("failed to update {}, cause:{}", toBeUpdated, Throwables.getStackTraceAsString(e));
            result.setError("update.shopAuthorizeInfo.fail");
            return result;
        }
    }

    @Override
    public Response<Boolean> delete(ShopAuthorizeInfo exist, Long sellerId) {
        Response<Boolean> result = new Response<Boolean>();
        try {
            shopAuthorizeInfoManager.delete(exist, sellerId);
            group.invalidate(exist.getShopId());
            result.setResult(Boolean.TRUE);
            return result;
        } catch (Exception e) {
            log.error("failed to update shopAuthorizeInfo where id={}, cause:{}", exist.getId(), Throwables.getStackTraceAsString(e));
            result.setError("delete.shopAuthorizeInfo.fail");
            return result;
        }
    }

    @Override
    public Response<ShopAuthorizeInfo> findById(Long id) {
        Response<ShopAuthorizeInfo> result = new Response<ShopAuthorizeInfo>();
        if (id == null) {
            log.error("shopAuthorizeInfo id can not be null");
            result.setError("illegal.param");
            return result;
        }
        try {
            ShopAuthorizeInfo shopAuthorizeInfo = shopAuthorizeInfoManager.findById(id);
            result.setResult(shopAuthorizeInfo);
            return result;
        } catch (Exception e) {
            log.error("failed to find shopAuthorizeInfo (id={}), cause:{}", id, Throwables.getStackTraceAsString(e));
            result.setError("query.shopAuthorizeInfo.fail");
            return result;
        }
    }

    @Override
    public Response<List<ShopAuthorizeInfo>> findByShopId(Long shopId) {
        Response<List<ShopAuthorizeInfo>> result = new Response<List<ShopAuthorizeInfo>>();
        if (shopId == null) {
            log.error("shopId can not be null");
            result.setError("illegal.param");
            return result;
        }
        try {
            List<ShopAuthorizeInfo> shopAuthorizeInfos = group.getUnchecked(shopId);
            result.setResult(shopAuthorizeInfos);
            return result;
        } catch (Exception e) {
            log.error("failed to find shopAuthorizeInfo shopId={}, cause:{}", shopId, Throwables.getStackTraceAsString(e));
            result.setError("query.shopAuthorizeInfo.fail");
            return result;
        }
    }

    @Override
    public Response<List<ShopAuthorizeInfo>> findByShopIdNoCache(Long shopId) {
        Response<List<ShopAuthorizeInfo>> result = new Response<List<ShopAuthorizeInfo>>();
        if (shopId == null) {
            log.error("shopId can not be null");
            result.setError("illegal.param");
            return result;
        }
        try {
            List<ShopAuthorizeInfo> shopAuthorizeInfos = shopAuthorizeInfoManager.findByShopId(shopId);
            result.setResult(shopAuthorizeInfos);
            return result;
        } catch (Exception e) {
            log.error("failed to find shopAuthorizeInfo no cache shopId={}, cause:{}", shopId, Throwables.getStackTraceAsString(e));
            result.setError("query.shopAuthorizeInfo.fail");
            return result;
        }
    }
}
