/*
 * Copyright (c) 2012 杭州端点网络科技有限公司
 */

package com.aixforce.item.service;

import com.aixforce.annotations.ParamInfo;
import com.aixforce.common.model.Paging;
import com.aixforce.common.model.Response;
import com.aixforce.item.model.TitleKeyword;

import java.util.List;
import java.util.Map;

/**
 * @desc 对查询页面中进行搜索的关键字进行落库，并提供对这些记录的 insert/delete/update/select 操作接口<BR>
 *       表 title_keyword 记录用户在页面搜索中使用过的关键字
 * @author wanggen
 * @date 2014-06-30
 */
public interface TitleKeywordService {

    /**
     * 新增一条在该页面进行搜索的关键字的记录
     *
     * @param  titleKeyword 页面-关键字实体
     * @return 新增的条数
     */
    Response<Long> create(TitleKeyword titleKeyword);

    /**
     * 根据条件删除记录
     *
     * @param   id 删除记录的id
     * @return  删除的记录数
     */
    Response<Long> deleteById(Long id);


    /**
     * 根据唯一 ID 查询记录
     *
     * @param   id  唯一ID
     * @return  返回的唯一记录
     */
    Response<TitleKeyword> findById(Long id);

    /**
     *  根据 nameId 查询一条记录
     * @param   nameId 业务标识ID
     * @return  查询出的唯一记录
     */
    Response<TitleKeyword> findByNameId(Long nameId);

    Response<TitleKeyword> findByPath(String path);

    /**
     * 根据查询条件查询结果集
     *
     * @return  查询返回的结果集
     */
    Response<Paging<TitleKeyword>> findAll(@ParamInfo("params")Map<String, Object> params,
                                           @ParamInfo("pageNo") Integer pageNo,
                                           @ParamInfo("count") Integer count);


    /**
     * 根据实体 Bean 更新记录，以 id，或 name_id 为查询条件,
     * 传入参数 id 不会为空，name_id 可为空
     *
     * @param   updateParam 更新传入参数
     * @return  更新的条数
     */
    Response<Long> update(TitleKeyword updateParam);

    /**
     * 根据fcid获得友情链接
     * @param fcid
     * @return
     */
    List<Map> findByFcid(@ParamInfo("fcid") Long fcid);
}
