package com.aixforce.rrs.settle.manager;

import com.aixforce.common.model.Response;
import com.aixforce.rrs.settle.dao.*;
import com.aixforce.rrs.settle.model.AbnormalTrans;
import com.aixforce.rrs.settle.model.ItemSettlement;
import com.aixforce.rrs.settle.model.SellerSettlement;
import com.aixforce.rrs.settle.model.Settlement;
import com.aixforce.trade.model.OrderItem;
import com.aixforce.trade.service.OrderQueryService;
import com.aixforce.user.model.User;
import com.aixforce.user.service.AccountService;
import com.google.common.base.Objects;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.Date;
import java.util.List;

import static com.aixforce.common.utils.Arguments.notNull;
import static com.google.common.base.Preconditions.checkState;

/**
 * Mail: remindxiao@gmail.com <br>
 * Date: 2014-01-20 4:29 PM  <br>
 * Author: xiao
 */
@Slf4j
@Component
public class SettlementManager {

    @Autowired
    private DepositFeeDao depositFeeDao;

    @Autowired
    private DepositAccountDao depositAccountDao;

    @Autowired
    private SettlementDao settlementDao;

    @Autowired
    private SellerSettlementDao sellerSettlementDao;

    @Autowired
    private DepositFeeCashDao depositFeeCashDao;

    @Autowired
    private SellerAlipayCashDao sellerAlipayCashDao;

    @Autowired
    private ItemSettlementDao itemSettlementDao;

    @Autowired
    private AbnormalTransDao abnormalTransDao;

    @Autowired
    private AccountService<User> accountService;

    @Autowired
    private OrderQueryService orderQueryService;


    public void settled(Long id) {
        Settlement settlement = settlementDao.get(id);
        checkState(notNull(settlement), "settle.entity.not.found");

        settlementDao.settled(id);
    }

    /**
     * 创建订单及其子订单的结算信息
     * @param settlement        订单结算信息
     */
    @Transactional
    public Long create(Settlement settlement, Double rate) {
        settlementDao.create(settlement);
        createItemSettlements(settlement, rate);
        return settlement.getId();
    }

    /**
     * 创建订单及其子订单的结算信息 for 预售订金
     * @param settlement        订单结算信息
     */
    @Transactional
    public Long createForPresale(Settlement settlement, Double rate) {
        settlementDao.create(settlement);
        createItemSettlements(settlement, rate);
        return settlement.getId();
    }

    /**
     * 根据订单创建子订单结算记录
     *
     * @param settlement 订单
     * @param rate       费率
     */
    private void createItemSettlements(Settlement settlement, Double rate) {
        checkState(notNull(settlement.getOrderId()));

        Long orderId = settlement.getOrderId();
        List<ItemSettlement> itemSettlements = itemSettlementDao.findByOrderId(orderId);  // 判断是否已经创建子订单结算信息
        if (itemSettlements.size() > 0) {  // 若已经存在则不创建
            log.debug("item settlements of {} existed skipped", settlement);
            return;
        }

        Response<List<OrderItem>> orderItemsQueryResult = orderQueryService.findOrderItemByOrderId(orderId);
        checkState(orderItemsQueryResult.isSuccess(), "order.item.query.fail");
        List<OrderItem> items = orderItemsQueryResult.getResult();

        for (OrderItem item : items) {
            ItemSettlement is = new ItemSettlement();
            is.setOrderId(settlement.getOrderId());
            is.setSellerId(settlement.getSellerId());
            is.setSellerName(settlement.getSellerName());
            is.setBuyerId(settlement.getBuyerId());
            is.setPaidAt(item.getPaidAt());//子订单的支付时间
            is.setBusiness(settlement.getBusiness());

            is.setSettleStatus(Settlement.SettleStatus.ING.value());
            is.setOrderItemId(item.getId());
            is.setTradeStatus(item.getStatus());
            is.setPayType(item.getPayType());
            is.setFee((long)item.getFee());
            is.setType(item.getType());
            is.setItemName(item.getItemName());
            is.setItemQuantity(item.getQuantity());
            is.setReason(item.getReason());
            is.setPaymentCode(item.getPaymentCode());
            is.setRefundAmount( item.getRefundAmount() == null? 0L: item.getRefundAmount().longValue());

            is.setCommissionRate(rate);

            Response<User> getUser = accountService.findUserById(item.getBuyerId());
            if (getUser.isSuccess()) {
                User buyer = getUser.getResult();
                is.setBuyerName(buyer.getName());
            }

            itemSettlementDao.create(is);
        }

    }

