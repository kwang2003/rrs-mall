package com.aixforce.rrs.settle.service;

import com.aixforce.common.model.PageInfo;
import com.aixforce.common.model.Paging;
import com.aixforce.common.model.Response;
import com.aixforce.rrs.settle.dao.MockedAlipayTransDao;
import com.aixforce.rrs.settle.model.MockedAlipayTrans;
import com.google.common.base.Optional;
import com.google.common.base.Throwables;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Map;

import static com.aixforce.common.utils.Arguments.notNull;
import static com.google.common.base.Preconditions.checkState;

/**
 * Mail: xiao@terminus.io <br>
 * Date: 2014-10-22 2:37 PM  <br>
 * Author: xiao
 */
@Slf4j
@Service
public class MockedAlipayTransServiceImpl implements MockedAlipayTransService {

    @Autowired
    private MockedAlipayTransDao mockedAlipayTransDao;

    private DateTimeFormatter DFT_TIME = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * 创建虚拟帐务记录
     *
     * @param trans 支付宝帐务记录
     * @return 帐务记录标识
     */
    @Override
    public Response<Long> create(MockedAlipayTrans trans) {
        Response<Long> res = new Response<Long>();

        try {
            Long id = mockedAlipayTransDao.create(trans);
            res.setResult(id);

        } catch (Exception e) {
            log.error("fail to create mockedAlipayTrans with trans:{}, cause:{}",
                    trans, Throwables.getStackTraceAsString(e));
            res.setError("mocked.trans.create.fail");
        }

        return res;
    }

    /**
     * 根据交易流水获取支付宝交易
     *
     * @param tradeNo 支付宝交易流水
     * @return 虚拟帐务记录
     */
    @Override
    public Response<MockedAlipayTrans> getByTradeNo(String tradeNo) {
        Response<MockedAlipayTrans> res = new Response<MockedAlipayTrans>();

        try {
            List<MockedAlipayTrans> mockedAlipayTranses = mockedAlipayTransDao.list(tradeNo);
            Optional<MockedAlipayTrans> payTrans = Optional.absent();

            for (MockedAlipayTrans trans : mockedAlipayTranses) {
                if (notNull(trans.getSubTransCodeMsg()) && trans.getSubTransCodeMsg().startsWith("快速支付")) {
                    payTrans = Optional.of(trans);
                    break;
                }
            }

            checkState(payTrans.isPresent(), "mocked.pay.trans.not.found");
            res.setResult( payTrans.get());
            return res;

        } catch (Exception e) {
            log.info("fail to get MockedAlipayTrans with transNo:{}, cause:{}",
                    tradeNo, Throwables.getStackTraceAsString(e));
            res.setError("mocked.trans.query.fail");
        }

        return res;
    }


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
    @Override
    public Response<Paging<MockedAlipayTrans>> findBy(MockedAlipayTrans criteria,
                                               Date startAt,
                                               Date endAt,
                                               Integer pageNo,
                                               Integer size) {
        Response<Paging<MockedAlipayTrans>> res = new Response<Paging<MockedAlipayTrans>>();

        try {
            Map<String, Object> params = Maps.newHashMap();
            params.put("criteria", criteria);
            params.put("createdStartAt", startAt);
            params.put("createdEndAt", endAt);
            PageInfo pageInfo = new PageInfo(pageNo, size);
            params.put("offset", pageInfo.offset);
            params.put("limit", pageInfo.limit);

            Paging<MockedAlipayTrans> paging = mockedAlipayTransDao.findBy(params);
            res.setResult(paging);

        } catch (Exception e) {
            log.info("fail to get MockedAlipayTrans with startAt:{}, endAt{}, transNo:{}, merchantNo:{} cause:{}",
                    startAt, endAt, criteria.getTradeNo(), criteria.getMerchantOutOrderNo(), Throwables.getStackTraceAsString(e));
            res.setError("mocked.trans.query.fail");

        }

        return res;
    }


}
