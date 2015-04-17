package com.aixforce.rrs.jde;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import lombok.ToString;

/**
 * Mail: remindxiao@gmail.com <br>
 * Date: 2014-03-03 3:11 PM  <br>
 * Author: xiao
 */
@ToString
@XStreamAlias("response")
public class JdeWriteResponse<T> {

    @XStreamAlias("success")
    private boolean success;    // 调用是否成功

    @XStreamAlias("code")
    private String code;        // 如果success = false,则通过error可以查看错误信息

    @XStreamAlias("msg")
    private String msg;         // 错误信息打印

    public JdeWriteResponse () {}
    public JdeWriteResponse (boolean success) { this.success = success; }


    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public void setResult(T result) {
        this.success = true;
    }

    public String getError() {
        return code;
    }

    public void setError(String code, String msg) {
        this.success = false;
        this.code = code;
        this.msg = msg;
    }

    public void setError(String code) {
        this.success = false;
        this.code = code;
    }

    public String getMsg() { return msg; }


}
