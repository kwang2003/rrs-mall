package com.aixforce.alipay.request;

import com.aixforce.alipay.Bank;
import com.aixforce.alipay.dto.*;
import com.aixforce.common.model.Response;
import com.github.kevinsawicki.http.HttpRequest;
import com.google.common.base.Strings;
import com.thoughtworks.xstream.XStream;
import lombok.extern.slf4j.Slf4j;

import java.net.URLEncoder;

import static com.aixforce.common.utils.Arguments.notEmpty;
import static com.aixforce.common.utils.Arguments.notNull;
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;

/**
 * Mail: dong-jie@neusoft.com <br>
 * Date: 2014-11-27 15:16 PM  <br>
 * Author: dong-jie
 */
@Slf4j
public class UnFreezeRequest extends Request {

    private static XStream xStream;

    static {
        xStream = new XStream();
        xStream.autodetectAnnotations(true);
        xStream.processAnnotations(AlipayFreezeSyncResponse.class);
    }

    private UnFreezeRequest(Token token) {
        super(token);
        params.put("service", "alipay.fund.auth.unfreeze");
    }

    public static UnFreezeRequest build(Token token) {
        return new UnFreezeRequest(token);
    }

    /**
     * 支付宝资金授权订单号
     * @param authNo
     * @return
     */
    public UnFreezeRequest authNo (String authNo) {
        checkArgument(notEmpty(authNo), "alipay.pay.authNo.no.empty");
        params.put("auth_no", authNo);
        return this;
    }

    /**
     * 商户请求流水号
     * @return
     */
    public UnFreezeRequest outRequestNo (String outRequestNo) {
        checkArgument(notEmpty(outRequestNo), "alipay.pay.productCode.no.empty");
        params.put("out_request_no", outRequestNo);
        return this;
    }

    /**
     * 业务描述
     * 商户对本次解冻操作的附言描述。长度不超过100个字母或50个汉字。
     * @param remark
     * @return
     */
    public UnFreezeRequest remark (String remark) {
        checkArgument(notEmpty(remark), "alipay.pay.remark.no.empty");
        params.put("remark", remark);
        return this;
    }

    /**
     * 冻结金额
     * @param amount
     * @return
     */
    public UnFreezeRequest amount(Integer amount) {
        checkArgument(notNull(amount), "alipay.pay.amount.empty");
        String fee = DECIMAL_FORMAT.format(amount / 100.0);
        params.put("amount", fee);
        return this;
    }

    public UnFreezeRequest forward(CallBack forward) {                     // 前台返回URL
        if (notNull(forward)) {
            params.put("return_url", forward);
        }
        return this;
    }

    public UnFreezeRequest notify(CallBack notify) {                       // 后台通知URL
        if (notNull(notify)) {
            params.put("notify_url", notify);
        }
        return this;
    }

    public UnFreezeRequest show(CallBack show) {                           // 商品展示URL
        if (notNull(show)) {
            params.put("show_url", show);
        }
        return this;
    }

    public String pay() {
        return super.url();
    }


    /**
     * 默认的银行渠道，如果填写则直接跳转该银行支付页面
     * @param bank 银行
     */
    public UnFreezeRequest defaultBank(Bank bank) {
        if (bank != null) {
            params.put("defaultbank", bank.value());
        }
        return this;
    }

    public UnFreezeRequest enableQrCode() {
        params.put("qr_pay_mode", "2");
        return this;
    }

    @Override
    public void sign() {
        try {
            super.sign();
            String remark = (String)params.get("remark");
            if (!Strings.isNullOrEmpty(remark)) {
                params.put("remark", URLEncoder.encode(remark, "utf-8"));
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 向支付宝网关发送退货请求
     *
     * @return 退货请求结果
     */
    public Response<Boolean> refund() {
        String url = super.url();
        log.info("refund url: {}", url);
        String body = HttpRequest.get(url).connectTimeout(10000).readTimeout(10000).body();
        log.info("refund result: {}", body);
        return convertToFreezeResponse(body);
    }

    protected Response<Boolean> convertToFreezeResponse(String body) {
        Response<Boolean> result = new Response<Boolean>();
        checkState(!Strings.isNullOrEmpty(body), "alipay.refund.fail");

        AlipayFreezeSyncResponse refundResponse = (AlipayFreezeSyncResponse)xStream.fromXML(body);
        if (refundResponse.isSuccess()) {
            result.setResult(Boolean.TRUE);
        } else {
            log.error("refund raise fail: {}", refundResponse.getError());
            result.setError(refundResponse.getError());
        }
        return result;
    }
}
