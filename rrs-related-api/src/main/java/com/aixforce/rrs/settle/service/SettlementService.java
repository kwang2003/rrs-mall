package com.aixforce.rrs.settle.service;

import com.aixforce.annotations.ParamInfo;
import com.aixforce.common.model.Paging;
import com.aixforce.common.model.Response;
import com.aixforce.rrs.settle.dto.FatSettlement;
import com.aixforce.rrs.settle.model.AlipayTrans;
import com.aixforce.rrs.settle.model.ItemSettlement;
import com.aixforce.rrs.settle.model.Settlement;
import com.aixforce.shop.model.Shop;
import com.aixforce.user.base.BaseUser;
import javax.annotation.Nullable;
import java.util.Date;
import java.util.List;

/**
 * 订单结算服务
 * Author:  <a href="mailto:jlchen.cn@gmail.com">jlchen</a>
 * Date: 2014-01-18
 */
public interface SettlementService {

    /**
     * 商户标记订单确认状态为“已确认”
     *
     * @param id        订单结算记录id
     * @param userId    用户id
     * @return  是否更新成功
     */
    Response<Boolean> confirmed(Long id, Long userId);


    /**
     * 根据订单号生成该订单下的所有结算记录 用于预售定金支付成功后<br/>
     * 当用户打款进来就会产生结算，相同的订单后续流程将会更新此表记录
     *
     *
     * @param orderId   订单号
     * @return 创建成功返回的结算id
     */
    Response<Long> generateForPresale(Long orderId,Date paidAt);


    /**
     * 根据订单号生成该订单下的所有结算记录 <br/>
     * 当用户打款进来就会产生结算，相同的订单后续流程将会更新此表记录
     *
     *
     * @param orderId   订单号
     * @return 创建成功返回的结算id
     */
    Response<Long> generate(Long orderId);


    /**
     * 根据订单号生成联合支付的订单结算记录 </br>
     *
     * @param orderId  订单号
     * @return 创建成功返回的结算id
     */
    Response<Long> generateMulti(Long orderId);

    /**
     * 分页查找指定日期范围内的子订单结算记录
     *
     * @param orderId   订单号
     * @param pageNo    起始偏移, 可以为空
     * @param size      返回条数, 可以为空
     * @return 查询结果
     */
    Response<Paging<ItemSettlement>> findSubsBy(@ParamInfo("orderId") Long orderId,
                                                @ParamInfo("pageNo") @Nullable Integer pageNo,
                                                @ParamInfo("size") @Nullable Integer size,
                                                @ParamInfo("baseUser") BaseUser user);



    /**
     * 分页查找符合条件的有效订单结算记录，有效订单指商户收入不为0的结算记录
     *
     * @param sellerName        商户名称, 可以为空
     * @param settleStatus      结算状态, 可以为空
     * @param paidStartAt       确认起始日期,可以为空
     * @param paidEndAt         确认截止日期,可以为空
     * @param paidAt            确认时间(具体到天)，可以为空
     * @param cashed            有线上交易的订单（非普通货到付款订单)
     * @param business          类别，可以为空
     * @param size              返回条数, 可以为空
     * @return 查询结果
     */
    Response<Paging<FatSettlement>> findValidBy(@ParamInfo("sellerName") @Nullable String sellerName,
                                                @ParamInfo("orderId") @Nullable Long orderId,
                                                @ParamInfo("status") @Nullable Integer settleStatus,
                                                @ParamInfo("startAt") @Nullable String paidStartAt,
                                                @ParamInfo("endAt") @Nullable String paidEndAt,
                                                @ParamInfo("paidAt") @Nullable String paidAt,
                                                @ParamInfo("confirmedAt") @Nullable String confirmedAt,
                                                @ParamInfo("cashed") @Nullable Boolean cashed,
                                                @ParamInfo("business") @Nullable Long business,
                                                @ParamInfo("type") @Nullable Integer type,
                                                @ParamInfo("pageNo") @Nullable Integer pageNo,
                                                @ParamInfo("size") @Nullable Integer size,
                                                @ParamInfo("baseUser") BaseUser user);



    /**
     * 分页查找符合条件的订单结算记录
     *
     * @param sellerName        商户名称, 可以为空
     * @param settleStatus      结算状态, 可以为空
     * @param paidStartAt       确认起始日期,可以为空
     * @param paidEndAt         确认截止日期,可以为空
     * @param paidAt            确认时间(具体到天)，可以为空
     * @param cashed            有线上交易的订单（非普通货到付款订单)
     * @param business          类别，可以为空
     * @param size              返回条数, 可以为空
     * @return 查询结果
     */
    Response<Paging<FatSettlement>> findBy(@ParamInfo("sellerName") @Nullable String sellerName,
                                           @ParamInfo("orderId") @Nullable Long orderId,
                                           @ParamInfo("status") @Nullable Integer settleStatus,
                                           @ParamInfo("startAt") @Nullable String paidStartAt,
                                           @ParamInfo("endAt") @Nullable String paidEndAt,
                                           @ParamInfo("paidAt") @Nullable String paidAt,
                                           @ParamInfo("confirmedAt") @Nullable String confirmedAt,
                                           @ParamInfo("cashed") @Nullable Boolean cashed,
                                           @ParamInfo("business") @Nullable Long business,
                                           @ParamInfo("type") @Nullable Integer type,
                                           @ParamInfo("pageNo") @Nullable Integer pageNo,
                                           @ParamInfo("size") @Nullable Integer size,
                                           @ParamInfo("baseUser") BaseUser user) ;


    /**
     * 更新和8码相关的所有表, 批量操作（谨慎使用）
     *
     * @param outerCode     商户编码
     * @param shop          店铺信息
     * @return  执行是否成功
     */
    Response<Boolean> batchUpdateOuterCodeOfShopRelated(String outerCode, Shop shop);


    /**
     * 获取支付宝的帐务记录
     *
     * @param transNo   支付宝交易流水
     * @return 支付宝帐务记录
     */
    Response<List<AlipayTrans>> findAlipayTransByMerchantNo(String transNo);

    /**
     * 根据订单id和type 查询唯一子订单结算
     * @param orderId 订单id
     * @param type 1:普通交易, 2:预售定金, 3:预售尾款
     * @return 子订单结算
     */
    Response<ItemSettlement> findByOrderIdAndType(Long orderId,Integer type);

    /**
     * 更新子订单结算
     * @param itemSettlement 子订单结算对象
     * @return 是否更新成功
     */
    Response<Boolean> updateItemSettlement(ItemSettlement itemSettlement);

    /**
     * 更新订单结算
     * @param settlement 订单结算对象
     * @return 是否更新成功
     */
    Response<Boolean> updateSettlement(Settlement settlement);

    /**
     * 根据订单id唯一订单结算
     * @param orderId 订单id
     * @return 订单结算
     */
    Response<Settlement> findByOrderId(Long orderId);

    /**
     * 更改某订单的结算状态
     * @author jiangpeng
     * @createAt 2015/1/6 15:10
     * @param orderNo 订单号
     * @param state 订单状态
     * @return 响应结果
     */
    Response<Boolean> updateSettleStatus(String orderNo,String state);
}
