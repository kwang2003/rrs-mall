package com.aixforce.rrs.settle.dao;

import com.aixforce.common.model.Paging;
import com.aixforce.rrs.settle.model.DailySettlement;
import com.google.common.collect.Maps;
import org.mybatis.spring.support.SqlSessionDaoSupport;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static com.aixforce.common.utils.Dates.endOfDay;
import static com.aixforce.common.utils.Dates.startOfDay;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;


/**
 * Mail: remindxiao@gmail.com <br>
 * Date: 2014-01-22 10:33 AM  <br>
 * Author: xiao
 */
@Repository
public class DailySettlementDao extends SqlSessionDaoSupport {


    /**
     * 创建日结算汇总记录
     * @param dailySettlement   日结算汇总记录
     * @return  新纪录id
     */
    public Long create(DailySettlement dailySettlement) {
        checkNotNull(dailySettlement.getConfirmedAt());

        Map<String, Object> params = Maps.newHashMapWithExpectedSize(3);
        params.put("confirmedStartAt", startOfDay(dailySettlement.getConfirmedAt()));
        params.put("confirmedEndAt", endOfDay(dailySettlement.getConfirmedAt()));

        Long count = getSqlSession().selectOne("DailySettlement.countOf", params);
        checkState(count == 0L, "daily.settlement.record.duplicate");

        getSqlSession().insert("DailySettlement.create", dailySettlement);
        return dailySettlement.getId();
    }



    /**
     * 根据起止日期分页查询日结算汇总信息
     * @param confirmedStartAt  起始日期
     * @param confirmedEndAt    截止日期
     * @param offset            偏移值
     * @param limit             返回记录数
     * @return  分页查询结果
     */
    public Paging<DailySettlement> findBy(Date confirmedStartAt, Date confirmedEndAt, Integer offset, Integer limit) {
        Map<String,Object> params = Maps.newHashMapWithExpectedSize(3);
        params.put("confirmedStartAt", confirmedStartAt);
        params.put("confirmedEndAt", confirmedEndAt);
        params.put("offset", offset);
        params.put("limit", limit);
        return findBy(params);
    }


    public Paging<DailySettlement> findBy(Map<String, Object> params) {
        Long count = getSqlSession().selectOne("DailySettlement.countOf", params);

        if(count == 0L){
            return new Paging<DailySettlement>(0L, Collections.<DailySettlement>emptyList());
        }

        List<DailySettlement> r = getSqlSession().selectList("DailySettlement.findBy",params);
        return new Paging<DailySettlement>(count, r);
    }

    /**
     * 根据标识获取日结信息
     *
     * @param id    标识
     * @return      日结算信息
     */
    public DailySettlement get(Long id) {
         return getSqlSession().selectOne("DailySettlement.get", id);
    }


}
