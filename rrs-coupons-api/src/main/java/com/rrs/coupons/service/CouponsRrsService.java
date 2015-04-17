package com.rrs.coupons.service;

import com.aixforce.annotations.ParamInfo;
import com.aixforce.common.model.Paging;
import com.aixforce.common.model.Response;
import com.aixforce.search.Pair;
import com.aixforce.user.base.BaseUser;
import com.rrs.coupons.model.*;

import java.util.List;
import java.util.Map;

/**
 * Created by zhum01 on 2014/8/19.
 */
public interface CouponsRrsService {
    int adminCount();
    Response<List<RrsCou>> queryCouponsBy(Long userId);


    //组件
    public Response<List<RrsShowCouponView>> getCouponByUser(@ParamInfo("baseUser") BaseUser baseUser);
    /**
     * 通过订单编号获取优惠信息
     *
     * @param ids
     * @return
     */
    public List<RrsCouOrder> findByOrderIds(List<Long> ids);

    /**
     * 通过订单编号获取优惠信息
     *
     * @param ids
     * @return
     */
    public List<RrsCouOrderItem> findOrderItemsByOrderIds(List<Long> ids);

    /**
     * 查看商品所属类目
     * **/
    Response<List<Pair>> queryPairByItemId(@ParamInfo("itemId") Long itemId);

    /**
     * 商品详情页面 判断该产品是否参加优惠任务 查询该商品的类目判断该商品是否属于该类目 属于 则显示不属于则不显示
     * 1 显示
     * 0 不显示
     * @param itemId 商品id
     * **/
    Response<Long> checkJoin(@ParamInfo("itemId") Long itemId);

    /**
     * 根据优惠券ID查询优惠券信息
     * **/

    Response<RrsCou> queryCouponsById(@ParamInfo("couponsId") Long couponsId);

    /**
     * 修改优惠券信息
     * **/
    Response<Boolean> updateRrsCou(RrsCou rrsCou);

    /**
     *下订单页面判断数据显示 根据当前sku和 baseuser
     * **/
    Response<List<RrsCouUserView>> preCouponsBySku(@ParamInfo("skus") String skus,@ParamInfo("baseUser") BaseUser baseUser);

    /**
     * 根据条件分页查询
     *               status        优惠券状态：有效（1）暂停（2）过期(3)
     *               channelId      频道ID
     *               pageNo        页码
     *               pageSize      每页数量
     * @return              分页结果
     */
    Response<List<RrsCou>> findCouponsByPaging(@ParamInfo("businessId") int businessId,@ParamInfo("status") String status);
    /**
     * 添加优惠券
     * @param paramMap
     * @return 操作是否成功
     */
    Integer addCoupon(Map<String, Object> paramMap);
    /**
     * 修改优惠券
     * @param paramMap
     * @return 操作是否成功
     */
    Response<Boolean> updateCoupon(Map<String, Object> paramMap);
    /**
     * 修改优惠券状态
     * @param paramMap
     * @return 操作是否成功
     */
    Response<Boolean> updateCouponStatus(Map<String, Object> paramMap);
    /**
     *
     * @return
     */
    List<Map<String, Object>> findCategory(Integer categoryId);


    Response<Long> checkJoinAndUser(Long itemId, Long userId);

    //根据优惠券查询列表
    Response<RrsCouUser> queryCouponsUserBy(Long userId,Long couponsId);

    //修改使用的优惠券状态
    void updateCouponUser(Long id);


    /**
     * 后台查询优惠券信息 需要分页
     * **/
    /**
     * 根据shopId或者userId查询店铺 给东软用的 接口 组件
     *
     * @param
     * @return shopDto
     */
    Response<Paging<RrsCou>> queryCouponsByPage(@ParamInfo("businessId") Long businessId,
                                  @ParamInfo("beginCreatedAt") String beginCreatedAt,
                                  @ParamInfo("endCreatedAt") String endCreatedAt,
                                  @ParamInfo("status") Long status,
                                  @ParamInfo("couponsType") Long couponsType,
                                   @ParamInfo("pageNo") Integer pageNo,
                                   @ParamInfo("size") Integer size);


/****
 * 查询商家优惠券  by cwf 勿改动 可使用
 * 用户登陆之后 查询某用户领取的未使用的有效优惠券信息
 * @param baseUser  当前登陆用户
 * @param userStatus 用户领取的优惠券状态 是否使用优惠券状态 1未使用 2使用 3过期
 * @param couponStatus 优惠券的状态 优惠券状态：未生效（0）暂停（1）生效（2）失效(3)
 * @param itemIds 传入多个ItemId (1,2) 逗号隔开
 * ****/
    Response<List<RrsCou>> querySellerCouponsByParam(@ParamInfo("baseUser") BaseUser baseUser,
                                                     @ParamInfo("userStatus") Long userStatus,
                                                     @ParamInfo("couponStatus") String couponStatus,
                                                     @ParamInfo("itemIds") String itemIds
    );

	/**
	 * 根据店铺编号查询商家优惠券
	 *
	 * @param shopId
	 * @return
	 */
    Response<Paging<RrsCou>> queryCouponsByShopId(@ParamInfo("sellerId") Long shopId,@ParamInfo("pageIndex") Long pageIndex,@ParamInfo("pageSize") Long pageSize);

    /**
     * 根据优惠券ID查询优惠券信息
     * **/

    Response<RrsCou> queryShopCouponsById(@ParamInfo("couponsId") Long couponsId);

    /**
     * 后台查询商家优惠券信息 需要分页
     * **/
    Response<Paging<ShopCoupons>> queryShopCouponsByPage(@ParamInfo("channel") String channel,
    							  @ParamInfo("name") String name,
                                  @ParamInfo("shopName") String shopName,
                                  @ParamInfo("status") Long status,
                                  @ParamInfo("pageNo") Integer pageNo,
                                  @ParamInfo("size") Integer size);

    /**
     * 添加商品信息
     * @param paramMap
     * @return 操作是否成功
     */
    Response<Boolean> insertItemIds(List<Map<String, Object>> paramMap);

    List<Map<String,Object>> findEditItems(String couponsId);

    /**
     * 删除商品信息
     * @return 操作是否成功
     */
    Response<Boolean> deleteCouponsId(String couponsId);

    /**
     * 自身平台使用的商家优惠券和平台优惠券组件信息
     * **/
    Response<Paging<RrsCou>> queryRrsCouponsByPage(@ParamInfo("baseUser") BaseUser baseUser,
                                                   @ParamInfo("cpName") String cpName,
                                                   @ParamInfo("businessId") Long businessId,
                                                @ParamInfo("beginCreatedAt") String beginCreatedAt,
                                                @ParamInfo("endCreatedAt") String endCreatedAt,
                                                @ParamInfo("status") String status,
                                                @ParamInfo("couponsType") Long couponsType,
                                                @ParamInfo("sellerName") String sellerName,
                                                @ParamInfo("shopName") String shopName,
                                   @ParamInfo("pageNo") Integer pageNo,
                                   @ParamInfo("size") Integer size);

}


