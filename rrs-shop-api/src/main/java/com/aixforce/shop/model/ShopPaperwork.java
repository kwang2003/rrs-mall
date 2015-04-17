package com.aixforce.shop.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.base.Objects;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;

/**
 * 店铺的各种证件
 * Author:  <a href="mailto:i@terminus.io">jlchen</a>
 * Date: 2014-05-12
 */

public class ShopPaperwork implements Serializable{
    private static final long serialVersionUID = -3229823227777396591L;
    @Getter
    @Setter
    private Long id;

    @Getter
    @Setter
    private Long shopId;

    @Getter
    @Setter
    private String businessLicence; //营业执照

    @Getter
    @Setter
    private String taxCertificate;  //税务登记证

    @Getter
    @Setter
    private String accountPermit;   //开户许可证

    @Getter
    @Setter
    private String organizationCode; //组织机构代码证

    @Getter
    @Setter
    private String corporateIdentity; //法人身份证

    @Getter
    @Setter
    private String corporateIdentityB;//法人身份证背面

    @Getter
    @Setter
    private String contractImage1;    //合同图片

    @Getter
    @Setter
    private String contractImage2;    //合同图片

    @Getter
    @Setter
    @JsonIgnore
    private Date createdAt;             // 创建时间

    @Getter
    @Setter
    @JsonIgnore
    private Date updatedAt;             // 修改时间


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ShopPaperwork that = (ShopPaperwork) o;

        return Objects.equal(this.shopId, that.shopId)
                && Objects.equal(this.businessLicence, that.businessLicence)
                && Objects.equal(this.taxCertificate, that.taxCertificate)
                && Objects.equal(this.accountPermit, that.accountPermit)
                && Objects.equal(this.organizationCode, that.organizationCode)
                && Objects.equal(this.corporateIdentity, that.corporateIdentity);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(shopId, businessLicence, taxCertificate,
                accountPermit, organizationCode, corporateIdentity);
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this)
                .add("id", id)
                .add("shopId", shopId)
                .add("businessLicence", businessLicence)
                .add("taxCertificate", taxCertificate)
                .add("accountPermit", accountPermit)
                .add("organizationCode", organizationCode)
                .add("corporateIdentity", corporateIdentity)
                .omitNullValues()
                .toString();
    }
}
