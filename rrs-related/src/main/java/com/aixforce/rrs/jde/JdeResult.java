package com.aixforce.rrs.jde;

import com.google.common.base.Objects;
import com.google.common.base.Strings;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import static com.aixforce.common.utils.Arguments.notEmpty;

/**
 * Mail: remindxiao@gmail.com <br>
 * Date: 2014-02-19 4:05 PM  <br>
 * Author: xiao
 */
@ToString
public class JdeResult {

    @Getter
    @Setter
    @XStreamAlias("szEDTL")
    private String voucher;                     // 凭据号

    @Getter
    @Setter
    @XStreamAlias("szE58HUS24")
    private String receipt;                     // 发票号

    @Getter
    @Setter
    @XStreamAlias("sz55MDOCNO")
    private String thirdPartyReceipt;           // 第三方(如支付宝)手续费发票号


    @Getter
    @Setter
    @XStreamAlias("szE58HUS06")
    private String vouchedDate;                 // 更新凭证时间

    @Getter
    @Setter
    @XStreamAlias("szE58SUS16")
    private String receiptedDate;               // 发票打印时间

    @Getter
    @Setter
    @XStreamAlias("szE58HUS09")
    private String thirdPartyReceiptDate;       // 更新凭证时间

    @Getter
    @Setter
    @XStreamAlias("szE58HUS23")
    private String result;                      // 处理结果

    @Getter
    @Setter
    @XStreamAlias("cErrorCode")
    private String status;                      // 是否成功

    @Getter
    @Setter
    @XStreamAlias("szErrorDescription")
    private String message;                     // 错误信息



    public boolean isSuccess() {
        return !Strings.isNullOrEmpty(status) && Objects.equal(status, "S");
    }


    public Object getError() {
        String prefix = notEmpty(result) ? result : null;
        String suffix = notEmpty(message) ? message : null;
        return Objects.toStringHelper(this)
                .add("result", prefix)
                .add("error", suffix)
                .omitNullValues().toString();
    }
}
