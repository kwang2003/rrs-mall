package com.aixforce.alipay.mpiPay;

import com.huashu.mpi.client.data.OrderData;
import com.huashu.mpi.client.trans.TopPayLink;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by DJs on 14-12-24.
 */
public class MPIRequest {

    private OrderData orderData;

    public MPIRequest() {
        //支付接口参数
        orderData = new OrderData();
        orderData.setAcqInsCd("10000003");                                         //收单ID
        orderData.setTranCode("1010");                                             //交易代码
        //orderData.setMerchantID("10000031");                                      //收单ID
        orderData.setShopCd("1054");                                                 //门店ID
        //orderData.setMerOrderNum(order.getId().toString());                        //商户交易流水号
        Date date = new Date();
        SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmss");
        String str_date = format.format(date);
        orderData.setTranDateTime(str_date);                                        //交易时间
        //orderData.setTranAmt(order.getFee().toString());                           //交易金额
        orderData.setCurType("156");                                               //币种
        //orderData.setCustName(order.getBuyerId().toString());                      //订货人姓名
        //orderData.setProdInfo("冰箱");                                             //商品信息
        orderData.setMsgExt("电子钱包支付");                                      //附加信息
        orderData.setMisc("");                                                     //自定义保留域
    }

    /**
     * 商户ID
     * @param merchantID
     */
    public void setMerchantID (String merchantID) {
        orderData.setMerchantID(merchantID);                                      //商户ID
    }

    /**
     * 订单号
     * @param orderId
     */
    public void setMerOrderNum (String orderId) {
        orderData.setMerOrderNum(orderId);
    }

    /**
     * 支付金额
     * @param totalFee
     */
    public void setTranAmt (String totalFee) {
        orderData.setTranAmt(totalFee);
    }

    /**
     * 商品描述
     * @param title
     */
    public void setProdInfo (String title) {
        orderData.setProdInfo(title);
    }

    /**
     * 详细信息
     * @param content
     */
    public void setMsgExt (String content) {
        orderData.setMsgExt(content);
    }

    /**
     * 加密参数
     * @return
     */
    public String pay () {
        //根据证书加密
        String result = null;
        try {
            result = TopPayLink.PayTrans(orderData);
        } catch (Exception e) {
            //e.printStackTrace();
        }
        return result;
    }

}
