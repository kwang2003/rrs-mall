package com.rrs.kjtpay.dto;

import lombok.*;

import java.io.Serializable;

/**
 * 快捷通账号绑定DTO
 * @author jiangpeng
 * @createAt 2015/1/5 13:19
 */
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class CreateAccredit implements Serializable {

    @Getter
    @Setter
    private String partnerId;//商户号

    @Getter
    @Setter
    private String partnerName;//

    @Getter
    @Setter
    private String businessId;//

    @Getter
    @Setter
    private String companyName;//公司名称

    @Getter
    @Setter
    private String memo;//备注

}
