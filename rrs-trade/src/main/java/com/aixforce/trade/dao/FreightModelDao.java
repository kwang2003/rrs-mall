package com.aixforce.trade.dao;

import com.aixforce.common.model.Paging;
import com.aixforce.trade.model.FreightModel;
import com.google.common.base.Objects;
import com.google.common.collect.Maps;
import org.mybatis.spring.support.SqlSessionDaoSupport;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Desc:运费模板处理
 * Mail:v@terminus.io
 * Created by Michael Zhao
 * Date:2014-04-21.
 */
@Repository
public class FreightModelDao extends SqlSessionDaoSupport {
    /**
     * 创建运费模板
     * @param freightModel  运费模板
     * @return Long
     * 返回创建的运费模板编号
     */
    public Long create(FreightModel freightModel){
        getSqlSession().insert("FreightModel.create" , freightModel);
        return freightModel.getId();
    }

    /**
     * 更新运费模板
     * @param freightModel  运费模板
     * @return Boolean
     * 返回更新是否成功
     */
    public Boolean update(FreightModel freightModel){
        return getSqlSession().update("FreightModel.update" , freightModel) == 1;
    }

    /**
     * 删除运费模板
     * @param modelId   模板编号
     * @return  Boolean
     * 返回删除是否成功
     */
    public Boolean delete(Long modelId){
        return getSqlSession().delete("FreightModel.delete" , modelId) == 1;
    }

    /**
     * 通过模板编号查询信息
     * @param modelId   模板编号
     * @return  FreightModel
     * 返回模板信息
     */
    public FreightModel findById(Long modelId){
        return getSqlSession().selectOne("FreightModel.findById" , modelId);
    }

    /**
     * 通过商家编号全部模板信息
     * @param sellerId  商家编号
     * @return  List
     * 返回全部模板信息
     */
    public List<FreightModel> findBySellerId(Long sellerId){
        return getSqlSession().selectList("FreightModel.findBySellerId" , sellerId);
    }

    /**
     * 通过卖家编号&删选参数查询需要查询的数据
     * @param sellerId  商家编号
     * @param params    查询参数数据
     * @return  List
     * 返回一个运费模板信息链表
     */
    public Paging<FreightModel> findByParams(Long sellerId , Map<String , Object> params){
        params.put("sellerId" , sellerId);
        Long total = getSqlSession().selectOne("FreightModel.countByParams", params);
        if(Objects.equal(total, 0L)) {
            return new Paging<FreightModel>(0L, Collections.<FreightModel>emptyList());
        }
        List<FreightModel> freightModels = getSqlSession().selectList("FreightModel.findByParams" , params);
        return new Paging<FreightModel>(total, freightModels);
    }

    /**
     * 通过商家编号&模板名称查询是否存在该模板
     * @param sellerId  商家编号
     * @param modelName 模板名称
     * @param modelId   模板编号（用于更新是判断参数）
     * @return  FreightModel
     * 返回模板对象
     */
    public FreightModel existModel(Long sellerId, String modelName, Long modelId){
        Map<String , Object> params = Maps.newHashMap();
        params.put("sellerId" , sellerId);
        params.put("modelName" , modelName);
        params.put("modelId", modelId);
        return getSqlSession().selectOne("FreightModel.existModel" , params);
    }
}
