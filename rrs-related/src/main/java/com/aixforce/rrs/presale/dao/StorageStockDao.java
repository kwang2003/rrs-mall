package com.aixforce.rrs.presale.dao;

import com.aixforce.rrs.presale.model.StorageStock;
import com.google.common.collect.ImmutableMap;
import org.mybatis.spring.support.SqlSessionDaoSupport;
import org.springframework.stereotype.Repository;

/**
 * Author:  <a href="mailto:i@terminus.io">jlchen</a>
 * Date: 2014-07-09
 */
@Repository
public class StorageStockDao extends SqlSessionDaoSupport {

    public StorageStock findByItemIdAndStorageId(Long itemId, Long storageId){
        return  getSqlSession().selectOne("StorageStock.findByItemIdAndStorageId",
                ImmutableMap.of("itemId", itemId, "storageId", storageId));
    }

    public boolean changeSoldCount(Integer delta, Long itemId, Long storageId){
        return getSqlSession().update("StorageStock.changeSoldCount",
                ImmutableMap.of("delta",delta, "itemId", itemId, "storageId", storageId )) == 1;
    }

    public Long create(StorageStock storageStock){
        getSqlSession().insert("StorageStock.create", storageStock);
        return storageStock.getId();
    }

    public StorageStock findById(Long id){
        return getSqlSession().selectOne("StorageStock.findById", id);
    }

    public boolean changeInitStockAndSoldCount(StorageStock storageStock){
        return getSqlSession().update("StorageStock.update", storageStock) == 1;
    }
}
