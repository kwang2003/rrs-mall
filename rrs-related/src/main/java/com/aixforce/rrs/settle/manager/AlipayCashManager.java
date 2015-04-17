package com.aixforce.rrs.settle.manager;

import com.aixforce.rrs.settle.dao.OrderAlipayCashDao;
import com.aixforce.rrs.settle.dao.SellerAlipayCashDao;
import com.aixforce.rrs.settle.dao.SettlementDao;
import com.aixforce.rrs.settle.model.OrderAlipayCash;
import com.aixforce.rrs.settle.model.SellerAlipayCash;
import com.aixforce.rrs.settle.model.Settlement;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.aixforce.common.utils.Arguments.equalWith;
import static com.aixforce.common.utils.Arguments.notNull;
import static com.google.common.base.Preconditions.checkState;


/**
 * Mail: remindxiao@gmail.com <br>
 * Date: 2014-02-26 4:13 PM  <br>
 * Author: xiao
 */
@Slf4j
@Component
public class AlipayCashManager {

    @Autowired
    private SettlementDao settlementDao;

    @Autowired
    private OrderAlipayCashDao orderAlipayCashDao;

    @Autowired
    private SellerAlipayCashDao sellerAlipayCashDao;

    /**
     * 批量标记订单提现列表中指定的记录为”已提现“
     *
     * @param cashes  订单提现列表
     */
    @Transactional
    public void batchCashing(List<OrderAlipayCash> cashes, String operator) {
        for (OrderAlipayCash cash : cashes) {
            cashing(cash, operator);
        }
    }


    /**
     * 标记订单提现明细中的指定记录为“已提现”
     *
     * @param cash      订单提现明细
     * @param operator  提现人
     */
    @Transactional
    public void cashing(OrderAlipayCash cash, String operator) {
        orderAlipayCashDao.cashing(cash.getId(), operator);
        Settlement settlement = settlementDao.getByOrderId(cash.getOrderId());
        checkState(notNull(settlement), "settlement.not.found");

        if (equalWith(settlement.getFinished(), Settlement.Finished.DONE.value())) {
            // 若当前订单已经关闭，则需要判断是否所有该订单有关的提交记录是否都已提现
            OrderAlipayCash criteria = new OrderAlipayCash();
            criteria.setOrderId(cash.getOrderId());
            criteria.setStatus(OrderAlipayCash.Status.NOT.value());

            Long notCashedCount = orderAlipayCashDao.countOf(criteria);  // 如果所有订单都已提现
            if (equalWith(notCashedCount, 0L)) {
                Settlement updating = new Settlement();
                updating.setCashed(Settlement.Cashed.DONE.value());
                updating.setSettleStatus(Settlement.SettleStatus.ING.value());
                updating.setId(settlement.getId());
                settlementDao.update(updating);
            }
        }
    }

    /**
     * 更新提现相关的凭证号
     *
     */
    @Transactional
    public void vouching(SellerAlipayCash vouch) {
        sellerAlipayCashDao.vouching(vouch);
        orderAlipayCashDao.batchVouching(vouch.getSellerId(), vouch.getSummedAt(), vouch.getVoucher());
    }


}
