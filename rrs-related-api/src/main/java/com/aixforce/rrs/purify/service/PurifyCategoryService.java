package com.aixforce.rrs.purify.service;

import com.aixforce.annotations.ParamInfo;
import com.aixforce.common.model.Response;
import com.aixforce.rrs.purify.model.PurifyCategory;

import java.util.List;

/**
 * Desc:净水类目名称处理
 * Mail:v@terminus.io
 * Created by Michael Zhao
 * Date:2014-04-10.
 */
public interface PurifyCategoryService {
    /**
     * 创建类目信息
     * @param purifyCategory  类目对象
     * @return  Response
     * 返回创建结果
     */
    public Response<Boolean> createCategory(@ParamInfo("purifyCategory")PurifyCategory purifyCategory);

    /**
     * 更新类目信息
     * @param purifyCategory  类目对象
     * @return  Response
     * 返回更新结果
     */
    public Response<Boolean> updateCategory(@ParamInfo("purifyCategory")PurifyCategory purifyCategory);

    /**
     * 通过类目编号删除(以及该类目下的组件与其他组件的关联关系)
     * @param categoryId  类目编号
     * @return Boolean
     * 返回删除结果
     */
    public Response<Boolean> deleteCategory(@ParamInfo("categoryId")Long categoryId);

    /**
     * 通过编号查询类目信息
     * @param categoryId    类目编号
     * @return  Response
     * 返回类目对象
     */
    public Response<PurifyCategory> findById(@ParamInfo("categoryId")Long categoryId);

    /**
     * 通过系列编号&类目名称查询是否存在该类目
     * @param seriesId  系列编号
     * @param categoryName  类目名称
     * @param categoryId    类目编号（用于区分，创建|更新时的判断逻辑.创建时scategoryId=null）
     * @return Response
     * 返回查询结果
     */
    public Response<Boolean> existCategory(@ParamInfo("seriesId")Long seriesId, @ParamInfo("categoryName")String categoryName
            , @ParamInfo("categoryId")Long categoryId);

    /**
     * 通过系列编号查询该序列下的全部组件类目
     * @param seriesId    系列编号
     * @return  Response
     * 返回一个封装好的组件类目信息链表
     */
    public Response<List<PurifyCategory>> findBySeriesId(@ParamInfo("seriesId")Long seriesId);
}
