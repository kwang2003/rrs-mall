package com.aixforce.rrs.predeposit.dao;

import com.aixforce.rrs.predeposit.model.DepositAddressStorage;
import com.aixforce.rrs.presale.model.AddressStorage;
import com.google.common.collect.ImmutableMap;
import org.mybatis.spring.support.SqlSessionDaoSupport;
import org.springframework.stereotype.Repository;

/**
 * Author:  <a href="mailto:i@terminus.io">jlchen</a>
 * Date: 2014-07-09
 */
@Repository
public class DepositAddressStorageDao extends SqlSessionDaoSupport {

    public Long create(DepositAddressStorage addressStorage){
        getSqlSession().insert("AddressStorage.create", addressStorage);
        return addressStorage.getId();
    }

    public DepositAddressStorage findByItemIdAndAddressId(Long itemId, Integer addressId){
        return getSqlSession().selectOne("AddressStorage.findByItemIdAndAddressId",
                ImmutableMap.of("itemId", itemId, "addressId", addressId));
    }

    public DepositAddressStorage findById(Long id){
        return getSqlSession().selectOne("AddressStorage.findById", id);
    }

    public boolean changeStorageIdByItemIdAndAddressId(Long itemId, Integer addressId, Long storageId){
        return getSqlSession().update("AddressStorage.update",
                ImmutableMap.of("itemId",itemId,"addressId", addressId, "storageId", storageId))
                == 1;
    }

}
