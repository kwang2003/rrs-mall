package com.aixforce.rrs.jde.mdm;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

/**
 * Mail: xiao@terminus.io <br>
 * Date: 2014-07-25 3:30 PM  <br>
 * Author: xiao
 */
@ToString
public class MdmPagingResponse {

    private boolean success;    // 调用是否成功

    private String code;        // 如果success = false,则通过error可以查看错误信息

    @Getter
    private String msg;         // 错误信息打印

    @Getter
    @Setter
    private List<MdmUpdating> data;     // 分页数据

    @Setter
    private Boolean next;    // 是否存在下一页


    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public void setResult(List<MdmUpdating> data) {
        this.success = true;
        this.data = data;
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

    public Boolean hasNext() {
        return next;
    }

}
