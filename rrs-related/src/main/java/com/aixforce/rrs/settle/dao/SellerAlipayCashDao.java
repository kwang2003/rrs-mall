package com.aixforce.rrs.settle.dao;

import com.aixforce.common.model.Paging;
import com.aixforce.rrs.settle.model.AlipayCash;
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

import static com.aixforce.common.utils.Arguments.notNull;
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

/**
 * Mail: remindxiao@gmail.com <br>
 * Date: 2014-02-20 5:18 PM  <br>
 * Author: xiao
 */
@Repository
public class SellerAlipayCashDao extends SqlSessionDaoSupport {

    /**
     * 创建支付宝商户提现汇总记录
     *
     * @param sellerAlipayCash   商户日结算汇总记录
     * @return  新纪录id
     */
    public Long create(SellerAlipayCash sellerAlipayCash) {
        checkNotNull(sellerAlipayCash.getSummedAt());
        checkNotNull(sellerAlipayCash.getSellerId());
        SellerAlipayCash criteria = new SellerAlipayCash();
        criteria.setSellerId(sellerAlipayCash.getSellerId());

        Map<String, Object> params = Maps.newHashMapWithExpectedSize(3);
        params.put("summedStartAt", startOfDay(sellerAlipayCash.getSummedAt()));
        params.put("summedEndAt", endOfDay(sellerAlipayCash.getSummedAt()));
        params.put("criteria", criteria);

        Long count = getSqlSession().selectOne("SellerAlipayCash.countOf", params);
        boolean exist = count > 0L;
        checkState(!exist, "seller.alipay.cash.record.duplicate");
        getSqlSession().insert("SellerAlipayCash.create", sellerAlipayCash);

        return sellerAlipayCash.getId();
    }


    /**
     * 根据标识获取商户支付宝日提现记录信息
     *
     * @param id    标识
     * @return      商户支付宝提现记录信息
     */
    public SellerAlipayCash get(Long id) {
        return getSqlSession().selectOne("SellerAlipayCash.get", id);

    }

    /**
     * 根据商家id与统计日期定位唯一一个商户日提现记录
     *
     * @param sellerId  商家id
     * @param summedAt  统计日期
     * @return  商户日提现汇总
     */
    public SellerAlipayCash getBy(Long sellerId, Date summedAt) {
        checkArgument(notNull(sellerId), "seller.id.can.not.be.empty");
        checkArgument(notNull(summedAt), "summed.at.can.not.be.empty");

        Map<String, Object> params = Maps.newHashMapWithExpectedSize(3);
        params.put("summedStartAt", startOfDay(summedAt));
        params.put("summedEndAt", endOfDay(summedAt));
        params.put("sellerId", sellerId);

        return getSqlSession().selectOne("SellerAlipayCash.getBy", params);
    }


    /**
     * 商户支付宝日提现记录完成同步
     *
     * @param id  记录id
     * @return 是否成功
     */
    public boolean synced(Long id) {
        return getSqlSession().update("SellerAlipayCash.synced", ImmutableMap.of("id", id)) > 0;
    }

    /**
     *
     * 查询分页商户支付宝提现
     *
     * @param sellerId          商户id
     * @param status            提现状态
     * @param summedStartAt     查询统计的起始时间
     * @param summedEndAt       查询统计的截止时间
     * @param offset            偏移值
     * @param limit             每页数据数
     * @return      分页查询信息
     */
    public Paging<SellerAlipayCash> findBy(Long sellerId, Integer status, Date summedStartAt,
                                           Date summedEndAt, Integer offset, Integer limit) {
        Map<String,Object> params = Maps.newHashMapWithExpectedSize(5);
        SellerAlipayCash criteria = new SellerAlipayCash();
        criteria.setSellerId(sellerId);
        criteria.setStatus(status);

        params.put("criteria", criteria);
        params.put("summedStartAt", summedStartAt);
        params.put("summedEndAt", summedEndAt);
        params.put("offset", offset);
        params.put("limit", limit);
        return findBy(params);
    }

    /**
     * 分页查询符合条件的记录
     *
     * @param criteria  标准查询单元
     * @param offset    起始偏移，不能为空
     * @param limit     返回条数，不能为空
     * @return  查询结果
     */
    public Paging<SellerAlipayCash> findBy(SellerAlipayCash criteria, Integer offset, Integer limit) {
        Map<String, Object> params = Maps.newHashMapWithExpectedSize(10);
        params.put("criteria", criteria);
        params.put("offset", offset);
        params.put("limit", limit);

        if (criteria.getSummedAt() != null) {
            Date summedAt = criteria.getSummedAt();
            params.put("summedStartAt", startOfDay(summedAt));
            params.put("summedEndAt", endOfDay(summedAt));
        }
        return findBy(params);
    }

    private Date startOfDay(Date date) {
        if (date == null) { return null; }
        return new DateTime(date).withTimeAtStartOfDay().toDate();
    }

    private Date endOfDay(Date date) {
        if (date == null) { return null; }
        return new DateTime(date).withTimeAtStartOfDay().plusDays(1).toDate();
    }


    public Long countOf(SellerAlipayCash criteria) {
        Map<String, Object> params = Maps.newHashMapWithExpectedSize(10);
        params.put("criteria", criteria);

        if (criteria.getSummedAt() != null) {
            Date summedAt = criteria.getSummedAt();
            params.put("summedStartAt", startOfDay(summedAt));
            params.put("summedEndAt", endOfDay(summedAt));
        }

        return countOf(params);

    }

