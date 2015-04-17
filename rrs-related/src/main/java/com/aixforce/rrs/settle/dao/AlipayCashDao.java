package com.aixforce.rrs.settle.dao;

import com.aixforce.common.model.Paging;
import com.aixforce.rrs.settle.model.AlipayCash;
import com.google.common.collect.Maps;
import org.joda.time.DateTime;
import org.mybatis.spring.support.SqlSessionDaoSupport;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

/**
 * Mail: remindxiao@gmail.com <br>
 * Date: 2014-01-23 2:04 PM  <br>
 * Author: xiao
 */
@Repository
public class AlipayCashDao extends SqlSessionDaoSupport{

    /**
     * 创建支付宝提现汇总记录
     *
     * @param alipayCash   日结算汇总记录
     * @return  新纪录id
     */
    public Long create(AlipayCash alipayCash) {
        checkNotNull(alipayCash.getSummedAt());

        Map<String, Object> params = Maps.newHashMapWithExpectedSize(3);
        params.put("summedStartAt", startOfDay(alipayCash.getSummedAt()));
        params.put("summedEndAt", endOfDay(alipayCash.getSummedAt()));

        Long count = getSqlSession().selectOne("AlipayCash.countOf", params);
        boolean canInsert = count == 0L;
        // 不允许同一天的数据出现多条
        checkState(canInsert, "alipay.cash.record.duplicate");
        getSqlSession().insert("AlipayCash.create", alipayCash);
        return alipayCash.getId();
    }


    /**
     * 根据起止日期分页查询每日支付宝提现汇总信息
     * @param summedStartAt     起始日期
     * @param summedEndAt       截止日期
     * @param offset            偏移值
     * @param limit             返回记录数
     * @return  分页查询结果
     */
    public Paging<AlipayCash> findBy(Date summedStartAt, Date summedEndAt, Integer offset, Integer limit) {
        Map<String,Object> params = Maps.newHashMapWithExpectedSize(3);
        params.put("summedStartAt", summedStartAt);
        params.put("summedEndAt", summedEndAt);
        params.put("offset", offset);
        params.put("limit", limit);
        return findBy(params);
    }


    /**
     * 根据指定条件分页查询支付宝日提现记录
     * @param criteria  标准查询单元
     * @param offset    偏移值
     * @param limit     返回记录数
     * @return  分页查询结果
     */
    public Paging<AlipayCash> findBy(AlipayCash criteria, Integer offset, Integer limit) {
        Map<String,Object> params = Maps.newHashMapWithExpectedSize(3);
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


    public Paging<AlipayCash> findBy(Map<String, Object> params) {
        Long count = getSqlSession().selectOne("AlipayCash.countOf", params);
        if(count == 0L){
            return new Paging<AlipayCash>(0L, Collections.<AlipayCash>emptyList());
        }
        List<AlipayCash> r = getSqlSession().selectList("AlipayCash.findBy",params);
        return new Paging<AlipayCash>(count, r);
    }

    /**
     * 根据标识获取支付宝提现记录信息
     *
     * @param id    标识
     * @return      支付宝提现记录信息
     */
    public AlipayCash get(Long id) {
        return getSqlSession().selectOne("AlipayCash.get", id);

    }

    /**
     * 更新指定支付宝提现记录状态为“已提现”
     *
     * @param id    标识
     * @return  是否成功
     */
    public boolean cashing(Long id) {
        return getSqlSession().update("AlipayCash.cashing", id) == 1;
    }

    /**
     * 刪除指定记录
     *
     * @param id    标识
     * @return  是否成功
     */
    public boolean delete(Long id) {
        return getSqlSession().delete("AlipayCash.delete", id) == 1;
    }

    /**
     * 获取指定汇总日期的支付宝提现记录
     *
     * @param summedAt  汇总日期
     * @return 支付宝提现记录
     */
    public AlipayCash getBySummedAt(Date summedAt) {
        Map<String, Object> params = Maps.newHashMapWithExpectedSize(2);
        params.put("summedStartAt", startOfDay(summedAt));
        params.put("summedEndAt", endOfDay(summedAt));

        return getSqlSession().selectOne("AlipayCash.getBySummedAt", params);
    }

    /**
     * 更新日汇总提现
     *
     * @param updating  待更新的数据
     * @return 更新是否成功
     */
    public Boolean update(AlipayCash updating) {
        return getSqlSession().update("AlipayCash.update", updating) == 1;
    }
}
