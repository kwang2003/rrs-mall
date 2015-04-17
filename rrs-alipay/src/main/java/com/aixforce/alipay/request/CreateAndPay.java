package com.aixforce.alipay.request;

import com.aixforce.common.model.Response;
import com.github.kevinsawicki.http.HttpRequest;
import com.google.common.base.Strings;
import java.net.URLEncoder;
import static com.aixforce.common.utils.Arguments.notEmpty;
import static com.aixforce.common.utils.Arguments.notNull;
import static com.google.common.base.Preconditions.checkArgument;

/**
 * Mail: dong-jie@neusoft.com <br>
 * Date: 2014-12-17 15:16 PM  <br>
 * Author: dong-jie
 */
public class CreateAndPay extends Request {

    private CreateAndPay(Token token) {
        super(token);
        // 接口名称 资金授权订单创建并申请冻结接口
        params.put("service", "alipay.acquire.createandpay");
        params.put("alipay_ca_request","2");
    }

    public static CreateAndPay build(Token token) {
        return new CreateAndPay(token);
    }

    /**
     * 商城订单号
     * @param outOrderNo
     * @return
     */
    public CreateAndPay outTradeNo(String outOrderNo) {
        checkArgument(notEmpty(outOrderNo), "alipay.pay.outer.trade.no.empty");
        params.put("out_trade_no", outOrderNo);
        return this;
    }

    /**
     * 支付宝授权号，冻结回调的时候会回传此参数
     * @param autnNo
     * @return
     */
    public CreateAndPay authNo(String autnNo) {
        checkArgument(notEmpty(autnNo), "alipay.pay.outer.autnNo.no.empty");
        params.put("auth_no", autnNo);
        return this;
    }

    /**
     * 买家pid，纯数字
     * @param buyerId
     * @return
     */
    public CreateAndPay buyerId(String buyerId) {
        checkArgument(notEmpty(buyerId), "alipay.pay.outer.buyerId.no.empty");
        params.put("buyer_id", buyerId);
        return this;
    }

    /**
     * 订单业务类型
     * FUND_TRADE_FAST_PAY：预授权产品
     * 当此参数为FUND_TRADE_FAST_PAY时，auth_no不能为空
     * @param productCode
     * @return
     */
    public CreateAndPay productCode (String productCode) {
        checkArgument(notEmpty(productCode), "alipay.pay.productCode.no.empty");
        params.put("product_code", productCode);
        return this;
    }

    /**
     * 订单标题
     * 业务订单的简单描述，如商品名称等。长度不超过100个字母或50个汉字。
     * @param subject
     * @return
     */
    public CreateAndPay subject (String subject) {
        checkArgument(notEmpty(subject), "alipay.pay.orderTitle.no.empty");
        params.put("subject", subject);
        return this;
    }

    /**
     * 支付金额
     * @param totalFee
     * @return
     */
    public CreateAndPay totalFee(Integer totalFee) {
        checkArgument(notNull(totalFee), "alipay.pay.amount.empty");
        String fee = DECIMAL_FORMAT.format(totalFee / 100.0);
        params.put("total_fee", totalFee);
        return this;
    }

    public CreateAndPay notify(CallBack notify) {                       // 后台通知URL
        if (notNull(notify)) {
            params.put("notify_url", notify);
        }
        return this;
    }

    /**
     * 支付url
     * @return
     */
    public String pay() {
        return super.url();
    }

    /**
     * 后台请求
     * @return
     */
    public Response<Boolean> payAuto () {
        String url = super.url();
        String body = HttpRequest.get(url).connectTimeout(10000).readTimeout(10000).body();
        return convertToResponse(body);
    }

    @Override
    public void sign() {
        try {
            super.sign();
            String subject = (String)params.get("subject");
            if (!Strings.isNullOrEmpty(subject)) {
                params.put("subject", URLEncoder.encode(subject, "utf-8"));
            }

            String body = (String)params.get("body");
            if (!Strings.isNullOrEmpty(body)) {
                params.put("body", URLEncoder.encode(body, "utf-8"));
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