    /**
     * 更新订单及其子订单的结算状态（除支付宝手续费以外的各项金额）
     *
     * @param settlement         订单结算信息
     * @param itemSettlements    子订单结算信息
     */
    @Transactional
    public void update(Settlement settlement, List<ItemSettlement> itemSettlements) {
        settlementDao.update(settlement);
        for (ItemSettlement sub : itemSettlements) {
            itemSettlementDao.update(sub);
        }
    }


    /**
     * 关闭订单及关闭子订单 （更新交易状态及结束标记)
     *
     * @param settlement        订单结算信息
     */
    @Transactional
    public void finished(Settlement settlement) {

        settlementDao.finished(settlement);

        List<ItemSettlement> itemSettlements = itemSettlementDao.findByOrderId(settlement.getOrderId());
        for (ItemSettlement itemSettlement : itemSettlements) {
            Long orderItemId = itemSettlement.getOrderItemId();
            Response<OrderItem> orderItemResponse = orderQueryService.findOrderItemById(orderItemId);
            if (orderItemResponse.isSuccess()) {
                OrderItem orderItem = orderItemResponse.getResult();
                itemSettlement.setTradeStatus(orderItem.getStatus());
                itemSettlement.setRefundAmount(orderItem.getRefundAmount() == null ? 0L : orderItem.getRefundAmount().longValue());
                itemSettlement.setReason(orderItem.getReason());
                itemSettlementDao.update(itemSettlement);
            }
        }
    }

    /**
     * 更新指定商户日汇总的同步状态为 “已同步“ 同时更新对应的结算信息为已结算 <br/>
     * 需要同步更新对应的订单结算信息
     *
     * @param id  商户结算信息标识
     */
    @Transactional
    public void synced(Long id) {
        SellerSettlement sellerSettlement = sellerSettlementDao.get(id);
        checkState(notNull(sellerSettlement), "seller.settlement.not.exist");

        Date confirmedAt = sellerSettlement.getConfirmedAt();
        Date startAt = new DateTime(confirmedAt).withTimeAtStartOfDay().toDate();
        Date endAt = new DateTime(startAt).plusDays(1).toDate();

        boolean success = sellerSettlementDao.synced(id);

        checkState(success, "seller.settlement.synced.fail");
        settlementDao.batchSynced(startAt, endAt);
        itemSettlementDao.batchSynced(startAt, endAt);
    }

    /**
     * 记录不正确的的订单信息
     * @param settlements 订单列表
     */
    public void recordIncorrectSettlements(Collection<Settlement> settlements, String reason) {
        for (Settlement settlement : settlements) {
            AbnormalTrans abnormalTrans = new AbnormalTrans();
            abnormalTrans.setReason(reason);
            abnormalTrans.setSettlementId(settlement.getId());
            abnormalTrans.setOrderId(settlement.getOrderId());
            abnormalTransDao.create(abnormalTrans);
        }
    }

    @Transactional
    public void batchUpdate(Collection<Settlement> multiPaidSettlements) {
        for (Settlement settlement : multiPaidSettlements) {
            settlementDao.update(settlement);
        }
    }

    @Transactional
    public void batchUpdateOuterCodeOfSeller(String outerCode, Long sellerId) {
        Integer count = depositAccountDao.updateOuterCode(outerCode, sellerId);
        log.info("handled {} depositAccount to {} of seller(id:{})", count, outerCode, sellerId);

        count = depositFeeDao.batchUpdateOuterCode(outerCode, sellerId);
        log.info("handled {} depositFee to {} of seller(id:{})", count, outerCode, sellerId);

        sellerSettlementDao.batchUpdateOuterCode(outerCode, sellerId);
        log.info("handled {} sellerSettlement to {} of seller(id:{})", count, outerCode, sellerId);

        sellerAlipayCashDao.batchUpdateOuterCode(outerCode, sellerId);
        log.info("handled {} sellerAlipayCash to {} of seller(id:{})", count, outerCode, sellerId);

        depositFeeCashDao.batchUpdateOuterCode(outerCode, sellerId);
        log.info("handled {} depositFeeCash to {} of seller(id:{})", count, outerCode, sellerId);
    }

    /**
     * 标记订单及子订单的帐务记录为补帐
     * @param settlement  帐务
     */
    @Transactional
    public void fixed(Settlement settlement) {
        settlementDao.fixed(settlement);
        List<ItemSettlement> itemSettlements = itemSettlementDao.list(settlement.getOrderId());
        for (ItemSettlement itemSettlement : itemSettlements) {
            itemSettlementDao.fixed(itemSettlement.getId());
        }
    }
}
