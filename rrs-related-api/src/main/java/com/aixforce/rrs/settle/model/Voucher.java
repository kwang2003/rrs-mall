package com.aixforce.rrs.settle.model;

import java.util.Date;

/**
 * Mail: remindxiao@gmail.com <br>
 * Date: 2014-03-12 6:39 PM  <br>
 * Author: xiao
 */
public interface Voucher  extends Bill  {

    public void setVoucher(String voucher);

    public void setVouchedAt(Date voucherAt);

    public String getVoucher();

    public Date getVouchedAt();
}
