package com.rrs.coupons.service;

import com.aixforce.common.model.Response;
import com.rrs.coupons.dao.LqCouponDao;
import com.rrs.coupons.dao.RrsCouOrderItemDao;
import com.rrs.coupons.model.RrsCouOrderItem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Created by Administrator on 2014/8/24.
 */
@Service
public class RrsCouOrderItemServiceImpl implements RrsCouOrderItemService {

    @Autowired
    private RrsCouOrderItemDao rrsCouOrderItemDao;

    @Override
    public Response<Boolean> saveCouOrderItem(RrsCouOrderItem rrsCouOrderItem) {
        Response<Boolean> result = new Response<Boolean>();
        try{
            Boolean istrue =  rrsCouOrderItemDao.saveCouOrderItem(rrsCouOrderItem);
            result.setResult(istrue);
            return result;
        }catch(Exception e){
            //log.error("failed to update brand, cause:", e);
            result.setError("brand.update.fail");
            return result;
        }
    }
}
