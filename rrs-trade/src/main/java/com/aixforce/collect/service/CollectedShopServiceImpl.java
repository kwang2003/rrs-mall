package com.aixforce.collect.service;

import com.aixforce.annotations.ParamInfo;
import com.aixforce.collect.dao.CollectedShopDao;
import com.aixforce.collect.dto.CollectedSummary;
import com.aixforce.collect.manager.CollectedManager;
import com.aixforce.collect.model.CollectedShop;
import com.aixforce.common.model.PageInfo;
import com.aixforce.common.model.Paging;
import com.aixforce.common.model.Response;
import com.aixforce.shop.model.Shop;
import com.aixforce.shop.service.ShopService;
import com.aixforce.user.base.BaseUser;
import com.google.common.base.Throwables;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Nullable;

import java.util.List;

import static com.aixforce.common.utils.Arguments.notNull;
import static com.google.common.base.Preconditions.checkState;

/**
 * Mail: xiao@terminus.io <br>
 * Date: 2014-10-10 7:00 PM  <br>
 * Author: xiao
 */
@Slf4j
@Service
public class CollectedShopServiceImpl implements CollectedShopService {

    @Autowired
    private CollectedManager collectedManager;

    @Autowired
    private CollectedShopDao collectedShopDao;

    @Autowired
    private ShopService shopService;




    /**
     * 添加商品收藏记录
     *
     * @param userId 用户id
     * @param shopId 店铺id
     * @return 操作是否成功
     */
    @Override
    public Response<CollectedSummary> create(Long userId, Long shopId) {
        Response<CollectedSummary> result = new Response<CollectedSummary>();

        try {
            CollectedShop collectedShop = new CollectedShop();
            collectedShop.setBuyerId(userId);
            collectedShop.setShopId(shopId);

            Response<Shop> shopResponse = shopService.findById(shopId);
            checkState(shopResponse.isSuccess(), shopResponse.getError());

            Shop shop = shopResponse.getResult();
            collectedShop.setShopNameSnapshot(shop.getName());
            collectedShop.setShopLogoSnapshot(shop.getImageUrl());
            collectedShop.setSellerId(shop.getUserId());

            collectedManager.createCollectedShop(collectedShop);

            Long total = collectedShopDao.countOf(userId);
            result.setResult(new CollectedSummary(collectedShop.getId(), total));

        } catch (IllegalStateException e) {
            log.error("fail to create collect shop whit userId:{}, shopId:{}, error:{}",
                    userId, shopId, e.getMessage());
            result.setError(e.getMessage());
        } catch (Exception e) {
            log.error("fail to create collect shop whit userId:{}, shopId:{}, cause:{}",
                    userId, shopId, Throwables.getStackTraceAsString(e));
            result.setError("collected.item.create.fail");
        }

        return result;
    }

    /**
     * 删除商品收藏记录
     *
     * @param userId 用户id
     * @param shopId 店铺id
     * @return 操作是否成功
     */
    @Override
    public Response<Boolean> delete(Long userId, Long shopId) {
        Response<Boolean> result = new Response<Boolean>();
        try {
            CollectedShop deleting = collectedShopDao.getByUserIdAndShopId(userId, shopId);
            if (notNull(deleting)) {
                collectedShopDao.delete(deleting.getId());
            }

            result.setResult(Boolean.TRUE);

        } catch (Exception e) {
            log.error("fail to delete collected shop with userId:{}, shopId:{}, cause:{}",
                    userId, shopId, Throwables.getStackTraceAsString(e));
            result.setError("collected.shop.delete.fail");
        }
        return result;
    }

    /**
     * 查询用户收藏的店铺
     *
     * @param shopName 店铺名称，选填，模糊匹配
     * @param pageNo   页码
     * @param size     分页大小
     * @param baseUser 查询用户
     * @return 收藏店铺分页
     */
    @Override
    public Response<Paging<CollectedShop>> findBy(@ParamInfo("shopName") @Nullable String shopName, 
                                                  @ParamInfo("pageNo") @Nullable Integer pageNo, 
                                                  @ParamInfo("size") @Nullable Integer size, 
                                                  @ParamInfo("baseUser") BaseUser baseUser) {

        Response<Paging<CollectedShop>> result = new Response<Paging<CollectedShop>>();

        try {

            PageInfo pageInfo = new PageInfo(pageNo, size);

            CollectedShop criteria = new CollectedShop();
            criteria.setShopNameSnapshot(shopName);
            criteria.setBuyerId(baseUser.getId());
            Paging<CollectedShop> paging = collectedShopDao.findBy(criteria, pageInfo.offset, pageInfo.limit);
            result.setResult(paging);

        } catch (Exception e) {
            log.error("fail to query collect items by shopName:{}, pageNo:{}, size:{}, uid:{} cause:{}",
                    shopName, pageNo, size, baseUser.getId(), Throwables.getStackTraceAsString(e));
            result.setResult(Paging.empty(CollectedShop.class));
        }

        return result;
    }

    /**
     * 批量删除店铺收藏记录
     *
     * @param userId  用户id
     * @param shopIds 商品id列表
     * @return 操作是否成功
     */
    @Override
    public Response<Boolean> bulkDelete(Long userId, List<Long> shopIds) {
        Response<Boolean> result = new Response<Boolean>();

        try {
            collectedManager.bulkDeleteCollectedShops(userId, shopIds);
            result.setResult(Boolean.TRUE);
        } catch (IllegalArgumentException e) {
            log.error("fail to delete collected items with userId:{}, itemIds:{}, error:{}",
                    userId, shopIds, e.getMessage());
            result.setError(e.getMessage());
        } catch (IllegalStateException e) {
            log.error("fail to delete collected items with userId:{}, itemIds:{}, error:{}",
                    userId, shopIds, e.getMessage());
            result.setError(e.getMessage());
        } catch (Exception e) {
            log.error("fail to delete collected items with userId:{}, itemIds:{}, cause:{}",
                    userId, shopIds, Throwables.getStackTraceAsString(e));
            result.setError("collected.item.delete.fail");
        }

        return result;
    }
}
