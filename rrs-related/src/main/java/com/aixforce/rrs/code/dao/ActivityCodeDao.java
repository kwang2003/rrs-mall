package com.aixforce.rrs.code.dao;

import com.aixforce.common.model.Paging;
import com.aixforce.rrs.code.model.ActivityCode;
import com.google.common.collect.ImmutableMap;
import org.mybatis.spring.support.SqlSessionDaoSupport;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * 优惠活动关联优惠码 CRUD
 * Created by wanggen on 14-7-3.
 */
@Repository
public class ActivityCodeDao extends SqlSessionDaoSupport {

    private static final String namespace = "ActivityCode.";


    /**
     * 新增
     *
     * @param activityCode add bean
     * @return 新增后的自增序列号
     */
    public long create(ActivityCode activityCode) {
        getSqlSession().insert(namespace + "create", activityCode);
        return activityCode.getId();
    }


    /**
     * 根据ID查询活动定义信息
     *
     * @return 优惠活动定义
     */
    public ActivityCode findById(Long id) {
        return getSqlSession().selectOne(namespace + "findById", id);
    }


    /**
     * 根据 id 列表查询多条结果
     *
     * @param ids id 列表
     * @return 返回的结果集
     */
    public List<ActivityCode> findByIds(List<Long> ids) {
        return getSqlSession().selectList(namespace + "findByIds", ids);
    }

    /**
     * 分页查询
     *
     * @param param 查询参数
     * @return 分页查询结果
     */
    public Paging<ActivityCode> findByPaging(Map<String, Object> param) {
        Long total = getSqlSession().selectOne(namespace + "countBy", param);
        if (total == null || total == 0)
            return new Paging<ActivityCode>(0L, Collections.<ActivityCode>emptyList());
        if (!param.containsKey("offset"))
            param.put("offset", 0);
        if (!param.containsKey("limit"))
            param.put("limit", total);
        List<ActivityCode> activityCodeList = getSqlSession().selectList(namespace + "findBy", param);
        return new Paging<ActivityCode>(total, activityCodeList);
    }


    /**
     * 根据条件无分页查询
     *
     * @param param 查询条件
     * @return 结果集
     */
    public List<ActivityCode> findAllBy(Map<String, Object> param) {
        return getSqlSession().selectList(namespace + "findBy", param);
    }


    /**
     * 根据优惠活动编号和优惠码号查询 活动-优惠码 信息
     *
     * @param activityId 优惠活动id
     * @param code       优惠码
     * @return 唯一记录
     */
    public ActivityCode findOneByActivityIdAndCode(Long activityId, String code) {
        return getSqlSession().selectOne(namespace + "findOneByActivityIdAndCode",
                ImmutableMap.of("activityId", activityId, "code", code));
    }


    /**
     * 根据优惠码查找
     *
     * @param code 优惠码
     * @return 优惠码信息
     */
    public List<ActivityCode> findByCode(String code) {
        return getSqlSession().selectList(namespace + "findByCode", code);
    }


    /**
     * 根据优惠活动 id 统计该活动所有发放码使用总量
     *
     * @param id 活动 id
     * @return 该活动发放码的使用数量
     */
    public Integer countUsageByActivityId(Long id) {
        return getSqlSession().selectOne(namespace + "countUsageByActivityId", id);
    }

    /**
     * 更新操作
     *
     * @param activityCode 更新操作参数
     * @return 影响行数
     */
    public int update(ActivityCode activityCode) {
        return getSqlSession().update(namespace + "update", activityCode);
    }


    /**
     * 根据 id 更新优惠码使用情况
     *
     * @param param 更新参数
     */
    public int updateUsageById(Map<String, Object> param) {
        return getSqlSession().update(namespace + "updateUsageById", param);
    }


    /**
     * 根据序列 id 删除记录
     *
     * @param ids 序列 id 列表
     * @return 删除行数
     */
    public int deleteByIds(List<Long> ids) {
        return getSqlSession().delete(namespace + "deleteByIds", ids);
    }


    /**
     * 根据优惠活动 ids 删除相关联的 item
     *
     * @param ids 优惠活动 ids
     * @return 删除影响的行数
     */
    public int deleteByActivityIds(List<Long> ids) {
        return getSqlSession().delete(namespace + "deleteByActivityIds", ids);
    }

    public List<Long> findActivityIdsByCode(String code) {
        return getSqlSession().selectList(namespace + "findActivityIdsByCode", code);
    }


    /**
     * 根据优惠活动查询 活动-码 信息
     *
     * @param id 优惠活动ID
     * @return 活动-码 信息
     */
    public List<ActivityCode> findByActivityId(Long id) {
        return getSqlSession().selectList(namespace + "findCodesByActivityId",id);
    }

    public Paging<ActivityCode> findCodesByActivityId(Long activityId, Integer pageNo, Integer count) {
        Long total = getSqlSession().selectOne(namespace + "countCodesByActivityId", activityId);
        if (total == 0) {
            return new Paging<ActivityCode>(0L, Collections.<ActivityCode>emptyList());
        }
        List<ActivityCode> data = getSqlSession().selectList(namespace + "pagingCodesByActivityId", ImmutableMap.of("activityId", activityId, "pageNo", pageNo, "count", count));
        return new Paging<ActivityCode>(total, data);
    }
}
