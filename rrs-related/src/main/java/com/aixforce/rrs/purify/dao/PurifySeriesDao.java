package com.aixforce.rrs.purify.dao;

import com.aixforce.rrs.purify.model.PurifySeries;
import com.google.common.collect.ImmutableMap;
import org.mybatis.spring.support.SqlSessionDaoSupport;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Desc:净水系列
 * Mail:v@terminus.io
 * Created by Michael Zhao
 * Date:2014-04-09.
 */
@Repository
public class PurifySeriesDao extends SqlSessionDaoSupport {
    /**
     * 创建系列信息
     * @param purifySeries  系列对象
     * @return  Long
     * 返回创建后生成的系列编号
     */
    public Long create(PurifySeries purifySeries){
        getSqlSession().insert("PurifySeries.create" , purifySeries);
        return purifySeries.getId();
    }

    /**
     * 更新系列信息
     * @param purifySeries  系列对象
     * @return  Boolean
     * 返回更新结果
     */
    public Boolean update(PurifySeries purifySeries){
        return getSqlSession().update("PurifySeries.update" , purifySeries) == 1;
    }

    /**
     * 删除系列信息
     * @param seriesId  系列编号
     * @return  Boolean
     * 返回删除结果
     */
    public Boolean delete(Long seriesId){
        return getSqlSession().delete("PurifySeries.delete" , seriesId) == 1;
    }

    /**
     * 通过系列编号列表删除系列信息
     * @param seriesIds 系列编号列表
     * @return  Boolean
     * 返回删除结果
     */
    public Boolean deleteBySeriesIds(Long[] seriesIds){
        return getSqlSession().delete("PurifySeries.deleteBySeriesIds", seriesIds) > 0;
    }

    /**
     * 通过编号查询系列信息
     * @param id    系列编号
     * @return  PurifySeries
     * 系列对象
     */
    public PurifySeries findById(Long id){
        return (PurifySeries)getSqlSession().selectOne("PurifySeries.findById" , id);
    }

    /**
     * 通过站点编号以及系列名称查询该系列是否存在
     * @param siteId        站点编号
     * @param seriesName    系列名称
     * @return  List
     * 返回系列列表
     */
    public List<PurifySeries> findByName(Long siteId , String seriesName){
        return getSqlSession().selectList("PurifySeries.findByName" , ImmutableMap.of("siteId", siteId, "seriesName", seriesName));
    }

    /**
     * 通过站点编号查询该站点下的全部系列信息
     * @param siteId    站点编号
     * @return  List
     * 返回一个封装好的系列信息链表
     */
    public List<PurifySeries> findSiteSeries(Long siteId){
        return getSqlSession().selectList("PurifySeries.findSiteSeries" , siteId);
    }
}
