package com.aixforce.alipay.wxPay;

import com.aixforce.alipay.dto.AlipaySyncResponse;
import com.aixforce.common.model.Response;
import com.google.common.base.Strings;
import com.google.common.base.Throwables;
import com.thoughtworks.xstream.XStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.SortedMap;
import java.util.TreeMap;

import static com.google.common.base.Preconditions.checkState;

/**
 * Created by DJs on 15-3-12.
 */
public class WXPayRefund extends WXPay{

    private final static Logger log = LoggerFactory.getLogger(WXPayRefund.class);

    private static XStream xStream;

    static {
        xStream = new XStream();
        xStream.autodetectAnnotations(true);
        xStream.processAnnotations(WXRefundResponse.class);
    }

    /**
     * 密钥
     * @param key
     * @return
     */
    public WXPayRefund key (String key) {
        params.put("key", key);
        return this;
    }

    /**
     * 退款网关
     * @param refundGateway
     * @return
     */
    public WXPayRefund refundGateway (String refundGateway) {
        params.put("refundGateway", refundGateway);
        return this;
    }

    /**
     * 证书路径
     * @param pfxPath
     * @return
     */
    public WXPayRefund pfxPath (String pfxPath) {
        params.put("pfxPath", pfxPath);
        return this;
    }

    /**
     * appId
     * @param appId
     * @return
     */
    public WXPayRefund appId (String appId) {
        params.put("appId", appId);
        return this;
    }

    /**
     * appSecret
     * @param appSecret
     * @return
     */
    public WXPayRefund appSecret (String appSecret) {
        params.put("appSecret", appSecret);
        return this;
    }

    /**
     * 签名类型 默认md5
     * @param signType
     * @return
     */
    public WXPayRefund signType (String signType) {
        params.put("sign_type", signType);
        return this;
    }

    /**
     * 字符集 默认utf-8
     * @param inputCharset
     * @return
     */
    public WXPayRefund inputCharset (String inputCharset) {
        params.put("input_charset", inputCharset);
        return this;
    }

    /**
     * 版本号
     * 填写为1.0 时，操作员密码为明文
     * 填写为1.1 时，操作员密码为MD5(密码)值
     * @param service_Version
     * @return
     */
    public WXPayRefund serviceVersion (String service_Version) {
        params.put("service_version", service_Version);
        return this;
    }

    /**
     * 商户号
     * @param partner
     * @return
     */
    public WXPayRefund partner (String partner) {
        params.put("partner", partner);
        return this;
    }

    /**
     * 商户订单号， out_trade_no 和 transaction_id 至少一个必填，同时存在时 transaction_id 优先
     * @param outTradeNo
     * @return
     */
    public WXPayRefund outTradeNo (String outTradeNo) {
        params.put("out_trade_no", outTradeNo);
        return this;
    }

    /**
     * 财付通交易号  out_trade_no 和 transaction_id 至少一个必填，同时存在时 transaction_id 优先
     * @param transactionId
     * @return
     */
    public WXPayRefund transactionId (String transactionId) {
        params.put("transaction_id", transactionId);
        return this;
    }

    /**
     * 商户退款单号，32 个字符内、可包含字母,确保在商户系统唯一
     * @param outRefundNo
     * @return
     */
    public WXPayRefund outRefundNo (String outRefundNo) {
        params.put("out_refund_no", outRefundNo);
        return this;
    }

    /**
     * 订单总金额，单位为分
     * @param totalFee
     * @return
     */
    public WXPayRefund totalFee (String totalFee) {
        params.put("total_fee", totalFee);
        return this;
    }

    /**
     * 退款总金额,单位为分,可以做部分退款
     * @param refundFee
     * @return
     */
    public WXPayRefund refundFee (String refundFee) {
        params.put("refund_fee", refundFee);
        return this;
    }

    /**
     * 操作员帐号,默认为商户号
     * @param opUserId
     * @return
     */
    public WXPayRefund opUserId (String opUserId) {
        params.put("op_user_id", opUserId);
        return this;
    }

    /**
     * 操作员密码,默认为商户后台登录密码
     * @param opUserPasswd
     * @return
     */
    public WXPayRefund opUserPasswd (String opUserPasswd) {
        params.put("op_user_passwd", opUserPasswd);
        return this;
    }

    /**
     * 转账退款接收退款的财付通帐号
     * 可空
     * @param recvUserId
     * @return
     */
    public WXPayRefund recvUserId (String recvUserId) {
        params.put("recv_user_id", recvUserId);
        return this;
    }

