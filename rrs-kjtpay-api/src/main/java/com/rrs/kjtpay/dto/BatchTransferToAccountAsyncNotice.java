package com.rrs.kjtpay.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;

/**
 * Created by Administrator on 2014/12/29.
 */
@ToString
@NoArgsConstructor
public class BatchTransferToAccountAsyncNotice implements Serializable {

    @Getter
    @Setter
    private String notify_id; //通知id

    @Getter
    @Setter
    private String notify_type; //通知类型

    @Getter
    @Setter
    private String notify_time; //通知时间

    @Getter
    @Setter
    private String _input_charset; //参数字符编码集

    @Getter
    @Setter
    private String sign; //签名

    @Getter
    @Setter
    private String sign_type; //签名类型

    @Getter
    @Setter
    private String version; //版本号

    @Getter
    @Setter
    private String outer_trade_no; //商户网站订单号

    @Getter
    @Setter
    private String inner_trade_no; //快捷通转账交易号

    @Getter
    @Setter
    private String transfer_amount;//转账金额

    @Getter
    @Setter
    private String transfer_status; //转账状态

    @Getter
    @Setter
    private String fail_reason; //失败原因

    @Getter
    @Setter
    private String gmt_transfe; //转账时间
}
