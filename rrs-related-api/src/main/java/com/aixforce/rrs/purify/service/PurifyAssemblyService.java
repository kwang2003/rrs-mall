package com.aixforce.rrs.purify.service;

import com.aixforce.annotations.ParamInfo;
import com.aixforce.common.model.Response;
import com.aixforce.rrs.purify.model.PurifyAssembly;

import java.util.List;

/**
 * Desc:净水组件实体处理
 * Mail:v@terminus.io
 * Created by Michael Zhao
 * Date:2014-04-10.
 */
public interface PurifyAssemblyService {
    /**
     * 创建组件实体信息
     * @param parentId          上级组件编号(可以为null:表示没有关联，不需要创建关系)
     * @param purifyAssembly    组件对象
     * @param productId         产品编号（可以为null：表示为绑定到最终的商品）
     * @return  Response
     * 返回创建是否成功
     */
    public Response<Boolean> createAssembly(@ParamInfo("parentId")Long parentId,
                                         @ParamInfo("purifyAssembly")PurifyAssembly purifyAssembly, @ParamInfo("productId")Long productId);

    /**
     * 更新组件实体信息
     * @param purifyAssembly  组件实体对象
     * @return  Response
     * 返回更新结果
     */
    public Response<Boolean> updateAssembly(@ParamInfo("purifyAssembly")PurifyAssembly purifyAssembly);

    /**
     * 通过组件编号删除
     * @param assemblyId  组件编号
     * @return Boolean
     * 返回删除结果
     */
    public Response<Boolean> deleteAssembly(@ParamInfo("assemblyId")Long assemblyId);

    /**
     * 通过编号查询组件实体信息
     * @param assemblyId  组件实体编号
     * @return  Response
     * 返回组件实体对象
     */
    public Response<PurifyAssembly> findById(@ParamInfo("assemblyId")Long assemblyId);

    /**
     * 通过类目编号查询该类目编号下的全部组件实体
     * @param categoryId    类目编号
     * @return  Response
     * 返回一个封装好的组件实体信息列表
     */
    public Response<List<PurifyAssembly>> findByCategory(@ParamInfo("categoryId")Long categoryId);

    /**
     * 通过上级组件实体编号查询对应的下级实体编号列表
     * @param parentId    上级组件实体编号
     * @return  List
     * 返回一个封装好的组件实体信息列表
     */
    public Response<List<PurifyAssembly>> findByAssembly(@ParamInfo("parentId")Long parentId);
}
