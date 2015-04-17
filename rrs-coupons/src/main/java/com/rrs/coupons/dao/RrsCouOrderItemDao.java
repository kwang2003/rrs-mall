package com.rrs.coupons.dao;

import com.rrs.coupons.model.RrsCouOrderItem;
import org.mybatis.spring.support.SqlSessionDaoSupport;
import org.springframework.stereotype.Repository;

/**
 * Created by Administrator on 2014/8/24.
 */
@Repository
public class RrsCouOrderItemDao extends SqlSessionDaoSupport {
    public Boolean saveCouOrderItem(RrsCouOrderItem rrsCouOrderItem) {
        return getSqlSession().insert("RrsCouOrderItem.saveCouOrderItem",rrsCouOrderItem) == 1;
    }
}
