package com.rrs.brand.model;

import java.io.Serializable;

/**
 * Created by zhua02 on 2014/7/28.
 */
public class BrandWRlView implements Serializable {

    private String brandName;
    private String shopName;
    private int shopId;
    private String phone;
    private String email;
    private String address;
    private String businessLicence;
    private String taxCertificate;
    private String accountPermit;
    private String organizationCode;
    private String corporateIdentity;
    private String corporateIdentityB;
    private String contractImage1;
    private String contractImage2;
    private String domain;
    private String business;
    private String status;

    public String getBrandName() {
        return brandName;
    }

    public void setBrandName(String brandName) {
        this.brandName = brandName;
    }

    public String getShopName() {
        return shopName;
    }

    public void setShopName(String shopName) {
        this.shopName = shopName;
    }

    public int getShopId() {
        return shopId;
    }

    public void setShopId(int shopId) {
        this.shopId = shopId;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getBusinessLicence() {
        return businessLicence;
    }

    public void setBusinessLicence(String businessLicence) {
        this.businessLicence = businessLicence;
    }

    public String getTaxCertificate() {
        return taxCertificate;
    }

    public void setTaxCertificate(String taxCertificate) {
        this.taxCertificate = taxCertificate;
    }

    public String getAccountPermit() {
        return accountPermit;
    }

    public void setAccountPermit(String accountPermit) {
        this.accountPermit = accountPermit;
    }

    public String getOrganizationCode() {
        return organizationCode;
    }

    public void setOrganizationCode(String organizationCode) {
        this.organizationCode = organizationCode;
    }

    public String getCorporateIdentity() {
        return corporateIdentity;
    }

    public void setCorporateIdentity(String corporateIdentity) {
        this.corporateIdentity = corporateIdentity;
    }

    public String getCorporateIdentityB() {
        return corporateIdentityB;
    }

    public void setCorporateIdentityB(String corporateIdentityB) {
        this.corporateIdentityB = corporateIdentityB;
    }

    public String getContractImage1() {
        return contractImage1;
    }

    public void setContractImage1(String contractImage1) {
        this.contractImage1 = contractImage1;
    }

    public String getContractImage2() {
        return contractImage2;
    }

    public void setContractImage2(String contractImage2) {
        this.contractImage2 = contractImage2;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public String getBusiness() {
        return business;
    }

    public void setBusiness(String business) {
        this.business = business;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
