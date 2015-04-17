package com.rrs.coupons.service;

import com.aixforce.common.model.Response;
import com.aixforce.user.base.BaseUser;
import com.rrs.coupons.dao.RrsCouUserDao;
import com.rrs.coupons.model.RrsCouUserView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * Created by yea01 on 2014/8/22.
 */
@Service
public class RrsCouUserServiceImpl implements RrsCouUserService {

    private final static Logger log = LoggerFactory.getLogger(RrsCouUserServiceImpl.class);

    @Autowired
    private RrsCouUserDao rrsCouUserDao;

    @Override
    public Response<List<RrsCouUserView>> queryCouponsAllByUser(BaseUser baseUse,String skus,Long status) {
        //直接判断改订单是否显示优惠劵信息 页面不用直接去判断

        //获取有效的优惠卷信息
        DateFormat format2 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String nowDate = format2.format(new Date());
        Response<List<RrsCouUserView>> result = new Response<List<RrsCouUserView>>();
        try {
            List<RrsCouUserView> brandClubs = rrsCouUserDao.queryCouponsAllByUser(baseUse.getId(),status,nowDate);
            result.setResult(brandClubs);
            return result;
        }catch (Exception e) {
            log.error("failed to find all brand, cause:", e);
            result.setError("brand.query.fail");
            return result;
        }

    }
}
