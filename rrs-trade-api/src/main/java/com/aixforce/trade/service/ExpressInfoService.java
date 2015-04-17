package com.aixforce.trade.service;

import com.aixforce.annotations.ParamInfo;
import com.aixforce.common.model.Paging;
import com.aixforce.common.model.Response;
import com.aixforce.trade.model.ExpressInfo;
import com.aixforce.user.base.BaseUser;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * 快递信息服务
 * Author: haolin
 * On: 9/22/14
 */
public interface ExpressInfoService {

    /**
     * 创建快递信息
     * @param expressInfo 快递信息
     * @return 快递信息id
     */
    Response<Long> create(ExpressInfo expressInfo);

    /**
     * 更新快递信息
     * @param expressInfo 快递信息
     * @return 更新成功返回true, 反之false
     */
    Response<Boolean> update(ExpressInfo expressInfo);

    /**
     * 逻辑删除快递信息
     * @param id 快递信息id
     * @return 删除成功返回true, 反之false
     */
    Response<Boolean> delete(Long id);

    /**
     * 通过名称查询快递信息
     * @param name 快递名称
     * @return ExpressInfo或者Null
     */
    Response<ExpressInfo> findByName(String name);

    /**
     * 获取快递信息列表
     * @param criteria 查询条件
     * @return 快递信息列表
     */
    Response<List<ExpressInfo>> list(
        @ParamInfo("criteria")
        Map<String, Object> criteria
    );

    /**
     * 获取所有启用的快递信息
     * @return 启用的快递信息
     */
    Response<Collection<ExpressInfo>> listEnables(@ParamInfo("user")BaseUser user);

    /**
     * 分页获取快递信息
     * @param pageNo 页号
     * @param pageSize 分页大小
     * @param criteria 查询条件
     * @return 快递分页信息
     */
    Response<Paging<ExpressInfo>> paging(
        @ParamInfo("pageNo")
        Integer pageNo,
        @ParamInfo("pageSize")
        Integer pageSize,
        @ParamInfo("criteria")
        Map<String, Object> criteria
    );

    /**
     * 获取用户常用的快递列表
     * @param user 当前用户
     * @return 常用快递信息列表
     */
    Response<List<ExpressInfo>> usual(@ParamInfo("user") BaseUser user);

    /**
     * 为用户添加常用快递
     * @param userId 用户id
     * @param expressInfoId 常用快递id
     * @return 添加成功返回true, 反之false
     */
    Response<Boolean> add2Usual(Long userId, Long expressInfoId);

    /**
     * 移除用户的常用快递
     * @param userId 用户id
     * @param expressInfoId 常用快递id
     * @return 移除成功返回true, 反之false
     */
    Response<Boolean> rmFromUsual(Long userId, Long expressInfoId);
}
