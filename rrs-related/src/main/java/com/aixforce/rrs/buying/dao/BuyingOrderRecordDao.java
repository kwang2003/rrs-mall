package com.aixforce.rrs.buying.dao;

import com.aixforce.common.model.Paging;
import com.aixforce.rrs.buying.model.BuyingOrderRecord;
import com.google.common.collect.ImmutableMap;
import org.mybatis.spring.support.SqlSessionDaoSupport;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

/**
 * Created by songrenfei on 14-9-22.
 */

@Repository
public class BuyingOrderRecordDao extends SqlSessionDaoSupport {

    public Long create(BuyingOrderRecord buyingOrderRecord) {
        getSqlSession().insert("BuyingOrderRecord.create", buyingOrderRecord);
        return buyingOrderRecord.getId();
    }

    public boolean delete(Long id) {
        return getSqlSession().delete("BuyingOrderRecord.delete", id) == 1;
    }

    public boolean update(BuyingOrderRecord buyingOrderRecord) {
        return getSqlSession().update("BuyingOrderRecord.update", buyingOrderRecord) == 1;
    }

    public BuyingOrderRecord findById(Long id) {
        return getSqlSession().selectOne("BuyingOrderRecord.findById", id);
    }

    public List<BuyingOrderRecord> findByActivityId(Long activityId) {
        return getSqlSession().selectList("BuyingOrderRecord.findByActivityId", activityId);
    }

    public Paging<BuyingOrderRecord> getByActivityId(Map<String, Object> param) {
        Long total = getSqlSession().selectOne("BuyingOrderRecord.countByActivityId", param);
        if (total == 0) {
            return Paging.empty(BuyingOrderRecord.class);
        }

        List<BuyingOrderRecord> cuList = getSqlSession().selectList("BuyingOrderRecord.getByActivityId",  param);

        return new Paging<BuyingOrderRecord>(total, cuList);
    }

    /**
     * 更新总订单的code
     * @param oldId
     * @param newId
     */
    public void updateOrderId(Long oldId, Long newId) {
        if(oldId==null||newId==null){
            return;
        }
        getSqlSession().update("BuyingOrderRecord.updateOrderId", ImmutableMap.of("oldId", oldId, "newId", newId));
    }
}
