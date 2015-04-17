package com.aixforce.rrs.settle.dao;

import com.aixforce.common.model.Paging;
import com.aixforce.common.utils.Arguments;
import com.aixforce.rrs.settle.model.*;
import com.aixforce.trade.model.Order;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import org.joda.time.DateTime;
import org.mybatis.spring.support.SqlSessionDaoSupport;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static com.aixforce.common.utils.Arguments.notNull;
import static com.google.common.base.Preconditions.checkArgument;

/**
 * Author:  <a href="mailto:jlchen.cn@gmail.com">jlchen</a>
 * Date: 2014-01-19
 */
@Repository
public class SettlementDao extends SqlSessionDaoSupport {

    public Long create(Settlement settlement) {
        //避免重复创建
        checkArgument(settlement.getOrderId() != null , "settlement.orderId cannot be null");
        Settlement criteria = new Settlement();
        criteria.setOrderId(settlement.getOrderId());
        Long count = countOf(criteria);
        checkArgument(count == 0 , "settlement.duplicate");    // 防止重复创建
        getSqlSession().insert("Settlement.create", settlement);
        return settlement.getId();
    }

    public Settlement get(Long id) {
        return getSqlSession().selectOne("Settlement.get", id);
    }

    public Boolean delete(Long id) {
        return getSqlSession().delete("Settlement.delete", id) == 1;
    }


    /**
     * 设置订单结算记录为已结算
     *
     * @param id 订单结算记录id
     * @return 是否更新成功
     */
    public boolean settled(Long id) {
        return getSqlSession().update("Settlement.settled", id) == 1;
    }

    /**
     * 订单关闭时需要更新订单结算记录的相应状态,可能是卖家退款或者交易成功, 此时订单结算记录会是可以结算的状态
     *
     * @param sellerId 卖家id
     * @param orderId  订单id
     * @param status   订单状态, 可能是卖家退款或者交易成功
     * @return 是否更新成功
     */
    public boolean closeOrder(Long sellerId, Long orderId, Order.Status status) {
        int count = getSqlSession().update("Settlement.closeOrder",
                ImmutableMap.of("sellerId", sellerId, "orderId", orderId, "tradeStatus", status.value()));
        return count > 0;
    }


    /**
     * 返回符合条件的结算信息数量
     *
     * @param criteria 标准查询单元
     * @return  符合条件的结算信息数量
     */
    public Long countOf(Settlement criteria) {
        Map<String, Object> params = Maps.newHashMapWithExpectedSize(20);
        params.put("criteria", criteria);

        if (criteria.getOrderedAt() != null) {
            Date orderedAt = criteria.getOrderedAt();
            params.put("orderedStartAt", startOfDay(orderedAt));
            params.put("orderedEndAt", endOfDay(orderedAt));
        }

        if (criteria.getPaidAt() != null) {
            Date paidAt = criteria.getPaidAt();
            params.put("paidStartAt", startOfDay(paidAt));
            params.put("paidEndAt", endOfDay(paidAt));
        }

        if (criteria.getFinishedAt() != null) {
            Date finishedAt = criteria.getFinishedAt();
            params.put("finishedStartAt", startOfDay(finishedAt));
            params.put("finishedEndAt", endOfDay(finishedAt));
        }

        if (criteria.getSettledAt() != null) {
            Date settledAt = criteria.getSettledAt();
            params.put("settledStartAt", startOfDay(settledAt));
            params.put("settledEndAt", endOfDay(settledAt));
        }

        if (criteria.getConfirmedAt() != null) {
            Date confirmedAt = criteria.getConfirmedAt();
            params.put("confirmedStartAt", startOfDay(confirmedAt));
            params.put("confirmedEndAt", endOfDay(confirmedAt));
        }

        return getSqlSession().selectOne("Settlement.countOf", params);
    }

