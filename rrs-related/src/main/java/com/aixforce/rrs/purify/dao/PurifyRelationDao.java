package com.aixforce.rrs.purify.dao;

import com.aixforce.rrs.purify.model.PurifyRelation;
import com.google.common.collect.ImmutableMap;
import org.mybatis.spring.support.SqlSessionDaoSupport;
import org.springframework.stereotype.Repository;

/**
 * Desc:净水组件上下级关系
 * Mail:v@terminus.io
 * Created by Michael Zhao
 * Date:2014-04-09.
 */
@Repository
public class PurifyRelationDao extends SqlSessionDaoSupport {
    /**
     * 创建组件上下级关系信息
     * @param purifyRelation  组件上下级关系对象
     * @return  Long
     * 返回创建后生成的组件上下级关系编号
     */
    public Long create(PurifyRelation purifyRelation){
        getSqlSession().insert("PurifyRelation.create" , purifyRelation);
        return purifyRelation.getId();
    }

    /**
     * 更新组件上下级关系信息
     * @param purifyRelation  组件上下级关系对象
     * @return  Boolean
     * 返回更新结果
     */
    public Boolean update(PurifyRelation purifyRelation){
        return getSqlSession().update("PurifyRelation.update" , purifyRelation) == 1;
    }

    /**
     * 删除组件上下级关系信息
     * @param relationId  组件上下级关系编号
     * @return  Boolean
     * 返回删除结果
     */
    public Boolean delete(Long relationId){
        return getSqlSession().delete("PurifyRelation.delete" , relationId) == 1;
    }

    /**
     * 通过组件编号列表删除关系信息
     * @param assemblyIds 组件编号列表
     * @return  Boolean
     * 返回删除结果
     */
    public Boolean deleteByAssemblyIds(Long[] assemblyIds){
        return getSqlSession().delete("PurifyRelation.deleteByAssemblyIds", assemblyIds) > 0;
    }

    /**
     * 通过编号查询组件上下级关系信息
     * @param id    组件上下级关系编号
     * @return  PurifyRelation
     * 组件上下级关系对象
     */
    public PurifyRelation findById(Long id){
        return (PurifyRelation)getSqlSession().selectOne("PurifyRelation.findById" , id);
    }

    /**
     * 根据上级组件&下级组件编号确定唯一的Relation
     * @param assemblyParent    上级组件编号
     * @param assemblyChild     下级组件编号
     * @return  PurifyAssembly
     * 返回一个组件关系对象
     */
    public PurifyRelation findRelation(Long assemblyParent , Long assemblyChild){
        return (PurifyRelation)getSqlSession().selectOne("PurifyRelation.findRelation"
                , ImmutableMap.of("assemblyParent" , assemblyParent, "assemblyChild", assemblyChild));
    }
}
