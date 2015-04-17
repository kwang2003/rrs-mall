package com.aixforce.admin.web.controller;


import com.aixforce.common.model.Response;
import com.aixforce.user.base.BaseUser;
import com.aixforce.user.base.UserUtil;
import com.rrs.coupons.model.RrsCou;
import com.rrs.coupons.service.CouponsManageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.text.SimpleDateFormat;
import java.util.List;

/**
 * Created by yea01 on 2014/12/1.
 */
@Controller
@RequestMapping("/api/Seller/Coupons")
public class SellerCouponsController {

    @Autowired
    private CouponsManageService couponsManageService;

    @RequestMapping(value = "/findAllCoupons", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Response<String> queryCouponsBy() {
        Response<String> result = new Response<String>();
        BaseUser baseUser = UserUtil.getCurrentUser();
        if(baseUser==null){
            result.setResult("404");
        }else {
            long userId = baseUser.getId();
            List<RrsCou> list = couponsManageService.findAllRrsCou(userId,0);
            if (list != null && list.size() != 0) {
                StringBuilder stb = new StringBuilder();
                for (RrsCou rrsCou : list) {
                    int tempTerm = rrsCou.getTerm() / 100;
                    int tempAmount = rrsCou.getAmount();
                    SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:MM:ss");
                    String startDate = df.format(rrsCou.getStartTime());
                    String endDate = df.format(rrsCou.getEndTime());
                    String createAt = df.format(rrsCou.getCreated_at());
                    String status = rrsCou.getStatus() == 0 ? "未生效" : rrsCou.getStatus() == 1 ? "暂停" : rrsCou.getStatus() == 2 ? "生效" : rrsCou.getStatus() == 3 ? "失效" : "获取状态失败";
                }
                result.setResult(stb.toString());
            } else {
                result.setResult("405");
            }
        }
        return result;
    }
}
