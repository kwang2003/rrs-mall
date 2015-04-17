package com.aixforce.rrs.predeposit.dao;

import com.aixforce.rrs.predeposit.model.DepositStorageStock;
import com.aixforce.rrs.presale.model.StorageStock;
import com.google.common.collect.ImmutableMap;
import org.mybatis.spring.support.SqlSessionDaoSupport;
import org.springframework.stereotype.Repository;

/**
 * Author:  <a href="mailto:i@terminus.io">jlchen</a>
 * Date: 2014-07-09
 */
@Repository
public class DepositStorageStockDao extends SqlSessionDaoSupport {

    public DepositStorageStock findByItemIdAndStorageId(Long itemId, Long storageId){
        return  getSqlSession().selectOne("StorageStock.findByItemIdAndStorageId",
                ImmutableMap.of("itemId", itemId, "storageId", storageId));
    }

    public boolean changeSoldCount(Integer delta, Long itemId, Long storageId){
        return getSqlSession().update("StorageStock.changeSoldCount",
                ImmutableMap.of("delta",delta, "itemId", itemId, "storageId", storageId )) == 1;
    }

    public Long create(DepositStorageStock storageStock){
        getSqlSession().insert("StorageStock.create", storageStock);
        return storageStock.getId();
    }

    public DepositStorageStock findById(Long id){
        return getSqlSession().selectOne("StorageStock.findById", id);
    }

    public boolean changeInitStockAndSoldCount(StorageStock storageStock){
        return getSqlSession().update("StorageStock.update", storageStock) == 1;
    }
}
