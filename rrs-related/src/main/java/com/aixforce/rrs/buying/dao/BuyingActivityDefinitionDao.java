package com.aixforce.rrs.buying.dao;

import com.aixforce.common.model.Paging;
import com.aixforce.rrs.buying.model.BuyingActivityDefinition;
import org.mybatis.spring.support.SqlSessionDaoSupport;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Created by songrenfei on 14-9-22.
 */

@Repository
public class BuyingActivityDefinitionDao extends SqlSessionDaoSupport {

    public Long create(BuyingActivityDefinition buyingActivityDefinition) {
        getSqlSession().insert("BuyingActivityDefinition.create", buyingActivityDefinition);
        return buyingActivityDefinition.getId();
    }

    public boolean delete(Long id) {
        return getSqlSession().delete("BuyingActivityDefinition.delete", id) == 1;
    }

    public boolean update(BuyingActivityDefinition buyingActivityDefinition) {
        return getSqlSession().update("BuyingActivityDefinition.update", buyingActivityDefinition) == 1;
    }

    public BuyingActivityDefinition findById(Long id) {
        return getSqlSession().selectOne("BuyingActivityDefinition.findById", id);
    }

    public Paging<BuyingActivityDefinition> paging(Map<String, Object> param){

        Long total = getSqlSession().selectOne("BuyingActivityDefinition.count", param);
        if(total==null||total==0){
            return Paging.empty(BuyingActivityDefinition.class);
        }
        List<BuyingActivityDefinition> buyingActivityDefinitionList =getSqlSession().selectList("BuyingActivityDefinition.paging",param);

        return new Paging<BuyingActivityDefinition>(total,buyingActivityDefinitionList);
    }

    public Boolean updateToRuning(Date now){
         getSqlSession().update("BuyingActivityDefinition.updateToRuning",now);
        return true;
    }

    public Boolean updateToFinish(Date now){
        getSqlSession().update("BuyingActivityDefinition.updateToFinish",now);
        return true;
    }

    public List<BuyingActivityDefinition> findAboutToStop(Date now) {
        return getSqlSession().selectList("BuyingActivityDefinition.findAboutToStop", now);
    }

    public Boolean updateToStop(Date now) {
        getSqlSession().update("BuyingActivityDefinition.updateToStop", now);
        return true;
    }

}
