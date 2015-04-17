package com.aixforce.rrs.purify.dao;

import com.aixforce.rrs.purify.model.PurifyCategory;
import com.google.common.collect.ImmutableMap;
import org.mybatis.spring.support.SqlSessionDaoSupport;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Desc:净水选择类目
 * Mail:v@terminus.io
 * Created by Michael Zhao
 * Date:2014-04-09.
 */
@Repository
public class PurifyCategoryDao extends SqlSessionDaoSupport {
    /**
     * 创建类目信息
     * @param purifyCategory  类目对象
     * @return  Long
     * 返回创建后生成的类目编号
     */
    public Long create(PurifyCategory purifyCategory){
        getSqlSession().insert("PurifyCategory.create" , purifyCategory);
        return purifyCategory.getId();
    }

    /**
     * 更新类目信息
     * @param purifyCategory  类目对象
     * @return  Boolean
     * 返回更新结果
     */
    public Boolean update(PurifyCategory purifyCategory){
        return getSqlSession().update("PurifyCategory.update" , purifyCategory) == 1;
    }

    /**
     * 删除类目信息
     * @param categoryId  类目编号
     * @return  Boolean
     * 返回删除结果
     */
    public Boolean delete(Long categoryId){
        return getSqlSession().delete("PurifyCategory.delete", categoryId) == 1;
    }

    /**
     * 通过系列编号列表删除类目信息
     * @param seriesId 系列编号列表
     * @return  Boolean
     * 返回删除结果
     */
    public Boolean deleteBySeriesIds(Long[] seriesId){
        return getSqlSession().delete("PurifyCategory.deleteBySeriesIds", seriesId) > 0;
    }

    /**
     * 通过编号查询类目信息
     * @param id    类目编号
     * @return  PurifyCategory
     * 类目对象
     */
    public PurifyCategory findById(Long id){
        return (PurifyCategory)getSqlSession().selectOne("PurifyCategory.findById" , id);
    }

    /**
     * 查询某个系列下的最大深度
     * @param seriesId  系列编号
     * @return PurifyCategory
     * 最深的类目对象
     */
    public PurifyCategory findMaxStage(Long seriesId){
        return (PurifyCategory)getSqlSession().selectOne("PurifyCategory.findMaxStage" , seriesId);
    }

    /**
     * 通过系列编号查询默认的第一个类目
     * @param seriesId  系列编号
     * @return PurifyCategory
     * 默认的类目对象
     */
    public PurifyCategory findDefaultBySeriesId(Long seriesId){
        return (PurifyCategory)getSqlSession().selectOne("PurifyCategory.findDefaultBySeriesId" , seriesId);
    }

    /**
     * 通过系列编号以及类目名称查询该类目是否已经存在了
     * @param seriesId      序列编号
     * @param categoryName  类目名称
     * @return  List
     * 返回类目列表
     */
    public List<PurifyCategory> findByName(Long seriesId , String categoryName){
        return getSqlSession().selectList("PurifyCategory.findByName" , ImmutableMap.of("seriesId" , seriesId, "categoryName", categoryName));
    }

    /**
     * 通过系列编号查询该序列下的全部组件类目
     * @param seriesId    系列编号
     * @return  List
     * 返回一个封装好的组件类目信息链表
     */
    public List<PurifyCategory> findBySeriesId(Long seriesId){
        return getSqlSession().selectList("PurifyCategory.findBySeriesId" , seriesId);
    }

    /**
     * 通过类目编号数组查询全部的类目详细信息
     * @param categoryIds   类目数组
     * @return  List
     * 返回类目详细信息列表
     */
    public List<PurifyCategory> findByCategoryIds(Long[] categoryIds){
        return getSqlSession().selectList("PurifyCategory.findByCategoryIds" , categoryIds);
    }

    /**
     * 通过组件编号查询类目对象
     * @param assemblyIds   组件编号
     * @return List
     * 返回类目详细信息列表
     */
    public List<PurifyCategory> findByAssemblyIds(Long[] assemblyIds){
        return getSqlSession().selectList("PurifyCategory.findByAssemblyIds" , assemblyIds);
    }
}
