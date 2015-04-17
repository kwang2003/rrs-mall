package com.aixforce.alipay.mpiPay;

import com.huashu.mpi.client.data.OrderData;
import com.huashu.mpi.client.trans.TopPayLink;

/**
 * Created by DJs on 14-12-24.
 */
public class MPIPayCallBack {

    public String checkPaySuccess (String mpiRes) {
        try {
            OrderData resBuff = new OrderData();
            resBuff = TopPayLink.ConvXml2OrderData(mpiRes, resBuff);
            String code = resBuff.getRespCode();
            if ("0000".equals(code)) {
                // 支付成功，返回订单ID
                String orderId = resBuff.getMerOrderNum();
                return orderId;
            } else {
                // 支付失败，直接返回failed
                return "failed";
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "failed";
    }
}