    /**
     * 转账退款接收退款的姓名
     * 可空
     * @param reccvUserName
     * @return
     */
    public WXPayRefund reccvUserName (String reccvUserName) {
        params.put("reccv_user_name", reccvUserName);
        return this;
    }

    /**
     * 空
     * @param useSpbillNoFlag
     * @return
     */
    public WXPayRefund useSpbillNoFlag (String useSpbillNoFlag) {
        params.put("use_spbill_no_flag", useSpbillNoFlag);
        return this;
    }

    public WXPayRefund refundType (String refundType) {
        params.put("refund_type", refundType);
        return this;
    }

    public Response<Boolean> wechatRefund () {
        Response<Boolean> response = new Response<Boolean>();
        SortedMap<String, String> packageParams = new TreeMap<String, String>();
        packageParams.put("appid", (String)params.get("appId"));
        packageParams.put("mch_id", (String) params.get("partner"));
        packageParams.put("nonce_str", System.currentTimeMillis() + "");
        packageParams.put("op_user_id", (String) params.get("partner"));
        packageParams.put("out_refund_no", (String) params.get("out_refund_no"));
        packageParams.put("out_trade_no", "");
        packageParams.put("refund_fee", (String) params.get("refund_fee"));
        packageParams.put("total_fee", (String) params.get("total_fee"));
        packageParams.put("transaction_id", (String) params.get("transaction_id"));

        RequestHandler reqHandler = new RequestHandler(
                null, null);
        String appId = (String)params.get("appId");
        String appSecret = (String)params.get("appSecret");
        String partnerKey = (String)params.get("key");
        reqHandler.init(appId, appSecret, partnerKey);

        String sign = reqHandler.createSign(packageParams);

        String xml = "<xml>"
                    + "<appid>" +  packageParams.get("appid") + "</appid>"
                    + "<mch_id>" + packageParams.get("mch_id") + "</mch_id>"
                    + "<nonce_str>" + packageParams.get("nonce_str") + "</nonce_str>"
                    + "<op_user_id>"+ packageParams.get("mch_id") +"</op_user_id>"
                    + "<out_refund_no>" + packageParams.get("out_refund_no") + "</out_refund_no>"
                    + "<out_trade_no>" + packageParams.get("out_trade_no") + "</out_trade_no>"
                    + "<refund_fee>" + packageParams.get("refund_fee") + "</refund_fee>"
                    + "<total_fee>" + packageParams.get("total_fee") + "</total_fee>"
                    + "<transaction_id>" + packageParams.get("transaction_id") + "</transaction_id>"
                    + "<sign><![CDATA[" + sign + "]]></sign>"
                    + "</xml>";
        log.info("wx pay xml = {}", xml);
        String createOrderURL = (String)params.get("refundGateway");
        try {
            String xmlBody = ClientCustomSSL.doRefund(createOrderURL, xml, (String)params.get("pfxPath"), (String)params.get("partner"));
            log.info("wx pay refund return xml body = {}", xmlBody);
            Response<Boolean> checkRefundStatus = this.convertToResponse(xmlBody);
            if (!checkRefundStatus.isSuccess()) {
                response.setError(checkRefundStatus.getError());
                return response;
            }
        } catch (Exception e) {
            log.error(Throwables.getStackTraceAsString(e));
            response.setError(e.getMessage());
            return response;
        }
        response.setResult(true);
        return response;
    }

    /**
     * xml解析
     * @param body
     * @return
     */
    private Response<Boolean> convertToResponse (String body) {
        Response<Boolean> response = new Response<Boolean>();
        checkState(!Strings.isNullOrEmpty(body), "wx.refund.fail");

        WXRefundResponse wxRefundResponse = (WXRefundResponse)xStream.fromXML(body);
        log.info("wx refund return code = {}.", wxRefundResponse.getReturnCode());
        if ("SUCCESS".equals(wxRefundResponse.getReturnCode())) {
            if ("SUCCESS".equals(wxRefundResponse.getResultCode())) {
                response.setResult(Boolean.TRUE);
            } else {
                log.error("wx pay refund fail code {}, cause {}.", wxRefundResponse.getErrCode(), wxRefundResponse.getErrCodeDes());
                response.setError("wx pay refund fail,error code " +  wxRefundResponse.getErrCode() + ", cause " + wxRefundResponse.getErrCodeDes());
            }
        } else {
           log.error("connect to wx refund server fail, cause {}.",wxRefundResponse.getReturnMsg());
            response.setError("connect to wx refund server fail, cause " + wxRefundResponse.getReturnMsg());
        }
        return response;
    }
}
