package com.aixforce.rrs.settle.dao;

import com.aixforce.common.model.Paging;
import com.aixforce.rrs.settle.model.AlipayCash;
import com.aixforce.rrs.settle.model.OrderAlipayCash;
import com.aixforce.rrs.settle.model.SellerAlipayCash;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import org.joda.time.DateTime;
import org.mybatis.spring.support.SqlSessionDaoSupport;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static com.aixforce.common.utils.Arguments.isNull;
import static com.aixforce.common.utils.Arguments.notNull;
import static com.aixforce.common.utils.Dates.endOfDay;
import static com.aixforce.common.utils.Dates.startOfDay;
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;

/**
 * Mail: remindxiao@gmail.com <br>
 * Date: 2014-02-20 5:18 PM  <br>
 * Author: xiao
 */
@Repository
public class OrderAlipayCashDao extends SqlSessionDaoSupport {


    private static final String namespace = "OrderAlipayCash.";


    /**
     * 插入时需控制防止重复录入，控制的逻辑为:
     * 1、提现单（提现金额大于0）订单ID与日期唯一
     * 2、退款单（提现金额小于0）订单ID与子订单ID唯一
     *
     */
    public Long create(OrderAlipayCash orderAlipayCash) {
        checkCreateArgs(orderAlipayCash);

        OrderAlipayCash existed;

        if (orderAlipayCash.getCashFee() >= 0) { // 提现单根据订单与交易日期确定
            existed = getBy(orderAlipayCash.getOrderId(), orderAlipayCash.getTradedAt());
        } else {    // 退款单根据订单和提现类型来确定唯一的记录
            existed = getBy(orderAlipayCash.getOrderId(), orderAlipayCash.getCashType());
        }

        checkState(isNull(existed), "order.alipay.cash.record.duplicate");
        getSqlSession().insert(namespace + "create", orderAlipayCash);
        return orderAlipayCash.getId();
    }



    private void checkCreateArgs(OrderAlipayCash orderAlipayCash) {
        checkArgument(notNull(orderAlipayCash.getTradedAt()), "traded.at.can.not.be.empty");
        checkArgument(notNull(orderAlipayCash.getOrderId()), "order.id.can.not.be.empty");
        checkArgument(notNull(orderAlipayCash.getCashFee()), "cash.fee.can.not.be.empty");
        checkArgument(notNull(orderAlipayCash.getAlipayFee()), "alipay.fee.can.not.be.empty");
        checkArgument(notNull(orderAlipayCash.getTotalFee()), "total.fee.can.not.be.empty");
    }


    public OrderAlipayCash get(Long id) {
        return getSqlSession().selectOne(namespace + "get", id);

    }

    public OrderAlipayCash getBy(Long orderId, Date date) {
        return getSqlSession().selectOne(namespace + "getByOrderIdAndDate", ImmutableMap.of("orderId", orderId, "tradedAt", date));
    }

     public OrderAlipayCash getBy(Long orderId, Long orderItemId) {
        return getSqlSession().selectOne(namespace + "getByOrderIdAndOrderItemId", ImmutableMap.of("orderId", orderId, "orderItemId", orderItemId));
    }

    public OrderAlipayCash getBy(Long orderId,Integer cashType) {
        return getSqlSession().selectOne(namespace + "getByOrderIdAndCashType", ImmutableMap.of("orderId", orderId,"cashType",cashType));
    }

    public Long countOf(OrderAlipayCash criteria) {
        Map<String, Object> params = Maps.newHashMap();
        params.put("criteria", criteria);
        return countOf(params);
    }

    public Long countOf(Map<String, Object> params) {
        return getSqlSession().selectOne(namespace + "countOf", params);
    }

    public List<OrderAlipayCash> findByIds(List<Long> ids) {
        return getSqlSession().selectList(namespace + "findByIds", ids);
    }



    public Paging<OrderAlipayCash> findBy(OrderAlipayCash criteria, Integer offset, Integer limit) {
        Map<String, Object> params = Maps.newHashMapWithExpectedSize(10);
        params.put("criteria", criteria);
        params.put("offset", offset);
        params.put("limit", limit);

        if (criteria.getTradedAt() != null) {
            Date tradedAt = criteria.getTradedAt();
            params.put("tradedStartAt", startOfDay(tradedAt));
            params.put("tradedEndAt", endOfDay(tradedAt));
        }

        if (criteria.getCashedAt() != null) {
            Date cashedAt = criteria.getCashedAt();
            params.put("cashedStartAt", startOfDay(cashedAt));
            params.put("cashedEndAt", endOfDay(cashedAt));
        }

        return findBy(params);
    }

