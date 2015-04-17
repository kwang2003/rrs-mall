package com.aixforce.alipay.request;

import com.aixforce.alipay.Bank;
import com.google.common.base.Strings;

import java.net.URLEncoder;

import static com.aixforce.common.utils.Arguments.notEmpty;
import static com.aixforce.common.utils.Arguments.notNull;
import static com.google.common.base.Preconditions.checkArgument;

/**
 * Mail: dong-jie@neusoft.com <br>
 * Date: 2014-11-27 15:16 PM  <br>
 * Author: dong-jie
 */
public class FreezePayRequest extends Request {

    private FreezePayRequest(Token token) {
        super(token);
        // 接口名称 资金授权订单创建并申请冻结接口
        params.put("service", "alipay.fund.auth.create.freeze.apply");
        // 商城登陆账号
        params.put("payee_logon_id", token.getAccount());
        // 商城账号ID
        params.put("payee_user_id", token.getPid());
        // 支付终端为PC（必填）可选参数 ：PC / WIRELESS（默认）
        params.put("pay_mode", "PC");
        // 二维码扫描方式支付 ？ 沙箱环境无效？
        this.enableQrCode();
    }

    public static FreezePayRequest build(Token token) {
        return new FreezePayRequest(token);
    }

    /**
     * 商城订单号
     * @param outOrderNo
     * @return
     */
    public FreezePayRequest outOrderNo(String outOrderNo) {
        checkArgument(notEmpty(outOrderNo), "alipay.pay.outer.trade.no.empty");
        params.put("out_order_no", outOrderNo);
        return this;
    }

    /**
     * 商户请求流水号
     * @return
     */
    public FreezePayRequest outRequestNo (String outRequestNo) {
        checkArgument(notEmpty(outRequestNo), "alipay.pay.productCode.no.empty");
        params.put("out_request_no", outRequestNo);
        return this;
    }

    /**
     * 业务产品码
     * 对应销售产品，商户签约时由支付宝统一分配。
     * @param productCode
     * @return
     */
    public FreezePayRequest productCode (String productCode) {
        checkArgument(notEmpty(productCode), "alipay.pay.productCode.no.empty");
        params.put("product_code", productCode);
        return this;
    }

    /**
     * 业务场景码
     * 对应业务场景，商户签约时由支付宝统一分配。
     * @param sceneCode
     * @return
     */
    public FreezePayRequest sceneCode (String sceneCode) {
        checkArgument(notEmpty(sceneCode), "alipay.pay.sceneCode.no.empty");
        params.put("scene_code", sceneCode);
        return this;
    }

    /**
     * 订单标题
     * 业务订单的简单描述，如商品名称等。长度不超过100个字母或50个汉字。
     * @param orderTitle
     * @return
     */
    public FreezePayRequest orderTitle (String orderTitle) {
        checkArgument(notEmpty(orderTitle), "alipay.pay.orderTitle.no.empty");
        params.put("order_title", orderTitle);
        return this;
    }

    /**
     * 冻结金额
     * @param amount
     * @return
     */
    public FreezePayRequest amount(Integer amount) {
        checkArgument(notNull(amount), "alipay.pay.amount.empty");
        String fee = DECIMAL_FORMAT.format(amount / 100.0);
        params.put("amount", fee);
        return this;
    }

    /**
     * 业务到期时间
     * 订单到期时间，只做展示用，到期支付宝不自动解冻剩余资金。 格式：YYYY-MM-DD HH:MM。
     * @param expireTime
     * @return
     */
    public FreezePayRequest expireTime (String expireTime) {
        params.put("expire_time", expireTime);
        return this;
    }

    public FreezePayRequest forward(CallBack forward) {                     // 前台返回URL
        if (notNull(forward)) {
            params.put("return_url", forward);
        }
        return this;
    }

    public FreezePayRequest notify(CallBack notify) {                       // 后台通知URL
        if (notNull(notify)) {
            params.put("notify_url", notify);
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
    public FreezePayRequest defaultBank(Bank bank) {
        if (bank != null) {
            params.put("defaultbank", bank.value());
        }
        return this;
    }

    public FreezePayRequest enableQrCode() {
        params.put("qr_pay_mode", "2");
        return this;
    }

    @Override
    public void sign() {
        try {
            super.sign();

            String orderTilte = (String)params.get("order_title");
            if (!Strings.isNullOrEmpty(orderTilte)) {
                params.put("order_title", URLEncoder.encode(orderTilte, "utf-8"));
            }

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
