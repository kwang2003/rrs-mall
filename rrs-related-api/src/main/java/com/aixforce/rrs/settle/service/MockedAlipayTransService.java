package com.aixforce.rrs.settle.service;

import com.aixforce.common.model.Paging;
import com.aixforce.common.model.Response;
import com.aixforce.rrs.settle.model.MockedAlipayTrans;

import java.util.Date;

/**
 *
 * 虚拟支付宝帐务记录
 *
 * Mail: xiao@terminus.io <br>
 * Date: 2014-10-22 2:28 PM  <br>
 * Author: xiao
 */
public interface MockedAlipayTransService {


    /**
     * 创建虚拟帐务记录
     * @param trans  支付宝帐务记录
     * @return  帐务记录标识
     */
    Response<Long> create(MockedAlipayTrans trans);


    /**
     * 根据交易流水获取支付宝交易
     *
     * @param tradeNo   支付宝交易流水
     * @return  虚拟帐务记录
     */
    Response<MockedAlipayTrans> getByTradeNo(String tradeNo);


    /**
     * 根据输入的条件查询支付宝虚拟帐务记录
     *
     * @param criteria  查询条件
     * @param startAt   查询开始时间（基于创建时间)
     * @param endAt     查询截止时间（基于创建时间)
     * @param pageNo    起始页
     * @param size      大小
     * @return   支付宝虚拟帐务分页记录
     */
    Response<Paging<MockedAlipayTrans>> findBy(MockedAlipayTrans criteria,
                                               Date startAt,
                                               Date endAt,
                                               Integer pageNo,
                                               Integer size);
}
