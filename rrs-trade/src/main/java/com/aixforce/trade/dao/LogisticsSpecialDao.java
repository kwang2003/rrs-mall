package com.aixforce.trade.dao;

import com.aixforce.trade.model.LogisticsSpecial;
import org.mybatis.spring.support.SqlSessionDaoSupport;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Desc:特殊地区的收费操作
 * Mail:v@terminus.io
 * Created by Michael Zhao
 * Date:2014-04-21.
 */
@Repository
public class LogisticsSpecialDao extends SqlSessionDaoSupport {
    /**
     * 创建特殊地区运费信息
     * @param logisticsSpecial  特殊地区运费信息
     * @return Long
     * 返回创建的特殊运费编号
     */
    public Long create(LogisticsSpecial logisticsSpecial){
        getSqlSession().insert("LogisticsSpecial.create" , logisticsSpecial);
        return logisticsSpecial.getId();
    }

    /**
     * 批量创建特殊地区收费信息
     * @param logisticsSpecialList  特殊地区运费集合
     * @return  Integer
     * 返回批量插入的数据条数
     */
    public Integer createBatch(List<LogisticsSpecial> logisticsSpecialList){
        return getSqlSession().insert("LogisticsSpecial.createBatch" , logisticsSpecialList);
    }

    /**
     * 更新特殊地区运费信息
     * @param logisticsSpecial  特殊地区运费信息
     * @return Boolean
     * 返回更新是否成功
     */
    public Boolean update(LogisticsSpecial logisticsSpecial){
        return getSqlSession().update("LogisticsSpecial.update", logisticsSpecial) == 1 ? true : false;
    }

    /**
     * 删除运费模板
     * @param specialId   特殊地区运费信息编号
     * @return  Boolean
     * 返回删除是否成功
     */
    public Boolean delete(Long specialId){
        return getSqlSession().delete("LogisticsSpecial.delete" , specialId) == 1 ? true : false;
    }

    /**
     * 通过模板编号删除模板下的全部特殊地区信息
     * @param modelId   模板编号
     * @return  Boolean
     * 返回删除是否成功
     */
    public Boolean deleteByModelId(Long modelId){
        return getSqlSession().delete("LogisticsSpecial.deleteByModelId" , modelId) > 0 ? true : false;
    }

    /**
     * 通过特殊地区运费信息编号查询信息
     * @param specialId   特殊地区运费信息编号
     * @return  logisticsSpecial
     * 返回模板信息
     */
    public LogisticsSpecial findById(Long specialId){
        return getSqlSession().selectOne("LogisticsSpecial.findById" , specialId);
    }

    /**
     * 通过模板编号查询该模板下的全部特殊地区运费信息
     * @param modelId   模板信息
     * @return  list
     * 返回特殊地区运费信息链表
     */
    public List<LogisticsSpecial> findByModelId(Long modelId){
        return getSqlSession().selectList("LogisticsSpecial.findByModelId" , modelId);
    }
}
