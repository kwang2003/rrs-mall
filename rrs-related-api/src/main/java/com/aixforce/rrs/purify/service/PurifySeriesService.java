package com.aixforce.rrs.purify.service;

import com.aixforce.annotations.ParamInfo;
import com.aixforce.common.model.Response;
import com.aixforce.rrs.purify.model.PurifySeries;

import java.util.List;

/**
 * Desc:净水系列名称处理
 * Mail:v@terminus.io
 * Created by Michael Zhao
 * Date:2014-04-10.
 */
public interface PurifySeriesService {

    /**
     * 创建系列信息
     * @param purifySeries  系列对象
     * @return  Response
     * 返回创建结果
     */
    public Response<Boolean> createPurifySeries(@ParamInfo("purifySeries")PurifySeries purifySeries);

    /**
     * 更改系列信息
     * @param purifySeries  系列对象
     * @return Response
     * 返回更改结果
     */
    public Response<Boolean> updatePurifySeries(@ParamInfo("purifySeries")PurifySeries purifySeries);

    /**
     * 通过系列编号查询系列详细信息
     * @param seriesId  系列编号
     * @return  Response
     * 返回系列信息
     */
    public Response<PurifySeries> findById(@ParamInfo("seriesId")Long seriesId);

    /**
     * 通过站点编号&系列名称查询在该站点下是否一存在这个系列
     * @param siteId        站点编号
     * @param seriesName    系列名称
     * @param seriesId      系列编号（用于区分，创建|更新时的判断逻辑.创建时seriesId=null）
     * @return  Response
     * 返回系列是否存在
     */
    public Response<Boolean> existSeries(@ParamInfo("siteId")Long siteId, @ParamInfo("seriesName")String seriesName, @ParamInfo("seriesId")Long seriesId);

    /**
     * 通过系列编号删除该系列对象
     * @param seriesId  系列编号
     * @return  Response
     * 返回删除结果
     */
    public Response<Boolean> deletePurifySeries(@ParamInfo("seriesId")Long seriesId);

    /**
     * 查询某个站点下的全部系列名称
     * @param siteId    站点编号(当siteId=null，查询全部系列名称)
     * @return  Response
     * 返回查询结果
     */
    public Response<List<PurifySeries>> findSiteSeries(@ParamInfo("siteId")Long siteId);
}
