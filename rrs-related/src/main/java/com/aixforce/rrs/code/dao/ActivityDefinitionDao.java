package com.aixforce.rrs.code.dao;

import com.aixforce.common.model.Paging;
import com.aixforce.rrs.code.model.ActivityDefinition;
import org.mybatis.spring.support.SqlSessionDaoSupport;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * 优惠活动信息表 CRUD
 * Created by wanggen on 14-7-3.
 */
@Repository
public class ActivityDefinitionDao extends SqlSessionDaoSupport{

    private static final String namespace = "ActivityDefinition.";


    /**
     * 新增
     * @param activityDefinition add bean
     * @return 新增后的自增序列号
     */
    public long create(ActivityDefinition activityDefinition){
        getSqlSession().insert(namespace+"create", activityDefinition);
        return activityDefinition.getId();
    }


    /**
     * 根据ID查询活动定义信息
     * @return  优惠活动定义
     */
    public ActivityDefinition findById(Long id) {
        return getSqlSession().selectOne(namespace+"findById", id);
    }

    /**
     * 根据ID查询活动定义信息
     * @return  优惠活动定义
     */
    public List<ActivityDefinition> findByIds(List<Long> ids) {
        return getSqlSession().selectList(namespace+"findByIds", ids);
    }

    /**
     * 根据ID查询有效活动定义信息
     * @return  优惠活动定义
     */
    public List<ActivityDefinition> findValidByIds(List<Long> ids) {
        return getSqlSession().selectList(namespace+"findValidByIds", ids);
    }


    /**
     * 分页查询
     * @param param 查询参数
     * @return  分页查询结果
     */
    public Paging<ActivityDefinition> findByPaging(Map<String, Object> param){
        Long total = getSqlSession().selectOne(namespace+"countBy", param);
        if(total==null || total==0)
            return new Paging<ActivityDefinition>(0L, Collections.<ActivityDefinition>emptyList());
        if(!param.containsKey("offset"))
            param.put("offset",0);
        if(!param.containsKey("limit"))
            param.put("limit", total);
        List<ActivityDefinition> activityDefinitions = getSqlSession().selectList(namespace+"findBy", param);
        return new Paging<ActivityDefinition>(total, activityDefinitions);
    }


    /**
     * 普通查询，无分页
     * @return 结果列表
     */
    public List<ActivityDefinition> findAllBy(Map<String, Object> param){
        return getSqlSession().selectList(namespace+"findBy", param);
    }


    /**
     * 更新操作
     * @param activityDefinition 更新操作参数
     * @return 影响行数
     */
    public int update(ActivityDefinition activityDefinition){
        return getSqlSession().update(namespace+"update", activityDefinition);
    }


    /**
     * 根据序列 id 列表删除记录
     * @param ids 序列 ids
     * @return 删除行数
     */
    public int deleteByIds(List<Long> ids){
        return getSqlSession().delete(namespace+"deleteByIds", ids);
    }

    /**
     * 计划任务自动生效
     * @return
     */
    public Boolean updateToEffect(){
        return  getSqlSession().update(namespace+"updateToEffect")>0;
    }

    /**
     * 计划任务自动失效
     * @return
     */
    public Boolean updateToExpiry(){
        return  getSqlSession().update(namespace+"updateToExpiry")>0;
    }

    /**
     * 手动失效
     * @param id 活动id
     * @return
     */
    public Boolean updateToExpiryByHand(Long id){
        return getSqlSession().update(namespace+"updateToExpiryByHand",id)==1;
    }

    /**
     * 将 activity_definitions 中字段 order_count 加 1
     * @param activityId  这个优惠活动的
     */
    public void addOrderUsedCount(Long activityId) {
        getSqlSession().update(namespace+"addOrderUsedCount", activityId);
    }
}
