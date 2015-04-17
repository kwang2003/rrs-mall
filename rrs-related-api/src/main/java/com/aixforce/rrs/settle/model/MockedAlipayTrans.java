package com.aixforce.rrs.settle.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * Mail: xiao@terminus.io <br>
 * Date: 2014-05-15 10:58 AM  <br>
 * Author: xiao
 */
@ToString
@EqualsAndHashCode(callSuper = true)
public class MockedAlipayTrans extends AlipayTrans implements Serializable {

    private static final long serialVersionUID = 6034070408462717534L;
}
