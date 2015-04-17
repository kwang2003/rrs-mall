package com.aixforce.web.controller.api.userEvent;

import lombok.Data;

import javax.servlet.http.HttpServletRequest;
import java.util.Set;

/**
 * Created by yjgsjone@163.com on 14-9-15.
 */
@Data
public class OrderEvent {

    protected final HttpServletRequest request;

    protected final Set<Long> ids;

    public OrderEvent(Set<Long> ids, HttpServletRequest request) {
        this.ids = ids;
        this.request = request;
    }

}