    /**
     * 分页查找指定日期范围(基于paid_at)内的订单结算记录
     *
     * @param startAt       开始日期 , 不能为空
     * @param endAt         结束日期 , 不能为空
     * @param sellerId      卖家id, 可以为空
     * @param settleStatus  卖家id, 可以为空
     * @param offset        起始偏移, 不能为空
     * @param limit         返回条数, 不能为空
     * @return 查询结果
     */
    public Paging<Settlement> findBy(Long sellerId, Integer settleStatus, Date startAt, Date endAt,  Integer offset, Integer limit) {
        Map<String, Object> params = Maps.newHashMapWithExpectedSize(5);
        Settlement criteria = new Settlement();
        criteria.setSellerId(sellerId);
        criteria.setSettleStatus(settleStatus);
        params.put("criteria", criteria);
        params.put("settledStartAt", startAt);
        params.put("settledEndAt", endAt);
        params.put("offset", offset);
        params.put("limit", limit);
        return findBy(params);
    }

    /**
     * 获取合并支付的T日订单
     */
    public Paging<Settlement> findOfMultiPaid(Settlement criteria, int offset, int limit) {
        Map<String, Object> params = Maps.newHashMapWithExpectedSize(4);
        criteria.setMultiPaid(Settlement.MultiPaid.YES.value());
        params.put("criteria", criteria);
        return findBy(criteria, offset, limit);
    }

    /**
     * 获取非合并支付的T日订单
     */
    public Paging<Settlement> findOfNoMultiPaid(Settlement criteria, int offset, int limit) {
        Map<String, Object> params = Maps.newHashMapWithExpectedSize(4);
        criteria.setMultiPaid(Settlement.MultiPaid.NOT.value());
        params.put("criteria", criteria);
        return findBy(criteria, offset, limit);
    }

    /**
     * 分页查询符合条件的记录
     *
     * @param criteria  标准查询单元
     * @param offset    起始偏移，不能为空
     * @param limit     返回条数，不能为空
     * @return  查询结果
     */
    public Paging<Settlement> findBy(Settlement criteria, Integer offset, Integer limit) {
        Map<String, Object> params = Maps.newHashMapWithExpectedSize(20);
        params.put("criteria", criteria);
        params.put("offset", offset);
        params.put("limit", limit);

        if (criteria.getOrderedAt() != null) {
            Date orderedAt = criteria.getOrderedAt();
            params.put("orderedStartAt", startOfDay(orderedAt));
            params.put("orderedEndAt", endOfDay(orderedAt));
        }

        if (criteria.getPaidAt() != null) {
            Date paidAt = criteria.getPaidAt();
            params.put("paidStartAt", startOfDay(paidAt));
            params.put("paidEndAt", endOfDay(paidAt));
        }

        if (criteria.getFinishedAt() != null) {
            Date finishedAt = criteria.getFinishedAt();
            params.put("finishedStartAt", startOfDay(finishedAt));
            params.put("finishedEndAt", endOfDay(finishedAt));
        }

        if (criteria.getSettledAt() != null) {
            Date settledAt = criteria.getSettledAt();
            params.put("settledStartAt", startOfDay(settledAt));
            params.put("settledEndAt", endOfDay(settledAt));
        }

        if (criteria.getConfirmedAt() != null) {
            Date confirmedAt = criteria.getConfirmedAt();
            params.put("confirmedStartAt", startOfDay(confirmedAt));
            params.put("confirmedEndAt", endOfDay(confirmedAt));
        }

        if (criteria.getCreatedAt() != null) {
            Date createdAt = criteria.getCreatedAt();
            params.put("createdStartAt", startOfDay(createdAt));
            params.put("createdEndAt", endOfDay(createdAt));
        }
        return findBy(params);
    }

    /**
     * 分页查询符合条件的记录
     *
     * @param params  参数
     * @return  查询结果
     */
    public Paging<Settlement> findBy(Map<String, Object> params) {
        Long total = getSqlSession().selectOne("Settlement.countOf", params);
        if (total == 0L) {
            return new Paging<Settlement>(0L, Collections.<Settlement>emptyList());
        }
        List<Settlement> settlements = getSqlSession().selectList("Settlement.findBy", params);
        return new Paging<Settlement>(total, settlements);
    }


    private Date startOfDay(Date date) {
        if (date == null) { return null; }
        return new DateTime(date).withTimeAtStartOfDay().toDate();
    }

    private Date endOfDay(Date date) {
        if (date == null) { return null; }
        return new DateTime(date).withTimeAtStartOfDay().plusDays(1).toDate();
    }


    /**
     * 根据订单号获取订单结算信息
     *
     * @param orderId   订单号
     * @return          订单结算列表
     */
    public Settlement getByOrderId(Long orderId) {
        Map<String, Object> params = Maps.newHashMapWithExpectedSize(1);
        params.put("orderId", orderId);
        return getSqlSession().selectOne("Settlement.getByOrderId", params);
    }

