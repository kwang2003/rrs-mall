package com.aixforce.rrs.presale.service;

import com.aixforce.annotations.ParamInfo;
import com.aixforce.common.model.Paging;
import com.aixforce.common.model.Response;
import com.aixforce.rrs.code.dto.DiscountAndUsage;
import com.aixforce.rrs.presale.dto.FatOrderPreSale;
import com.aixforce.rrs.presale.dto.FullItemPreSale;
import com.aixforce.rrs.presale.dto.MarketItem;
import com.aixforce.rrs.presale.dto.PreOrderPreSale;
import com.aixforce.rrs.presale.model.PreSale;
import com.aixforce.trade.model.Order;
import com.aixforce.user.base.BaseUser;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Created by yangzefeng on 14-2-12
 */
public interface PreSaleService {

    /**
     * 根据id查找预售
     * @param id  预售id
     * @return 预售
     */
    Response<PreSale> findById(Long id);

    /**
     * 创建预售，同时创建商品，sku
     * @param preSale preSale
     * @return 是否操作成功
     */
    Response<Boolean> create(PreSale preSale, BaseUser user);

    /**
     * 在未发布前更新预售
     * @param preSale preSale
     * @return 操作是否成功
     */
    Response<Boolean> update(PreSale preSale, BaseUser user);

    /**
     * 发布预售商品
     * @param id    预售id
     * @return 操作是否成功
     */
    Response<Boolean> release(Long id);

    /**
     * 终止预售
     *
     * @param id    预售id
     * @return  操作是否成功
     */
    Response<Boolean> stop(Long id);

    /**
     * 更新预售的虚拟销量
     *
     * @param id            预售id
     * @param quantity      更新后的虚拟销量
     * @return  操作是否成功
     */
    Response<Boolean> updateQuantity(Long id, Integer quantity);


    /**
     * 预售商品分页
     * @param pageNo            页码
     * @param size              每页大小
     * @param start             查询起始时间（基于创建时间)
     * @param end               查询截止时间（基于创建时间）
     * @return 分页后的marketItem列表
     */
    @SuppressWarnings("unused")
    Response<Paging<MarketItem>> pagination(@ParamInfo("pageNo") @Nullable Integer pageNo,
                                            @ParamInfo("size") @Nullable Integer size,
                                            @ParamInfo("status") @Nullable Integer status,
                                            @ParamInfo("start") @Nullable String start,
                                            @ParamInfo("end") @Nullable String end,
                                            @ParamInfo("itemId") @Nullable Long itemId,
                                            @ParamInfo("baseUser")BaseUser user);

    /**
     * 买家预售商品列表
     * @param pageNo 页码
     * @param size 每页大小
     * @return 分页后的marketItem列表
     */
    @SuppressWarnings("unused")
    Response<Paging<MarketItem>> paginationByUser(@ParamInfo("pageNo") Integer pageNo, @ParamInfo("size") Integer size);

    /**
     * 预售商品详情页
     * @param itemId 商品id
     * @return fullItemPreSale
     */
    @SuppressWarnings("unused")
    Response<FullItemPreSale> findFullItemPreSale(@ParamInfo("itemId") Long itemId);

    /**
     * 下订单预览页面
     * @param skus skuId和数量
     * @return FatOrderPreSale
     */
    Response<PreOrderPreSale> preOrderPreSale(@ParamInfo("skus") String skus,
                                              @ParamInfo("rid") Integer regionId,
                                              @ParamInfo("baseUser") BaseUser baseUser);

    /**
     * 根据商品id为预售订单匹配符合授权的商家
     * @param itemId 预售订单对应的商品id
     * @param regionId 区域id,from cookie
     * @param count 购买数量
     * @return 返回符合授权的商家店铺id列表，如没有匹配到，返回false
     */
    Response<List<Long>> findShopForPreOrder(Long itemId, Integer regionId,Integer count);

    /**
     * 创建预售商品订单
     * @param buyerId 买家id
     * @param tradeInfoId 收货地址id
     * @param fatOrderPreSale fatOrderPreSale
     * @return 是否创建成功
     */
    Response<Long> createPreSaleOrder(Long buyerId, Long tradeInfoId,Integer regionId, FatOrderPreSale fatOrderPreSale,
                                      DiscountAndUsage discountAndUsage,String bank);

    /**
     * 根据itemId查找preSale
     * @param itemId 商品id
     * @return preSale
     */
    Response<PreSale> findPreSaleByItemId(Long itemId);

    /**
     * 从预售订单列表移除已经cancel的订单
     * @param orderId 订单id
     * @return 是否移除成功
     */
    Response<Boolean> removePreSaleOrder(Long orderId);

    /**
     * 判断预售商品订单是否过期
     */
    Response<Boolean> verifyPreSaleOrderExpire();

    /**
     * 判断预售商品是否过期
     */
    Response<Boolean> verifyPreSaleExpire();


    /**
     * 如果是预售分仓, 恢复预售分仓的库存
     * @param orderId  订单Id
     * @return  是否成功
     */
    Response<Boolean> recoverPreSaleStorageIfNecessary(Long orderId);

    /**
     * 恢复预售购买限制
     * @param order 订单, 订单必须保证是预售的，不然就报错返回
     * @return 是否成功
     */
    Response<Boolean> recoverPreSaleBuyLimitIfNecessary(Order order);

    /**
     * 根据itemId寻找预售
     * @param itemId  商品id
     * @return  对应的预售
     */
    Response<PreSale> findByItemId(Long itemId);

    /**
     * 判断对应的仓库的库存是否足够
     *
     * @param itemId   商品id
     * @param regionId 地区id
     * @param quantity 购买数量
     * @return 是否有足够的库存
     */
     boolean enoughStock(Long itemId, Integer regionId, Integer quantity);
}
