package com.rrs.kjtpay.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;

/**
 * 快捷通账户绑定同步通知
 * @author jiangpeng
 * @createAt 2015/1/6 9:36
 */
@ToString
@NoArgsConstructor
public class CreateAccreditSyncNotice implements Serializable {

    @Getter
    @Setter
    private String is_success;//是否成功 T or F

    @Getter
    @Setter
    private String partner_id;//商户号

    @Getter
    @Setter
    private String _input_charset;//参数编码字符集

    @Getter
    @Setter
    private String sign;//签名

    @Getter
    @Setter
    private String sign_type;//签名方式

    @Getter
    @Setter
    private String error_code;//返回错误码

    @Getter
    @Setter
    private String error_message;//返回错误原因

    @Getter
    @Setter
    private String memo;//备注

    @Getter
    @Setter
    private String plat_user_id;//平台会员 ID

    @Getter
    @Setter
    private String plat_user;//会员账号

    @Getter
    @Setter
    private String member_id;//快捷通账号 ID

    @Getter
    @Setter
    private String member_name;//快捷通账户名

}
