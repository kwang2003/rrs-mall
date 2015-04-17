package com.rrs.coupons.service;

import com.aixforce.common.model.Response;
import com.aixforce.user.base.BaseUser;
import com.google.common.base.Objects;
import com.rrs.coupons.dao.LqCouponDao;
import com.rrs.coupons.model.LqCouponView;
import com.rrs.coupons.model.LqMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * Created by zhua02 on 2014/8/21.
 */
@Service
public class LqCouponServiceImpl implements LqCouponService {

    @Autowired
    private LqCouponDao lqdao;

    @Override
    public Response<List<LqCouponView>> findCouponAll() {
        Response<List<LqCouponView>> result = new Response<List<LqCouponView>>();
        SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = new Date();
        result.setResult(lqdao.findCouponAll(sf.format(date)));
        return result;
    }

    @Override
    public LqMessage LqCoupon(BaseUser baseUser,int couponId) {
        LqMessage lqm=new LqMessage();
        int userlimit = lqdao.findUserLimit(couponId);//单个用户限领数额
        int sendnum = lqdao.findSendNum(couponId);//发券的总数量
        int usecount = lqdao.findUseCount(couponId);//该券已经领取
        int uucount = lqdao.findUserUseCount(couponId,baseUser.getId().intValue());//用户领取数量
//        if(usecount < sendnum && uucount < userlimit){
//            lqdao.addUserCoupon(couponId,baseUser.getId().intValue());
//            lqdao.updateCouponReceive(couponId);
//        }

        if (Objects.equal(sendnum, 0)) {//发券数量为0 不限制数量
            if (Objects.equal(userlimit, 0)) {//用户限领取数量为 0 时 不做限制

            }else{
                if(uucount >= userlimit){
                    lqm.setStatus("3");
                    lqm.setMessage("你的领取数已经达到最大数");
                    return lqm;
                }
            }
            lqdao.addUserCoupon(couponId, baseUser.getId().intValue());
            lqdao.updateCouponReceive(couponId);
            lqm.setStatus("4");
            lqm.setMessage("恭喜你领取成功");
            return lqm;
        }else{
            if (Objects.equal(userlimit, 0)) {//用户限领取数量为 0 时 不做限制

            }else{
                if(uucount >= userlimit){
                    lqm.setStatus("3");
                    lqm.setMessage("你的领取数已经达到最大数");
                    return lqm;
                }
            }
            if(usecount >= sendnum){
                lqm.setStatus("2");
                lqm.setMessage("优惠券已被领完");
                return lqm;
            }else if(usecount < sendnum){
                lqdao.addUserCoupon(couponId,baseUser.getId().intValue());
                lqdao.updateCouponReceive(couponId);
                lqm.setStatus("4");
                lqm.setMessage("恭喜你领取成功");
                return lqm;
            }else{
                lqm.setStatus("5");
                lqm.setMessage("优惠券过期或不存在");
                return lqm;
            }
        }
//        if(usecount >= sendnum){
//            lqm.setStatus("2");
//            lqm.setMessage("优惠券已被领完");
//            return lqm;
//        }else if(uucount >= userlimit){
//            lqm.setStatus("3");
//            lqm.setMessage("你的领取数已经达到最大数");
//            return lqm;
//        }else if(usecount < sendnum && uucount < userlimit){
//            lqdao.addUserCoupon(couponId,baseUser.getId().intValue());
//            lqdao.updateCouponReceive(couponId);
//            lqm.setStatus("4");
//            lqm.setMessage("恭喜你领取成功");
//            return lqm;
//        }else{
//            lqm.setStatus("5");
//            lqm.setMessage("优惠券过期或不存在");
//            return lqm;
//        }
    }
}
