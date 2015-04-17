package com.aixforce.rrs.jde;

import com.aixforce.rrs.settle.model.DepositFee;
import com.google.common.base.Objects;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.io.Serializable;
import java.text.DecimalFormat;
import java.util.Date;

/**
 *
 * 与JDE对接时的数据模型
 *
 * Mail: remindxiao@gmail.com <br>
 * Date: 2014-02-11 3:55 PM  <br>
 * Author: xiao
 */
@XStreamAlias("data")
@ToString
public class Jde implements Serializable{

    private static final long serialVersionUID = -7011098165480755942L;


    private static final String PREFIX = "R";

    private static final DateTimeFormatter dft = DateTimeFormat.forPattern("yyyy-MM-dd");
    private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("0.##");

    private static XStream xStream;

    static {
        xStream = new XStream();
        xStream.autodetectAnnotations(true);
        xStream.setMode(XStream.NO_REFERENCES);
        xStream.processAnnotations(Jde.class);
    }

    //批次号:
    //长度15位
    //第一位R 表示由RRS.COM生成
    //第二位根据业务

    //1 商户收入(费用单)
    //2 佣金及第三方(支付宝)手续费(费用单)
    //3 积分
    //4 预售金
    //5 退保证金(对账)
    //6 退货款(对账)
    //7 缴纳保证金(对账)
    //8 技术服务费(订单)
    //9 技术服务费(对账)
    //a 商户支付宝日提现(对账)
    //b 商户日汇总订单金额(对账)
    //c 基础金提现(对账)
    //d 扣除保证金(对账)


    //后以订单结算记录ID区分，中间补0
    //如果订单的id 13,则支付宝日提现的批次号 Ra0000000000013
    @Getter
    @Setter
    @XStreamAlias("serial")
    private String serial;

    //业务类型:
    //1、商户收入(费用单) 1FK
    //2、佣金及第三方(支付宝)手续费(费用单) 1FYD
    //3、积分 待定
    //4、预售金 4YDJ
    //5、退保证金(对账) 2TYJ
    //6、退货款(对账) 2THK
    //7、缴纳保证金(对账) 3JCFDZ
    //8、技术服务费(订单) 3JCFDD
    //9、技术服务费(对账) 3JCFDZ
    //10、商户支付宝日提现(对账) 2TX
    //11、商户日汇总订单金额(对账) 2DZ
    //12、基础金提现(对账) 2TX
    //13、扣除保证金(对账) 3JCFKYJ
    @Getter
    @Setter
    @XStreamAlias("type")
    private String type;

    //收款类型:
    //1、商户收入(费用单) 1FK
    //2、佣金及第三方(支付宝)手续费(费用单) 102
    //   佣金 填入表中AA字段
    //   第三方(支付宝)手续费 填入表中AN01字段
    //3、积分 待定
    //4、预售金 401
    //5、退保证金(对账) 2TYJ
    //6、退货款(对账) 2THK 金额填写负值
    //7、缴纳保证金(对账) 3Z0101
    //8、技术服务费(订单) 3D02
    //9、技术服务费(对账) 3Z0201
    //10、商户支付宝日提现(对账) 2TX
    //11、商户日汇总订单金额(对账) 201
    //12、基础金提现(对账) 2TX
    //13、扣除保证金(对账) 301
    @Getter
    @Setter
    @XStreamAlias("collectType")
    private String collectType;         // 收款类型

    @Getter
    @Setter
    @XStreamAlias("outerCode")
    private String outerCode;           // 商户88码，10位 MDI

    @Getter
    @Setter
    @XStreamAlias("amount")
    private String amount;              // 金额,除第三方(支付宝手续费)以外的其他金额

    @Getter
    @Setter
    @XStreamAlias("thirdAmount")
    private String thirdAmount;         // 支付宝手续费

    @Getter
    @Setter
    @XStreamAlias("createDate")
    private String createDate;          // RRS.COM创建单据时间 yyyy-MM-dd

    @Getter
    @Setter
    @XStreamAlias("note")
    private String note;                // 备注,暂时可以不用

    @Getter
    @Setter
    @XStreamAlias("extra1")
    private String extra1;              // 支付方式默认 'ZFB'

    @Getter
    @Setter
    @XStreamAlias("extra2")
    private String extra2;              // 备用字段2

    @Setter
    @Getter
    @XStreamAlias("channel")
    private String channel;             // 渠道号,固定填 1

    @Getter
    @Setter
    @XStreamAlias("business")
    private String business;            // 品类，需要传中文及行业描述,如；【家电】【家具】

    @Getter
    @Setter
    @XStreamAlias("voucher")
    private String voucher;             // 凭据号

    @Getter
    @Setter
    @XStreamAlias("voucherAt")
    private String voucherDate;         // 记账时间

