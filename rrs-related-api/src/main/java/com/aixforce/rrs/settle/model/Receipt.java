package com.aixforce.rrs.settle.model;

import java.util.Date;

/**
 * Mail: xiao@terminus.io <br>
 * Date: 2014-05-22 10:40 PM  <br>
 * Author: xiao
 */
public interface Receipt extends Bill {

    public String getReceipt();

    public Date getReceiptedAt();

    public void setReceipt(String receipt);

    public void setReceiptedAt(Date receiptedAt);

}
