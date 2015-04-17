package com.aixforce.trade.service;

import com.aixforce.annotations.ParamInfo;
import com.aixforce.common.model.Paging;
import com.aixforce.common.model.Response;
import com.aixforce.trade.model.InstallInfo;
import java.util.List;
import java.util.Map;

/**
 * 物流安装信息服务
 * Author: haolin
 * On: 9/22/14
 */
public interface InstallInfoService {

    /**
     * 创建物流安装信息
     * @param installInfo 安装信息
     * @return 安装信息id
     */
    Response<Long> create(InstallInfo installInfo);

    /**
     * 更新物流安装信息
     * @param installInfo 安装信息
     * @return 更新成功返回true, 反之false
     */
    Response<Boolean> update(InstallInfo installInfo);

    /**
     * 逻辑删除安装信息
     * @param id 安装信息id
     * @return 删除成功返回true, 反之false
     */
    Response<Boolean> delete(Long id);

    /**
     * 通过名称查询安装信息
     * @param name 安装公司名称
     * @return InstallInfo或者Null
     */
    Response<InstallInfo> findByName(String name);

    /**
     * 获取安装信息列表
     * @param criteria 查询条件
     * @return 安装信息列表
     */
    Response<List<InstallInfo>> list(
        @ParamInfo("criteria")
        Map<String, Object> criteria
    );

    /**
     * 获取所有启用的安装信息
     * @return 启用的安装信息
     */
    Response<List<InstallInfo>> listEnables(Integer type);

    /**
     * 分页获取快递信息
     * @param pageNo 页号
     * @param pageSize 分页大小
     * @param criteria 查询条件
     * @return 安装分页信息
     */
    Response<Paging<InstallInfo>> paging(
        @ParamInfo("pageNo")
        Integer pageNo,
        @ParamInfo("pageSize")
        Integer pageSize,
        @ParamInfo("criteria")
        Map<String, Object> criteria
    );
}
