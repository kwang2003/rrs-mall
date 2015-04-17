package com.rrs.kjtpay.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.util.Date;

/**
 * 快捷通支付帐号
 * @author jiangpeng
 * @createAt 2015/1/5 13:08
 */
@ToString
@NoArgsConstructor
public class KjtpayAccount implements Serializable{

    @Getter
    @Setter
    private Long id; //id

    @Getter
    @Setter
    private String partnerId;//商户号

    @Getter
    @Setter
    private String platUserId;//平台会员 ID

    @Getter
    @Setter
    private String platUser;//会员账号

    @Getter
    @Setter
    private String memberId;//快捷通账号 ID

    @Getter
    @Setter
    private String memberName;//快捷通账户名

    @Getter
    @Setter
    private String shopName;//店铺名称

    @Getter
    @Setter
    @JsonIgnore
    private Date createdAt;             // 创建时间

    @Getter
    @Setter
    @JsonIgnore
    private Date updatedAt;             // 修改时间

}
