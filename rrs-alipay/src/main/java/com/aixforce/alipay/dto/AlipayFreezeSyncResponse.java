package com.aixforce.alipay.dto;

import com.google.common.base.Objects;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamConverter;
import com.thoughtworks.xstream.annotations.XStreamImplicit;
import com.thoughtworks.xstream.converters.extended.ToAttributedValueConverter;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * Mail: remindxiao@gmail.com <br>
 * Date: 2014-03-13 11:55 AM  <br>
 * Author: xiao
 */
@XStreamAlias("alipay")
public class AlipayFreezeSyncResponse {


    @Setter
    @XStreamAlias("is_success")
    private String success;

    @Getter
    @Setter
    @XStreamAlias("error")
    private String error;

    @Getter
    @Setter
    @XStreamAlias("sign")
    private String sign;

    @Getter
    @Setter
    @XStreamAlias("sign_type")
    private String signType;

    public boolean isSuccess() {
        return Objects.equal(success, "T");
    }

    @Getter
    @Setter
    @XStreamAlias("request")
    private AlipayFreezeRequest reuqest;

    @Getter
    @Setter
    @XStreamAlias("response")
    private AlipayFreezeReponse response;

    public static class AlipayFreezeRequest {

        @XStreamImplicit(itemFieldName = "param")
        private List<Param> param;

        public List<Param> getParam() {
            return param;
        }

        public void setParam(List<Param> param) {
            this.param = param;
        }

    }

    @XStreamAlias("param")
    @XStreamConverter(value = ToAttributedValueConverter.class, strings = { "content" })
    public static class Param {

        public Param(String name, String content) {
            super();
            this.name = name;
            this.content = content;
        }

        @Setter
        @Getter
        @XStreamAsAttribute
        private String name;

        @Setter
        @Getter
        private String content;

    }


    public static class AlipayFreezeReponse {

        @Setter
        @Getter
        @XStreamAlias("order")
        private FreezeOrder rrade;

    }

    public static class FreezeOrder {

        @Getter
        @Setter
        @XStreamAlias("gmt_create")
        private String gmtCreate;         // 支付宝交易号

        @Getter
        @Setter
        @XStreamAlias("gmt_trans")
        private String gmtTrans;   // 退款金额

        @Getter
        @Setter
        @XStreamAlias("auth_no")
        private String authNo;          // 退款理由

        @Getter
        @Setter
        @XStreamAlias("operation_id")
        private String operationId;          // 退款理由

        @Getter
        @Setter
        @XStreamAlias("out_request_no")
        private String outRequestNo;          // 退款理由

        @Getter
        @Setter
        @XStreamAlias("result_code")
        private String resultCode;          // 退款理由

        @Getter
        @Setter
        @XStreamAlias("result_message")
        private String resultMessage;          // 退款理由

    }
}
