package com.rrs.kjtpay.core;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ResourceBundle;

/**
 * 快捷通配置
 * @author jiangpeng
 * @createAt 2015/1/5 13:35
 */
@Component
public class KjtConfig {


    private static ResourceBundle res = ResourceBundle.getBundle("app");

    public static final String cvmPath = get("cvmConfigFile");

    public static final String pfxPath = get("pfxFileName");

    public static final String pfxKey = get("keyPassword");

    public static final String certPath = get("certFileName");

    public static String get(String key){
        return res.getString(key);
    }


    //基本定义
    @Getter
    @Setter
    @Value("#{app.gatewayUrl}")
    private String gatewayUrl;
    @Getter
    @Setter
    @Value("#{app.kjtPartnerId}")
    private String partnerId;//商户号
    @Getter
    @Setter
    @Value("#{app.kjtInputCharset}")
    private String inputCharset;//编码集
    @Getter
    @Setter
    @Value("#{app.kjtVersion}")
    private String version;//版本号
    @Getter
    @Setter
    @Value("#{app.signType}")
    private String signType;//签名类型

    //接口定义

    //账户绑定
    @Getter
    @Setter
    @Value("#{app.createAccreditService}")
    private String  createAccreditService;//service Name
    @Getter
    @Setter
    @Value("#{app.createAccreditReturnUrl}")
    private String createAccreditReturnUrl;//同步通知路径
    @Getter
    @Setter
    @Value("#{app.createAccreditCheckFlag}")
    private String createAccreditCheckFlag;//检测公司名标识,0为否1为是


    //批量返款
    @Getter
    @Setter
    @Value("#{app.createBatchTransferToAccountService}")
    private String createBatchTransferToAccountService;//service Name

    @Getter
    @Setter
    @Value("#{app.createBatchTransferToAccountNotifyUrl}")
    private String createBatchTransferToAccountNotifyUrl;//异步通知路径

}
