package com.aixforce.trade.dao;

import com.aixforce.common.model.Paging;
import com.aixforce.common.utils.JsonMapper;
import com.aixforce.trade.model.ExpressInfo;
import com.google.common.collect.Maps;
import org.mybatis.spring.support.SqlSessionDaoSupport;
import org.springframework.stereotype.Repository;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * 物流快递信息Dao实现
 * Author: haolin
 * On: 9/22/14
 */
@Repository
public class ExpressInfoDao extends SqlSessionDaoSupport {

    /**
     * 创建快递信息
     * @param expressInfo 快递信息
     * @return 创建记录数
     */
    public Integer create(ExpressInfo expressInfo){
        return getSqlSession().insert("ExpressInfo.create", expressInfo);
    }

    /**
     * 更新快递信息
     * @param expressInfo 快递信息
     * @return 更新记录数
     */
    public Integer update(ExpressInfo expressInfo){
        return getSqlSession().update("ExpressInfo.update", expressInfo);
    }

    /**
     * 删除快递信息(逻辑删除)
     * @param id 快递信息id
     * @return 删除记录数
     */
    public Integer delete(Long id){
        return getSqlSession().update("ExpressInfo.delete", id);
    }

    /**
     * 获取快递信息
     * @param id 快递信息id
     * @return 快递信息
     */
    public ExpressInfo load(Long id){
        return getSqlSession().selectOne("ExpressInfo.load", id);
    }

    /**
     * 通过名称查找快递信息
     * @param name 快递名称
     * @return ExpressInfo或者Null
     */
    public ExpressInfo findByName(String name){
        return getSqlSession().selectOne("ExpressInfo.findByName", name);
    }

    /**
     * 通过名称查找快递信息
     * @param code 快递代码
     * @return ExpressInfo或者Null
     */
    public ExpressInfo findByCode(String code){
        return getSqlSession().selectOne("ExpressInfo.findByCode", code);
    }

    /**
     * 获取所有快递信息列表
     * @return 所有快递信息列表
     */
    public List<ExpressInfo> listAll(){
        return list(null);
    }

    /**
     * 获取快递信息列表
     * @param criteria 查询条件
     * @return 快递信息列表
     */
    public List<ExpressInfo> list(Map<String, Object> criteria){
        return getSqlSession().selectList("ExpressInfo.list", criteria);
    }

    /**
     * 分页获取快递信息
     * @param offset 起始记录
     * @param limit 分页大小
     * @param criteria 查询条件
     * @return 快递信息分页对象
     */
    public Paging<ExpressInfo> paging(Integer offset, Integer limit, Map<String, Object> criteria){

        // get total count
        Long total = getSqlSession().selectOne("ExpressInfo.count", criteria);
        if (total <= 0){
            return new Paging<ExpressInfo>(0L, Collections.<ExpressInfo>emptyList());
        }
        criteria.put("offset", offset);
        criteria.put("limit", limit);
        // get data
        List<ExpressInfo> datas = getSqlSession().selectList("ExpressInfo.paging", criteria);
        return new Paging<ExpressInfo>(total, datas);
    }

    /**
     * 查询多个id的快递信息
     * @param expressInfoIds 快递信息id列表
     * @return 快递信息列表
     */
    public List<ExpressInfo> loads(List<Long> expressInfoIds) {
        return getSqlSession().selectList("ExpressInfo.loads", expressInfoIds);
    }

    /**
     * 通过代码和接口查找快递信息
     * @param expressInfo 快递信息
     * @return 快递信息
     */
    public ExpressInfo findByCodeAndInterface(ExpressInfo expressInfo) {
        return getSqlSession().selectOne("ExpressInfo.findByCodeAndInterface", expressInfo);
    }
}