    @Getter
    @Setter
    @XStreamAlias("receipt")
    private String receipt;             // 发票号

    @Getter
    @Setter
    @XStreamAlias("receiptAt")
    private String receiptDate;         // 开票时间

    @Getter
    @Setter
    @XStreamAlias("thirdPartyReceipt")
    private String thirdPartyReceipt;   // 第三方（如支付宝）手续费发票

    @Getter
    @Setter
    @XStreamAlias("thirdPartyReceiptAt")
    private String thirdPartyReceiptDate; // 第三方（如支付宝）手续费发票时间

    @Getter
    @Setter
    @XStreamAlias("jdeResult")
    private String jdeErrorMsg;         // JDE报错信息

    @SuppressWarnings("unused")
    public Jde() {}




    public Jde(String serial, String type, String collectType, String outerCode,
               Long amount, Long thirdAmount, String createDate, Long business, Integer paymentType) {
        this.serial = serial;
        this.type = type;
        this.collectType = collectType;
        this.outerCode = outerCode;
        this.amount = amount == null? null : DECIMAL_FORMAT.format(amount / 100.0);
        this.thirdAmount = thirdAmount == null? null : DECIMAL_FORMAT.format(thirdAmount / 100.0);
        this.createDate = createDate;
        this.business = String.valueOf(business);

        if (paymentType == null) {
            this.extra1 = "ZFB";
        } else {
            this.extra1 = Objects.equal(paymentType , DepositFee.PaymentType.CBC.value()) ? "JHDH" : "ZFB";
        }

        this.channel = "1";
        this.extra2 = "2";
    }

    // 商户收入
    public static Jde sellerEarning(Long id, String outerCode, Long amount, Date createdAt, Long business){
        String serial = generate(id, Type.SELLER_EARNING.code());
        return new Jde(serial, Type.SELLER_EARNING.type(), Type.SELLER_EARNING.collect(),
                outerCode, amount, null, dft.print(new DateTime(createdAt)), business, null);
    }

    // 平台佣金
    public static Jde commissionAndThird(Long id, String outerCode, Long amount, Long thirdAmount, Date createdAt, Long business) {
        String serial = generate(id, Type.COMMISSION_AND_THIRD.code());
        return new Jde(serial, Type.COMMISSION_AND_THIRD.type(), Type.COMMISSION_AND_THIRD.collect(),
                outerCode, amount, thirdAmount, dft.print(new DateTime(createdAt)), business, null);
    }

    // 积分
    public static Jde score(Long id, String outerCode, Long amount, Date createdAt, Long business) {
        String serial = generate(id, Type.SCORE.code());
        return new Jde(serial, Type.SCORE.type(), Type.SCORE.collect(),
                outerCode, amount, null, dft.print(new DateTime(createdAt)), business, null);
    }

    // 预售金扣除
    public static Jde presell(Long id, String outerCode, Long amount, Long commission, Date createdAt, Long business) {
        String serial = generate(id, Type.PRESELL.code());
        return new Jde(serial, Type.PRESELL.type(), Type.PRESELL.collect(),
                outerCode, amount, commission, dft.print(new DateTime(createdAt)), business, null);
    }

    // 退保证金
    public static Jde depositRefund(Long id, String outerCode, Long amount, Date createdAt, Long business) {
        String serial = generate(id, Type.DEPOSIT_REFUND.code());
        return new Jde(serial, Type.DEPOSIT_REFUND.type(), Type.DEPOSIT_REFUND.collect(),
                outerCode, amount, null, dft.print(new DateTime(createdAt)), business, null);
    }

    // 退货款
    public static Jde paymentRefund(Long id, String outerCode, Long amount, Date createdAt, Long business) {
        String serial = generate(id, Type.PAYMENT_REFUND.code());
        return new Jde(serial, Type.PAYMENT_REFUND.type(), Type.PAYMENT_REFUND.collect(),
                outerCode, amount, null, dft.print(new DateTime(createdAt)), business, null);
    }

    // 技术服务费对账, 建行电汇： 3Z01S02 支付宝:  3Z0101
    public static Jde depositPay(Long id, String outerCode, Long amount, Date createdAt, Long business, Integer paymentType) {
        String serial = generate(id, Type.DEPOSIT_PAY.code());

        String collect = Objects.equal(DepositFee.PaymentType.CBC.value(), paymentType) ? "3Z0102" : Type.DEPOSIT_PAY.collect();
        return new Jde(serial, Type.DEPOSIT_PAY.type(), collect,
                outerCode, amount, null, dft.print(new DateTime(createdAt)), business, paymentType);
    }

