package com.rrs.brand.model;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by zhua02 on 2014/7/28.
 */
public class BrandsClubKey implements Serializable {

    private int id;
    private int shopId;
    private int brandClubId;
    private int status;
    private Date createTime;


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getShopId() {
        return shopId;
    }

    public void setShopId(int shopId) {
        this.shopId = shopId;
    }

    public int getBrandClubId() {
        return brandClubId;
    }

    public void setBrandClubId(int brandClubId) {
        this.brandClubId = brandClubId;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

}
