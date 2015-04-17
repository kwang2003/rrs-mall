package com.aixforce.sms.haier;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import lombok.Getter;
import lombok.Setter;


/**
 * Author:  <a href="mailto:remindxiao@gmail.com">xiao</a>
 * Date: 2013-12-18
 */
@XStreamAlias("CoreSMS")
public class SmsRequest {

    @Getter
    @Setter
    @XStreamAlias("OperID")
    //用户标识
    private String operID;
    @Getter
    @Setter
    @XStreamAlias("OperPass")
    //操作密码
    private String operPass;
    @Getter
    @Setter
    @XStreamAlias("Action")
    //操作命令
    private String action = "Submit";
    @Getter
    @Setter
    @XStreamAlias("Category")
    //数据包类型
    private String category = "0";
    @Getter
    @Setter
    @XStreamAlias("Body")
    //请求内容
    private SmsRequestBody body;


    public SmsRequest(String operID, String operPass, SmsRequestBody body) {
        this.operID = operID;
        this.operPass = operPass;
        this.body = body;
    }


}