    // 技术服务费订单
    public static Jde techFeeOrder(Long id, String outerCode, Long amount, Date createdAt, Long business, Integer paymentType) {
        String serial = generate(id, Type.TECH_FEE_ORDER.code());
        return new Jde(serial, Type.TECH_FEE_ORDER.type(), Type.TECH_FEE_ORDER.collect(),
                outerCode, amount, null, dft.print(new DateTime(createdAt)), business, paymentType);
    }

    // 技术服务费对账, 建行电汇： 3Z0202 支付宝:  3Z0201
    public static Jde techFeeSettlement(Long id, String outerCode, Long amount, Date createdAt, Long business, Integer paymentType) {
        String serial = generate(id, Type.TECH_FEE_SETTLEMENT.code());
        String collect = Objects.equal(DepositFee.PaymentType.CBC.value(), paymentType) ? "3Z0202" : Type.TECH_FEE_SETTLEMENT.collect();
        return new Jde(serial, Type.TECH_FEE_SETTLEMENT.type(), collect,
                outerCode, amount, null, dft.print(new DateTime(createdAt)), business, paymentType);
    }

    // 支付宝日提现记录
    public static Jde alipayCash(Long id, String outerCode, Long amount, Date createdAt, Long business) {
        String serial = generate(id, Type.ALIPAY_CASH.code());
        return new Jde(serial, Type.ALIPAY_CASH.type(), Type.ALIPAY_CASH.collect(),
                outerCode, amount, null, dft.print(new DateTime(createdAt)), business, null);
    }

    // 商户日汇总订单金额
    public static Jde orderTotal(Long id, String outerCode, Long amount, Date createdAt, Long business) {
        String serial = generate(id, Type.ORDER_TOTAL.code());
        return new Jde(serial, Type.ORDER_TOTAL.type(), Type.ORDER_TOTAL.collect(),
                outerCode, amount, null, dft.print(new DateTime(createdAt)), business, null);
    }

    // 基础金提现
    public static Jde depositCash(Long id, String outerCode, Long amount, Date createdAt, Long business) {
        String serial = generate(id, Type.DEPOSIT_CASH.code());
        return new Jde(serial, Type.DEPOSIT_CASH.type(), Type.DEPOSIT_CASH.collect(),
                outerCode, amount, null, dft.print(new DateTime(createdAt)), business, null);
    }

    // 扣保证金
    public static Jde depositDeduction(Long id, String outerCode, Long amount, Date createdAt, Long business) {
        String serial = generate(id, Type.DEPOSIT_DEDUCTION.code());
        return new Jde(serial, Type.DEPOSIT_DEDUCTION.type(), Type.DEPOSIT_DEDUCTION.collect(),
                outerCode, amount, null, dft.print(new DateTime(createdAt)), business, null);
    }



    /**
     * 生成编码
     *
     * @param id  订单id
     * @return 编号
     */
    public static String generate(Long id, String code) {
        //补齐13位数字
        String suffix = "0000000000000" + id;
        suffix = suffix.substring(suffix.length() - 13, suffix.length());
        return PREFIX + code + suffix;
    }


    public static enum Type {
        SELLER_EARNING("1", "1FK", "1FK" ,"商户收入"),
        COMMISSION_AND_THIRD("2", "1FYD", "102" ,"佣金及第三方（支付宝）手续费"),
        SCORE("3", "", "", "积分"),
        PRESELL("4", "4YDJ", "401", "预售金扣除"),
        DEPOSIT_REFUND("5", "2TYJ", "2TYJ", "退保证金"),
        PAYMENT_REFUND("6", "2THK", "2THK", "退货款"),
        DEPOSIT_PAY("7", "3JCFDZ", "3Z0101", "缴纳保证金"),
        TECH_FEE_ORDER("8", "3JCFDD", "3D02", "技术服务费订单"),
        TECH_FEE_SETTLEMENT("9", "3JCFDZ", "3Z0201", "技术服务费对账"),
        ALIPAY_CASH("a", "2TX", "2TX", "支付宝提现"),
        ORDER_TOTAL("b", "2DZ", "201", "商户日订单总金额"),
        DEPOSIT_CASH("c", "2TX", "2TX", "基础金提现"),
        DEPOSIT_DEDUCTION("d", "3JCFKYJ", "301", "扣除保证金");




        private Type(String code, String type, String collect, String description) {
            this.code = code;
            this.type = type;
            this.description = description;
            this.collect = collect;
        }

        private final String code;          // 序号
        private final String type;          // 类型
        private final String collect;       // 收款类型
        private final String description;   // 说明

        public final String type() {
            return this.type;
        }

        public final String collect(){
            return this.collect;
        }

        public final String code(){
            return this.code;
        }

        @Override
        public String toString() {
            return this.description;
        }

    }

    public String toXml(){
        return xStream.toXML(this);
    }

}
