package com.aixforce.rrs.purify.service;

import com.aixforce.annotations.ParamInfo;
import com.aixforce.common.model.Response;
import com.aixforce.rrs.purify.model.PurifyRelation;

/**
 * Desc:净水组件上下级关系处理
 * Mail:v@terminus.io
 * Created by Michael Zhao
 * Date:2014-04-10.
 */
public interface PurifyRelationService {
    /**
     * 创建组件上下级关系信息
     * @param purifyRelation  组件上下级关系对象
     * @return  Response
     * 返回创建结果
     */
    public Response<Boolean> createRelation(@ParamInfo("purifyRelation")PurifyRelation purifyRelation);

    /**
     * 更新组件上下级关系信息
     * @param purifyRelation  组件上下级关系对象
     * @return  Response
     * 返回更新结果
     */
    public Response<Boolean> updateRelation(@ParamInfo("purifyRelation")PurifyRelation purifyRelation);

    /**
     * 通过编号查询组件上下级关系信息
     * @param relationId  组件上下级关系编号
     * @return  Response
     * 返回关系对象
     */
    public Response<PurifyRelation> findById(@ParamInfo("relationId")Long relationId);

    /**
     * 根据上级组件&下级组件编号确定唯一的Relation
     * @param assemblyParent    上级组件编号
     * @param assemblyChild     下级组件编号
     * @return  Response
     * 返回一个组件关系对象
     */
    public Response<PurifyRelation> findRelation(@ParamInfo("assemblyParent")Long assemblyParent , @ParamInfo("assemblyChild")Long assemblyChild);
}