    /**
     * 分页查找指定日期范围(基于settle_at)内的订单结算记录
     *
     * @param startAt  开始日期 , 不能为空
     * @param endAt    结束日期 , 不能为空
     * @param offset   起始偏移, 不能为空
     * @param limit    返回条数, 不能为空
     * @return 查询结果
     */
    public Paging<Settlement> findByFinishedAt(Date startAt, Date endAt, Integer offset, Integer limit) {
        Map<String, Object> params = Maps.newHashMapWithExpectedSize(5);
        params.put("startAt", startAt);
        params.put("endAt", endAt);
        params.put("offset", offset);
        params.put("limit", limit);

        Long total = getSqlSession().selectOne("Settlement.countOfFinishedAt", params);
        if (total == 0L) {
            return new Paging<Settlement>(0L, Collections.<Settlement>emptyList());
        }
        List<Settlement> settlements = getSqlSession().selectList("Settlement.findByFinishedAt", params);
        return new Paging<Settlement>(total, settlements);
    }



    /**
     * 进行订单结算, 将结算状态设为正在结算,并计算卖家收入,平台收入,第三方佣金收入等
     *
     * @param settlement 已计算好的结算分配
     * @return 是否更新成功
     */
    public boolean settle(Settlement settlement) {
        return getSqlSession().update("Settlement.settle", settlement) == 1;
    }


    /**
     * 更新
     * @param settlement 结算记录
     * @return 是否更新成功
     */
    public boolean update(Settlement settlement) {
        return getSqlSession().update("Settlement.update", settlement) == 1;
    }

    /**
     * 更新结算凭证号
     *
     * @param id      订单结算记录id
     * @param voucher 结算凭证号
     * @return 是否更新成功
     */
    public boolean updateVoucher(Long id, String voucher) {
        return getSqlSession().update("Settlement.updateVoucher",
                ImmutableMap.of("id", id, "voucher", voucher)) > 0;
    }

    /**
     * 更新结束时间
     *
     * @param settlement     待更新的记录
     * @return 是否更新成功
     */
    public boolean finished(Settlement settlement) {
        Settlement updating = new Settlement();
        updating.setFinished(Settlement.Finished.DONE.value());
        updating.setFinishedAt(DateTime.now().toDate());
        updating.setCashed(settlement.getCashed());
        updating.setTradeStatus(settlement.getTradeStatus());
        updating.setSettleStatus(settlement.getSettleStatus());
        updating.setId(settlement.getId());

        return update(updating);
    }

    /**
     * 商户确认订单
     *
     * @param id    订单结算记录id
     * @return  是否确认成功
     */
    public boolean confirmed(Long id) {
        return getSqlSession().update("Settlement.confirmed",
                ImmutableMap.of("id", id)) == 1;

    }

    /**
     * 获取卖家日汇总
     *
     * @param sellerId      商户id
     * @param startAt       起始时间
     * @param endAt         截止时间
     * @return  商户汇总信息
     */
    public SellerSettlement sumSellerSettlement(Long sellerId, Date startAt, Date endAt) {
        return getSqlSession().selectOne("Settlement.sumSellerSettlement",
                ImmutableMap.of("sellerId", sellerId, "startAt", startAt, "endAt", endAt));
    }

    /**
     * 获取日汇总
     *
     * @param startAt   起始时间
     * @param endAt     截止时间
     * @return  日汇总信息
     */
    public DailySettlement sumDailySettlement(Date startAt, Date endAt) {
        return getSqlSession().selectOne("Settlement.sumDailySettlement",
                ImmutableMap.of("startAt", startAt, "endAt", endAt));

    }

    /**
     * 更新指定时间段内的支付的订单提现状态为 "已提现"
     *
     * @param sellerId      商户id,若为NULL则更新所有商户记录
     * @param paidStartAt   支付起始时间
     * @param paidEndAt     支付截止时间
     */
    public void batchSetSettlementsAsCashed(Long sellerId, Date paidStartAt, Date paidEndAt) {
        Map<String, Object> params = Maps.newHashMapWithExpectedSize(3);
        params.put("sellerId", sellerId);
        params.put("paidStartAt", paidStartAt);
        params.put("paidEndAt", paidEndAt);
        getSqlSession().update("Settlement.batchCashed", params);
    }


