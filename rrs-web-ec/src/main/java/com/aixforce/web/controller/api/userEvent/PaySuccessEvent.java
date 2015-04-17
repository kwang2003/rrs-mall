package com.aixforce.web.controller.api.userEvent;

import lombok.Data;

import javax.servlet.http.HttpServletRequest;

/**
 * Created by yjgsjone@163.com on 14-9-15.
 */
@Data
public class PaySuccessEvent {

    protected final HttpServletRequest request;


    public PaySuccessEvent(HttpServletRequest request) {
        this.request = request;
    }

}
