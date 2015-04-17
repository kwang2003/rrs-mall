package com.aixforce.trade.dao;

import com.aixforce.common.model.Paging;
import com.aixforce.common.utils.JsonMapper;
import com.aixforce.trade.model.InstallInfo;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import org.mybatis.spring.support.SqlSessionDaoSupport;
import org.springframework.stereotype.Repository;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * 物流安装信息Dao实现
 * Author: haolin
 * On: 9/22/14
 */
@Repository
public class InstallInfoDao extends SqlSessionDaoSupport {

    /**
     * 创建安装信息
     * @param installInfo 安装信息
     * @return 创建记录数
     */
    public Integer create(InstallInfo installInfo){
        return getSqlSession().insert("InstallInfo.create", installInfo);
    }

    /**
     * 批量创建安装信息
     * @param installInfos 安装信息列表
     * @return 创建记录数
     */
    public Integer creates(List<InstallInfo> installInfos) {
        return getSqlSession().insert("InstallInfo.creates", installInfos);
    }

    /**
     * 更新安装信息
     * @param installInfo 安装信息
     * @return 更新记录数
     */
    public Integer update(InstallInfo installInfo){
        return getSqlSession().update("InstallInfo.update", installInfo);
    }

    /**
     * 删除安装信息(逻辑删除)
     * @param id 安装信息id
     * @return 删除记录数
     */
    public Integer delete(Long id){
        return getSqlSession().update("InstallInfo.delete", id);
    }

    /**
     * 获取安装信息
     * @param id 安装信息id
     * @return 安装信息
     */
    public InstallInfo load(Long id){
        return getSqlSession().selectOne("InstallInfo.load", id);
    }

    /**
     * 通过名称查找安装信息
     * @param name 安装公司名称
     * @return InstallInfo或者Null
     */
    public InstallInfo findByName(String name){
        return getSqlSession().selectOne("InstallInfo.findByName", name);
    }

    /**
     * 通过多个名称查找安装信息
     * @param names 安装公司名称列表
     * @return InstallInfo列表
     */
    public List<InstallInfo> findByNames(List<String> names) {
        return getSqlSession().selectOne("InstallInfo.findByNames", names);
    }

    /**
     * 获取所有安装信息列表
     * @return 所有安装信息列表
     */
    public List<InstallInfo> listAll(){
        return list(null);
    }

    /**
     * 获取安装信息列表
     * @param criteria 查询条件
     * @return 安装信息列表
     */
    public List<InstallInfo> list(Map<String, Object> criteria){
        return getSqlSession().selectList("InstallInfo.list", criteria);
    }

    /**
     * 分页获取快递信息
     * @param offset 起始记录
     * @param limit 分页大小
     * @param criteria 查询条件
     * @return 安装信息分页对象
     */
    public Paging<InstallInfo> paging(Integer offset, Integer limit, Map<String, Object> criteria){
        // get total count
        Long total = getSqlSession().selectOne("InstallInfo.count", criteria);
        if (total <= 0){
            return new Paging<InstallInfo>(0L, Collections.<InstallInfo>emptyList());
        }
        criteria.put("offset", offset);
        criteria.put("limit", limit);
        // get data
        List<InstallInfo> datas = getSqlSession().selectList("InstallInfo.paging", criteria);
        return new Paging<InstallInfo>(total, datas);
    }

    /**
     * 批量更新状态
     * @param ids 安装信息id列表
     * @param status 状态
     * @return 更新记录数
     */
    public Integer updatesStatus(List<Long> ids, int status) {
        return getSqlSession().update("InstallInfo.updatesStatus", ImmutableMap.of("ids", ids, "status", status));
    }
}
