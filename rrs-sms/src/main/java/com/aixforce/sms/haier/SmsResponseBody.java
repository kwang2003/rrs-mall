package com.aixforce.sms.haier;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamImplicit;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * Author:  <a href="mailto:remindxiao@gmail.com">xiao</a>
 * Date: 2013-12-18
 */
public class SmsResponseBody {

    @Getter
    @Setter
    @XStreamAlias("Code")
    //响应码
    private String code;

    @Getter
    @Setter
    @XStreamImplicit
    //消息列表
    private List<SmsResponseMessage> messageList;


    public SmsResponseBody(String code, List<SmsResponseMessage> messageList) {
        this.code = code;
        this.messageList = messageList;
    }


}