    /**
     * 获取符合条件的记录数量
     *
     * @param params    参数
     * @return  查询结果
     */
    public Long countOf(Map<String, Object> params) {
        return getSqlSession().selectOne("SellerAlipayCash.countOf", params);
    }


    /**
     * 分页查询符合条件的记录
     *
     * @param params  参数
     * @return  查询结果
     */
    public Paging<SellerAlipayCash> findBy(Map<String, Object> params) {
        Long total = getSqlSession().selectOne("SellerAlipayCash.countOf", params);
        if (total == 0L) {
            return new Paging<SellerAlipayCash>(0L, Collections.<SellerAlipayCash>emptyList());
        }
        List<SellerAlipayCash> settlements = getSqlSession().selectList("SellerAlipayCash.findBy", params);
        return new Paging<SellerAlipayCash>(total, settlements);
    }


    /**
     * 更新指定的统计时间内所有的商户提现状态为 “已提现”
     *
     * @param summedAt 统计时间
     */
    public boolean batchCashing(Date summedAt) {
        return getSqlSession().update("SellerAlipayCash.batchCashing",
                ImmutableMap.of("summedAt", summedAt)) > 0;
    }

    /**
     * 更新指定商户支付宝提现记录状态为“已提现”
     *
     * @param id    标识
     */
    public boolean cashing(Long id) {
        return getSqlSession().update("SellerAlipayCash.cashing", id) == 1;
    }


    /**
     * 查看统一汇总日期下的其他记录是否都已提现
     *
     * @param summedAt 汇总日期
     * @return 所有都已经提现
     */
    public boolean casedAll(Date summedAt) {
        SellerAlipayCash criteria = new SellerAlipayCash();
        criteria.setStatus(SellerAlipayCash.Status.NOT.value());
        criteria.setSummedAt(summedAt);
        return countOf(criteria) == 0L;
    }

    public AlipayCash sumAlipayCash(Date paidAt) {

        Map<String, Object> params = Maps.newHashMapWithExpectedSize(2);
        params.put("summedStartAt", startOfDay(paidAt));
        params.put("summedEndAt", endOfDay(paidAt));
        return getSqlSession().selectOne("SellerAlipayCash.sumAlipayCash", params);
    }

    /**
     * 查询尚未打印凭证的数据
     */
    public Paging<SellerAlipayCash> findSynced(int offset, int limit) {
        SellerAlipayCash criteria = new SellerAlipayCash();
        criteria.setSynced(SellerAlipayCash.Synced.DONE.value());
        Map<String, Object>  params = Maps.newHashMapWithExpectedSize(5);
        params.put("criteria", criteria);
        params.put("offset", offset);
        params.put("limit", limit);

        return findBy(params);
    }

    /**
     * 更新商户汇总的凭证及发票号
     *
     * @param sellerAlipayCash  已开票的商户汇总信息
     */
    public boolean vouching(SellerAlipayCash sellerAlipayCash) {
        SellerAlipayCash updating = new SellerAlipayCash();
        updating.setId(sellerAlipayCash.getId());
        updating.setVouched(SellerAlipayCash.Vouched.DONE.value());
        updating.setVoucher(sellerAlipayCash.getVoucher());
        updating.setVouchedAt(sellerAlipayCash.getVouchedAt());
        return getSqlSession().update("SellerAlipayCash.update", sellerAlipayCash) == 1;
    }

    /**
     * 查询已经提现但未打印凭证的提现单
     */
    public Paging<SellerAlipayCash> findCashedNotVouched(Integer offset, Integer limit) {
        SellerAlipayCash criteria = new SellerAlipayCash();
        criteria.setVouched(SellerAlipayCash.Vouched.NOT.value());
        criteria.setStatus(SellerAlipayCash.Status.DONE.value());
        Map<String, Object> params = Maps.newHashMapWithExpectedSize(4);
        params.put("criteria", criteria);
        params.put("offset", offset);
        params.put("limit", limit);
        return findBy(params);
    }

    /**
     * 批量根据用户更新商户88码
     *
     * @param outerCode 商户88码
     * @param sellerId  商家id
     * @return  更新数量
     */
    public Integer batchUpdateOuterCode(String outerCode, Long sellerId) {
        return getSqlSession().update("SellerAlipayCash.batchUpdateOuterCode",
                ImmutableMap.of("outerCode", outerCode, "sellerId", sellerId));
    }

    /**
     * 更新
     *
     * @param updating  待更新的数据
     * @return  是否执行成功
     */
    public Boolean update(SellerAlipayCash updating) {
        return getSqlSession().update("SellerAlipayCash.update", updating) == 1;
    }

    /**
     * 获取需要提现的商户日汇总列表
     *
     * @param summedAt  获取当前未提现的店铺
     * @return 商户日汇总列表
     */
    public List<SellerAlipayCash> findNotCashedOfDaily(Date summedAt) {
        Map<String, Object> params = Maps.newHashMap();
        params.put("summedStartAt", startOfDay(summedAt));
        params.put("summedEndAt", endOfDay(summedAt));
        return getSqlSession().selectList("SellerAlipayCash.findNotCashedOfDaily", params);
    }
}
