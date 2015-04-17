package com.rrs.coupons.model;

import java.io.Serializable;

/**
 * Created by zhua02 on 2014/9/2.
 */
public class LqMessage implements Serializable {
    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    private String status;
    private String message;
}
