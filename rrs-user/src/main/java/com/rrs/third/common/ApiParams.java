package com.rrs.third.common;

import lombok.Getter;
import lombok.Setter;

/**
 * Created by zhaop01 on 2014/9/11.
 */
public class ApiParams {
    @Getter
    @Setter
    private String source;
    @Getter
    @Setter
    private String url;
    @Getter
    @Setter
    private String ssoSessionId;
    @Getter
    @Setter
    private String secretKey;
    @Getter
    @Setter
    private String coAppName;

    @Getter
    @Setter
    private String securityType;
}
