package com.aixforce.rrs.purify.dao;

import com.aixforce.rrs.purify.model.PurifyAssembly;
import org.mybatis.spring.support.SqlSessionDaoSupport;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Desc:净水组件实体对象信息
 * Mail:v@terminus.io
 * Created by Michael Zhao
 * Date:2014-04-09.
 */
@Repository
public class PurifyAssemblyDao extends SqlSessionDaoSupport {
    /**
     * 创建组件实体信息
     * @param purifyAssembly  组件实体对象
     * @return  Long
     * 返回创建后生成的组件实体编号
     */
    public Long create(PurifyAssembly purifyAssembly){
        getSqlSession().insert("PurifyAssembly.create" , purifyAssembly);
        return purifyAssembly.getId();
    }

    /**
     * 更新组件实体信息
     * @param purifyAssembly  组件实体对象
     * @return  Boolean
     * 返回更新结果
     */
    public Boolean update(PurifyAssembly purifyAssembly){
        return getSqlSession().update("PurifyAssembly.update" , purifyAssembly) == 1;
    }

    /**
     * 删除组件实体信息
     * @param assemblyId  组件实体编号
     * @return  Boolean
     * 返回删除结果
     */
    public Boolean delete(Long assemblyId){
        return getSqlSession().delete("PurifyAssembly.delete" , assemblyId) == 1;
    }

    /**
     * 通过类目编号列表删除组件信息(批量删除时)
     * @param categoryIds 类目编号列表
     * @return  Boolean
     * 返回删除结果
     */
    public Boolean deleteByCategoryIds(Long[] categoryIds){
        return getSqlSession().delete("PurifyAssembly.deleteByCategoryIds", categoryIds) > 0;
    }

    /**
     * 通过编号查询组件实体信息
     * @param id    组件实体编号
     * @return  PurifyAssembly
     * 组件实体对象
     */
    public PurifyAssembly findById(Long id){
        return (PurifyAssembly)getSqlSession().selectOne("PurifyAssembly.findById" , id);
    }

    /**
     * 通过类目编号查询该类目编号下的全部组件实体
     * @param categoryId    类目编号
     * @return  List
     * 返回一个封装好的组件实体信息列表
     */
    public List<PurifyAssembly> findByCategory(Long categoryId){
        return getSqlSession().selectList("PurifyAssembly.findByCategory" , categoryId);
    }

    /**
     * 通过一个类目编号数组查询全部组件实体
     * @param categoryIds 类目编号数组
     * @return List
     * 返回一个封装好的组件实体信息列表
     */
    public List<PurifyAssembly> findByCategoryIds(Long[] categoryIds){
        return getSqlSession().selectList("PurifyAssembly.findByCategoryIds" , categoryIds);
    }

    /**
     * 通过组件链表查询组件信息
     * @param assemblyIds 组件链表
     * @return List
     * 返回一个封装好的组件实体信息列表
     */
    public List<PurifyAssembly> findByAssemblyIds(Long[] assemblyIds){
        return getSqlSession().selectList("PurifyAssembly.findByAssemblyIds" , assemblyIds);
    }

    /**
     * 通过上级组件实体编号查询对应的下级实体编号列表
     * @param parentId    上级组件实体编号
     * @return  List
     * 返回一个封装好的组件实体信息列表
     */
    public List<PurifyAssembly> findByAssembly(Long parentId){
        return getSqlSession().selectList("PurifyAssembly.findByAssembly" , parentId);
    }
}
