package com.rrs.brand.model;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by zhua02 on 2014/7/25.
 */
public class BrandUserAnnouncement implements Serializable {

    private int id;
    private int brandUserId;
    private String announcement;
    private int status;
    private String title;
    private Date createTime;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getBrandUserId() {
        return brandUserId;
    }

    public void setBrandUserId(int brandUserId) {
        this.brandUserId = brandUserId;
    }

    public String getAnnouncement() {
        return announcement;
    }

    public void setAnnouncement(String announcement) {
        this.announcement = announcement;
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
