package com.aixforce.alipay.request;

import com.aixforce.alipay.Bank;
import com.google.common.base.Strings;
import org.aspectj.weaver.ast.Call;

import java.net.URLEncoder;

import static com.aixforce.common.utils.Arguments.notEmpty;
import static com.aixforce.common.utils.Arguments.notNull;
import static com.google.common.base.Preconditions.checkArgument;

/**
 * Mail: remindxiao@gmail.com <br>
 * Date: 2014-03-25 11:40 AM  <br>
 * Author: xiao
 */
public class PayRequest extends Request {

    private PayRequest(Token token) {
        super(token);
        params.put("service", "create_direct_pay_by_user");
        params.put("payment_type", "1");

    }

    public static PayRequest build(Token token) {
        return new PayRequest(token);
    }



    public PayRequest forward(CallBack forward) {                     // 前台返回URL
        if (notNull(forward)) {
            params.put("return_url", forward);
        }
        return this;
    }

    public PayRequest notify(CallBack notify) {                       // 后台通知URL
        if (notNull(notify)) {
            params.put("notify_url", notify);
        }
        return this;
    }

    public PayRequest show(CallBack show) {                           // 商品展示URL
        if (notNull(show)) {
            params.put("show_url", show);
        }
        return this;
    }

    public PayRequest title(String title) {                           // 收银台上显示的商品标题
        if (title != null) {
            params.put("subject", title);
        }
        return this;
    }

    public PayRequest paymentType(String type) {                      // 支付方式, 默认为1
        if (Strings.isNullOrEmpty(type)) {
            params.put("payment_type", "1");
        } else {
            params.put("payment_type", type);
        }
        return this;
    }

    public PayRequest content(String content) {                       // 商品内容
        if (notEmpty(content)) {
            params.put("body", content);
        }

        return this;
    }


    public PayRequest outerTradeNo(String outerTradeNo) {
        checkArgument(notEmpty(outerTradeNo), "alipay.pay.outer.trade.no.empty");
        params.put("out_trade_no", outerTradeNo);
        return this;
    }

    public PayRequest total(Integer total) {
        checkArgument(notNull(total), "alipay.pay.total.empty");
        String fee = DECIMAL_FORMAT.format(total / 100.0);
        params.put("total_fee", fee);
        return this;
    }



    public String pay() {
        return super.url();
    }


    /**
     * 默认的银行渠道，如果填写则直接跳转该银行支付页面
     * @param bank 银行
     */
    public PayRequest defaultBank(Bank bank) {
        if (bank != null) {
            this.payModeController(bank.value());
        }
        return this;
    }

    public PayRequest enableQrCode() {
        params.put("qr_pay_mode", "2");
        return this;
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

    /**
     * 根据选择的支付方式，跳转到不同银行
     * @param bank
     */
    private void payModeController (String bank) {
        if ("ALIPAY".equals(bank)) {
            params.remove("qr_pay_mode");
        } else if ("SCAN-ALIPAY".equals(bank)) {
            params.put("qr_pay_mode", "0");
            CallBack forward = (CallBack)params.get("return_url");
            String returnUrl = forward.getUrl() + "-qrCode";
            CallBack forwardQRCODE = new CallBack(returnUrl);
            params.put("return_url", forwardQRCODE);
        } else if (bank.contains("-CCIP")) {
            params.put("defaultbank", bank);
            params.put("default_login","Y");
            params.put("paymethod", "CCIP");
        } else {
            params.put("defaultbank", bank);
            params.remove("qr_pay_mode");
        }
    }
}