    /**
     * 更新 "提现完成&订单关闭" 的记录为 "结算中"
     *
     * @param sellerId      商户id,若为NULL则更新所有商户记录
     * @param paidStartAt   支付起始时间
     * @param paidEndAt     支付截止时间
     */
    public void batchSetSettlementAsIng(Long sellerId, Date paidStartAt, Date paidEndAt) {
        Map<String, Object> params = Maps.newHashMapWithExpectedSize(3);
        params.put("sellerId", sellerId);
        params.put("paidStartAt", paidStartAt);
        params.put("paidEndAt", paidEndAt);
        getSqlSession().update("Settlement.batchMarkAsIng", params);
    }




    /**
     * 批量更新符合条件的结算信息的凭证号及发票号等信息
     *
     * @param sellerId                  商家号
     * @param voucher                   凭证号
     * @param vouchedAt                 凭证打印时间
     * @param thirdPartyReceipt         第三方（支付宝手续费）发票号
     * @param thirdPartyReceiptAt       第三方（支付宝手续费）发票打印时间
     * @param confirmedStartAt          查询起始时间（基于确认时间）
     * @param confirmedEndAt            查询截止时间（基于截止时间）
     * @return   是否更新成功
     */
    public boolean batchVouched(Long sellerId,
                                String voucher, Date vouchedAt,
                                String thirdPartyReceipt, Date thirdPartyReceiptAt,
                                Date confirmedStartAt, Date confirmedEndAt) {
        Map<String, Object> params = Maps.newHashMapWithExpectedSize(8);
        params.put("sellerId", sellerId);
        params.put("voucher", voucher);
        params.put("vouchedAt", vouchedAt);
        params.put("thirdPartyReceipt", thirdPartyReceipt);
        params.put("thirdPartyReceiptAt", thirdPartyReceiptAt);
        params.put("confirmedStartAt", confirmedStartAt);
        params.put("confirmedEndAt", confirmedEndAt);
        getSqlSession().update("Settlement.batchVouched", params);  // 如没有订单会返回0，这是正常现象
        return true;
    }

    /**
     * 批量更新符合条件 <br/>
     * 同步状态 -> “已同步” <br/>
     * 结算状态 -> “已结算”
     *
     * @param confirmedStartAt      查询起始时间（基于确认时间）
     * @param confirmedEndAt        查询截止时间（基于截止时间）
     * @return  是否更新成功
     */
    public boolean batchSynced(Date confirmedStartAt, Date confirmedEndAt) {
        Map<String, Object> params = Maps.newHashMapWithExpectedSize(2);
        params.put("confirmedStartAt", confirmedStartAt);
        params.put("confirmedEndAt", confirmedEndAt);
        getSqlSession().update("Settlement.batchSynced", params);
        return true;
    }


    /**
     * 根据支付宝交易流水来获取结算记录
     * @param paymentCode  支付宝交易流水号
     * @return 结算信息列表
     */
    public List<Settlement> findByPaymentCode(String paymentCode) {
        Settlement criteria = new Settlement();
        criteria.setPaymentCode(paymentCode);
        return getSqlSession().selectList("Settlement.findBy", ImmutableMap.of("criteria", criteria));
    }


    /**
     * 标记为补帐记录
     */
    public Boolean fixed(Settlement settlement) {
        checkArgument(notNull(settlement.getId()), "settlement.id.can.not.be.empty");
        Settlement updating = new Settlement();
        updating.setId(settlement.getId());
        updating.setFixed(Boolean.TRUE);
        updating.setFinished(settlement.getFinished());
        updating.setFinishedAt(settlement.getFinishedAt());
        updating.setCashed(settlement.getCashed());
        updating.setSettleStatus(settlement.getSettleStatus());
        return update(updating);
    }

    /**
     * 更新结算状态
     * @author jiangpeng
     * @createAt 2015/1/7 10:28
     * @param orderNo
     * @param state
     * @return
     */
    public Boolean updateSettleStatus(String orderNo,String state){
        return getSqlSession().update("Settlement.updateSettleStatus",
                ImmutableMap.of("orderNo", orderNo, "state", state))==1;
    }
}
