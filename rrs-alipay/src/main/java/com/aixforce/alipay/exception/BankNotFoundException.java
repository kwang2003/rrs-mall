package com.aixforce.alipay.exception;

/**
 * 未找到指定银行
 *
 * Mail: remindxiao@gmail.com <br>
 * Date: 2014-03-19 11:41 AM  <br>
 * Author: xiao
 */
public class BankNotFoundException extends RuntimeException{

    public BankNotFoundException(String s) {
        super(s);
    }
}
