package com.aixforce.rrs.settle.handle;

import com.aixforce.rrs.settle.enums.JobStatus;
import com.aixforce.rrs.settle.model.ItemSettlement;
import com.aixforce.rrs.settle.model.SettleJob;
import com.aixforce.rrs.settle.model.Settlement;
import com.aixforce.shop.model.Shop;
import com.aixforce.user.model.User;
import org.joda.time.DateTime;

import static com.aixforce.common.utils.Arguments.equalWith;

/**
 * Mail: xiao@terminus.io <br>
 * Date: 2014-10-14 4:16 PM  <br>
 * Author: xiao
 */
public abstract class HandleTest {

    protected User getUser(User.TYPE userType) {

        User user = new User();

        if (equalWith(userType, User.TYPE.BUYER)) {
            user.setId(2L);
            user.setName("买家");
        }

        if (equalWith(userType, User.TYPE.SELLER)) {
            user.setId(1L);
            user.setName("卖家");
        }

        return user;
    }

    protected Shop getShop() {
        Shop shop = new Shop();
        shop.setName("店铺");
        shop.setUserId(1L);
        return shop;
    }

   
    protected Settlement getSettlement(Settlement.Type type, Settlement.PayType payType, Settlement.TradeStatus status) {
        Settlement mock = new Settlement();
        mock.setId(1L);
        mock.setSellerName("卖家");
        mock.setSellerId(1L);
        mock.setBuyerName("买家");
        mock.setBuyerId(2L);
        mock.setOrderId(1L);
        mock.setFee(2000L);
        mock.setType(type.value());
        mock.setPayType(payType.value());
        mock.setTradeStatus(status.value());
        // 佣金百分之一
        mock.setCommissionRate(0.01);

        mock.setSettleStatus(Settlement.SettleStatus.ING.value());
        return mock;
    }

    protected ItemSettlement getItemSettlement(ItemSettlement.Type type, Settlement.PayType payType, Settlement.TradeStatus status) {
        ItemSettlement mock = new ItemSettlement();
        mock.setFee(1000L);
        mock.setType(type.value());
        mock.setPayType(payType.value());
        mock.setTradeStatus(status.value());
        mock.setCommissionRate(0.01);

        mock.setSellerName("卖家");
        mock.setSellerId(1L);
        mock.setBuyerName("买家");
        mock.setBuyerId(2L);


        mock.setSettleStatus(Settlement.SettleStatus.ING.value());
        return mock;
    }



    protected SettleJob getSettleJob() {
        SettleJob settleJob = new SettleJob();
        settleJob.setId(1L);
        settleJob.setDoneAt(DateTime.parse("2014-07-14").toDate());
        settleJob.setTradedAt(DateTime.parse("2014-07-13").toDate());
        settleJob.setStatus(JobStatus.NOT.value());
        return settleJob;
    }
}