    public Paging<OrderAlipayCash> findBy(Map<String, Object> params) {
        Long total = countOf(params);
        if (total == 0L) {
            return new Paging<OrderAlipayCash>(0L, Collections.<OrderAlipayCash>emptyList());
        }
        List<OrderAlipayCash> cashes = getSqlSession().selectList(namespace + "findBy", params);
        return new Paging<OrderAlipayCash>(total, cashes);
    }

    /**
     * 统计指定日期返回内商户提现的统计信息，并包装为 SellerAlipayCash 对象返回
     *
     * @param sellerId   卖家id
     * @param cashedAt   交易日期
     * @return 指定日支付宝手续费的统计信息
     */
    public SellerAlipayCash sumSellerAlipayCash(Long sellerId, Date cashedAt) {
        Map<String, Object> params = Maps.newHashMapWithExpectedSize(3);
        params.put("sellerId", sellerId);
        params.put("startAt", startOfDay(cashedAt));
        params.put("endAt", endOfDay(cashedAt));
        return getSqlSession().selectOne(namespace + "sumSellerAlipayCash", params);
    }


    /**
     * 统计指定日期内提现的统计信息，并包装为 AlipayCash
     *
     * @param cashedAt  交易日期
     * @return  日汇总
     */
    public AlipayCash summaryCashesDaily(Date cashedAt) {
        Map<String, Object> params = Maps.newHashMapWithExpectedSize(3);
        params.put("startAt", startOfDay(cashedAt));
        params.put("endAt", endOfDay(cashedAt));

        return getSqlSession().selectOne(namespace + "summaryCashesDaily", params);
    }


    /**
     * 更新提现记录状态为"已提现"
     *
     * @param id 提现状态
     * @return 是否更新成功
     */
    public boolean cashing(Long id, String operator) {
        OrderAlipayCash updating = new OrderAlipayCash();
        updating.setId(id);
        updating.setStatus(AlipayCash.Status.DONE.value());
        updating.setCashedAt(DateTime.now().toDate());
        updating.setOperator(operator);
        return update(updating);
    }

    /**
     * 更新
     */
    public boolean update(OrderAlipayCash updating) {
        return getSqlSession().update(namespace + "update", updating) == 1;
    }


    /**
     * 统计已提现的商户日汇总提现金额
     *
     * @param sellerId  卖家id
     * @param summedAt  汇总时间
     * @return  已提现金额
     */
    public Long sumCashedAmountOfSellerDaily(Long sellerId, Date summedAt) {
        Map<String, Object> params = Maps.newHashMapWithExpectedSize(3);
        params.put("sellerId", sellerId);
        params.put("cashedStartAt", startOfDay(summedAt));
        params.put("cashedEndAt", endOfDay(summedAt));

        return getSqlSession().selectOne(namespace + "sumCashedAmountOfSellerDaily", params);
    }

    /**
     * 统计已提现的日汇总提现金额
     *
     * @param summedAt  汇总时间
     * @return  已提现金额
     */
    public Long sumCashedAmountOfDaily(Date summedAt) {
        Map<String, Object> params = Maps.newHashMapWithExpectedSize(3);
        params.put("cashedStartAt", startOfDay(summedAt));
        params.put("cashedEndAt", endOfDay(summedAt));

        return getSqlSession().selectOne(namespace + "sumCashedAmountOfDaily", params);
    }


    /**
     * 根据订单号获取列表
     * @param orderId  订单id
     */
    public List<OrderAlipayCash> findByOrderId(Long orderId) {
        return getSqlSession().selectList(namespace + "findByOrderId", orderId);
    }

    /**
     * 批量提现某一天的数据
     *
     * @param sellerId  商家账户
     * @param tradedAt  交易日期
     */
    public void batchCashingOfSeller(Long sellerId, Date tradedAt) {
        Map<String, Object> params = Maps.newHashMap();
        params.put("sellerId", sellerId);
        params.put("tradedStartAt", startOfDay(tradedAt));
        params.put("tradedEndAt", endOfDay(tradedAt));
        getSqlSession().update(namespace + "batchCashingOfSeller", params);
    }

    /**
     * 批量更新凭证号
     * @param sellerId      商家id
     * @param cashedAt      提现日期
     * @param voucher       凭证号
     */
    public void batchVouching(Long sellerId, Date cashedAt, String voucher) {
        checkArgument(notNull(sellerId), "seller.id.can.not.be.empty");
        checkArgument(notNull(cashedAt), "cashed.at.can.not.be.empty");
        checkArgument(notNull(voucher), "voucher.can.not.be.empty");
        Map<String, Object> params = Maps.newHashMap();
        params.put("sellerId", sellerId);
        params.put("cashedStartAt", startOfDay(cashedAt));
        params.put("cashedEndAt", endOfDay(cashedAt));
        params.put("voucher", voucher);
        getSqlSession().update(namespace + "batchVouching", params);
    }


}
