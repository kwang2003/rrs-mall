package com.aixforce.rrs.settle.dao;

import com.aixforce.rrs.settle.model.SettleJob;
import com.google.common.collect.ImmutableMap;
import org.mybatis.spring.support.SqlSessionDaoSupport;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

/**
 * Mail: remindxiao@gmail.com <br>
 * Date: 2014-01-25 11:18 AM  <br>
 * Author: xiao
 */
@Repository
public class SettleJobDao extends SqlSessionDaoSupport{

    /**
     * 创建结算任务
     *
     * @param job 结算任务
     * @return 新记录的id
     */
    public Long create(SettleJob job) {
        getSqlSession().insert("SettleJob.create", job);
        return job.getId();
    }

    /**
     * 标记任务结束
     *
     * @param id    任务标识
     * @param cost  花费的时间
     * @return 标记成功
     */
    public boolean done(Long id, Long cost) {
        return getSqlSession().update("SettleJob.done", ImmutableMap.of("id", id, "cost", cost)) == 1;
    }

    /**
     * 标记任务正在处理
     *
     * @param id 任务标识
     * @return 标记成功
     */
    public boolean ing(Long id) {
        return getSqlSession().update("SettleJob.ing", id) == 1;
    }

    /**
     * 标记任务失败
     *
     * @param id 任务标识
     * @return 标记成功
     */
    public boolean fail(Long id) {
        return getSqlSession().update("SettleJob.fail", id) == 1;
    }


    /**
     * 获取尚未处理的任务清单
     *
     * @return 任务清单
     */
    public List<SettleJob> unfinished() {
        return  getSqlSession().selectList("SettleJob.unfinished");
    }

    /**
     * 根据ID获取任务
     *
     * @return 任务信息
     */
    public SettleJob get(Long id) {
        return getSqlSession().selectOne("SettleJob.get", id);
    }

    /**
     * 获取任务
     *
     * @param doneAt    处理时间
     * @param type   任务类型
     * @return  任务
     */
    public SettleJob getByDoneAtAndJobType(Date doneAt, Integer type) {
        return getSqlSession().selectOne("SettleJob.getByDoneAtAndJobType", ImmutableMap.of("doneAt", doneAt , "type", type));
    }

    /**
     * 标记任务处理完毕
     *
     * @param id 任务标识
     */
    public boolean success(Long id) {
        return getSqlSession().update("SettleJob.success", id) > 0;
    }

    /**
     * 查看当前任务是否已经创建
     * @param startAt   查询处理的开始时间
     * @param endAt     查询处理的截止时间
     *
     * @return true-已创建 false-未创建
     */
    public boolean check(Date startAt, Date endAt) {
        Long count = getSqlSession().selectOne("SettleJob.countOf",
                ImmutableMap.of("doneStartAt", startAt, "doneEndAt", endAt));
        return count > 0L;
    }
}
