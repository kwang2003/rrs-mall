package com.aixforce.rrs.settle.handle;

import com.aixforce.alipay.dto.AlipaySettlementResponse;
import com.aixforce.alipay.dto.settlement.AlipaySettlementDto;
import com.aixforce.alipay.request.PageQueryRequest;
import com.aixforce.alipay.request.Token;
import com.aixforce.common.model.PageInfo;
import com.aixforce.common.model.Paging;
import com.aixforce.common.utils.BeanMapper;
import com.aixforce.rrs.settle.dao.AlipayTransDao;
import com.aixforce.rrs.settle.dao.AlipayTransLoadDao;
import com.aixforce.rrs.settle.dao.ItemSettlementDao;
import com.aixforce.rrs.settle.dao.SettlementDao;
import com.aixforce.rrs.settle.enums.JobStatus;
import com.aixforce.rrs.settle.model.*;
import com.google.common.base.Objects;
import com.google.common.base.Splitter;
import com.google.common.base.Stopwatch;
import com.google.common.base.Throwables;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.aixforce.common.utils.Arguments.*;
import static org.elasticsearch.common.base.Preconditions.checkState;

/**
 * Mail: xiao@terminus.io <br>
 * Date: 2014-05-30 10:07 PM  <br>
 * Author: xiao
 */
@Slf4j
@Component
public class AlipayFeeHandle extends JobHandle {

    @Autowired
    private SettlementDao settlementDao;

    @Autowired
    private ItemSettlementDao itemSettlementDao;

    @Autowired
    private AlipayTransLoadDao alipayTransLoadDao;

    @Autowired
    private AlipayTransDao alipayTransDao;

    @Autowired
    private Token token;

    /**
     * 更新支付宝手续费
     *
     * @param job  任务信息
     */
    public void updateAlipayFees(SettleJob job) {
        if (equalWith(job.getStatus(), JobStatus.DONE.value())) return;      // 完成的任务无需再次处理

        log.info("[UPDATE-ALIPAY-FEE] begin at {}", DFT.print(DateTime.now()));
        Stopwatch stopwatch = Stopwatch.createStarted();
        try {
            checkState(dependencyOk(job), "job.dependency.not.over");
            settleJobDao.ing(job.getId());
            Date paidAt = job.getTradedAt();

            // 查询指定日期内完成在线支付的订单
            loadAlipayTransInfo(paidAt);
            // 处理合并支付的订单（仅包含普通订单）
            handleMultiPaid(paidAt);
            // 处理非合并支付的订单（包括普通和预售订单）
            handleNonMultiPaid(paidAt);

        } catch (IllegalStateException e) {
            log.error("[UPDATE-ALIPAY-FEE] failed with job:{}, error:{} ", job, e.getMessage());
            settleJobDao.fail(job.getId());
        } catch (Exception e) {
            log.error("[UPDATE-ALIPAY-FEE] failed with job:{}, cause:{} ", job, Throwables.getStackTraceAsString(e));
            settleJobDao.fail(job.getId());
        }

        stopwatch.stop();
        settleJobDao.done(job.getId(), stopwatch.elapsed(TimeUnit.SECONDS));
        log.info("[UPDATE-ALIPAY-FEE] begin at {}, cost {}", DFT.print(DateTime.now()), stopwatch.elapsed(TimeUnit.SECONDS));
    }



    private void handleNonMultiPaid(Date paidAt) {
        Integer pageNo = 1;
        PageInfo pageInfo = new PageInfo(pageNo, BATCH_SIZE);
        boolean next = batchUpdateAlipayFee(paidAt, pageInfo.offset, pageInfo.limit);
        while (next) {
            pageNo ++;
            pageInfo = new PageInfo(pageNo, BATCH_SIZE);
            next = batchUpdateAlipayFee(paidAt, pageInfo.offset, pageInfo.limit);
        }
    }

    private void handleMultiPaid(Date paidAt) {
        Integer pageNo = 1;
        PageInfo pageInfo = new PageInfo(pageNo, BATCH_SIZE);
        boolean next = batchUpdateMultiPaidAlipayFee(paidAt, pageInfo.offset, pageInfo.limit);
        while (next) {
            pageNo ++;
            pageInfo = new PageInfo(pageNo, BATCH_SIZE);
            next = batchUpdateMultiPaidAlipayFee(paidAt, pageInfo.offset, pageInfo.limit);
        }
    }

    private void loadAlipayTransInfo(Date paidAt) {
        Integer pageNo = 1;
        boolean next = batchLoadSettlementsOfAlipay(paidAt, pageNo, BATCH_SIZE);
        while (next) {
            next = batchLoadSettlementsOfAlipay(paidAt, pageNo, BATCH_SIZE);
            pageNo ++;
        }
    }


    /**
     * 批量导入支付宝对账记录
     *
     * @param paidAt        对账日期
     * @param pageNo        页码
     * @param pageSize      数量
     * @return  是否存在下一批数据
     */
    private boolean batchLoadSettlementsOfAlipay(Date paidAt, int pageNo, int pageSize) {
        Date queryStart = new DateTime(paidAt).withTimeAtStartOfDay().toDate();
        Date queryEnd = new DateTime(queryStart).plusDays(1).minusSeconds(1).toDate();
        AlipayTransLoad alipayTransLoad = new AlipayTransLoad(queryStart, queryEnd, pageNo, pageSize);
        // 之前已加载成功则不加载
        AlipayTransLoad existed = alipayTransLoadDao.getBy(alipayTransLoad);

        if (existed != null && existed.getStatus() == AlipayTransLoad.Status.DONE.value()) {
            log.info("skipped {}", existed);
            return existed.getNext();
        }

        boolean next = false;

        try {
            AlipaySettlementResponse result = PageQueryRequest.build(token)
                    .start(queryStart).end(queryEnd)
                    .pageNo(pageNo).pageSize(existed == null ? pageSize : existed.getPageSize()).query();
            checkState(result.isSuccess(), "settlement.alipay.trans.download.fail");
            List<AlipaySettlementDto> dtos = result.getResult().getPaging().getAccountLogList();
            for (AlipaySettlementDto dto : dtos) {
                AlipayTrans alipayTrans = new AlipayTrans();
                BeanMapper.copy(dto, alipayTrans);
                alipayTransDao.create(alipayTrans);
            }

            next = result.hasNextPage();
            alipayTransLoad.setPageSize(dtos.size());
            alipayTransLoad.setStatus(AlipayTransLoad.Status.DONE.value());
            alipayTransLoad.setNext(next);

        } catch (Exception e) {
            log.error("fail to load alipay settlements with queryStart:{}, queryEnd:{}, pageNo:{}, pageSize:{}",
                    queryStart, queryEnd, pageNo, pageSize, e);
            alipayTransLoad.setStatus(AlipayTransLoad.Status.FAIL.value());
        }

        alipayTransLoadDao.createOrUpdate(alipayTransLoad);
        return next;
    }

    /**
     * 批量更新合并支付的订单
     *
     * @param paidAt    支付日期
     * @param size      批次数量
     * @return 是否存在下一个批次
     */
    private boolean batchUpdateMultiPaidAlipayFee(Date paidAt, int pageSize, int size) {

        Settlement criteria = new Settlement();
        criteria.setPaidAt(paidAt);
        Paging<Settlement> settlementPaging = settlementDao.findOfMultiPaid(criteria, pageSize, size);
        long total = settlementPaging.getTotal();
        if (total == 0L) { return false; }

        List<Settlement> settlements = settlementPaging.getData();
        // 我们需要一个数据结构来关联 paymentCode 与合并支付的订单列表
        Multimap<String, Settlement> mappedSettlements = ArrayListMultimap.create();
        for (Settlement settlement : settlements) {
            mappedSettlements.put(settlement.getPaymentCode(), settlement);
        }

        for (String paymentCode : mappedSettlements.keySet()) {
            try {
                checkState(notEmpty(paymentCode), "settlement.payment.code.empty"); // 若交易流水为空则挂账

                AlipayTrans multiPaidTrans = getAlipayTrans(paymentCode);
                Collection<Settlement> multiPaidSettlements = mappedSettlements.get(paymentCode);
                String ids = multiPaidTrans.getMerchantOutOrderNo();
                List<String> idList = Splitter.on(",").splitToList(ids);
                List<Long> outerIds = convertToLong(idList);

                if (multiPaidSettlements.size() != outerIds.size())  {   // 数量不一致可能是由于分批处理造成，这里要采用补偿措施
                    List<Settlement> list = settlementDao.findByPaymentCode(paymentCode);
                    checkState(equalWith(list.size(), outerIds.size()), "settlement.order.count.mismatch");
                    mappedSettlements.removeAll(paymentCode);
                    mappedSettlements.putAll(paymentCode, list);
                }


                List<Long> orderIdsOfSettlement = idOfSettlements(multiPaidSettlements);
                // 如果有一个订单不在支付宝的帐务记录里面，就要集体挂账
                checkState(containOf(outerIds, orderIdsOfSettlement), "settlement.not.match.with.alipay");
                // 分账逻辑
                doAlipayFeeSharing(multiPaidSettlements, multiPaidTrans.getOutcomeOfFen(), multiPaidTrans.getTotalFeeOfFen());

                settlementManager.batchUpdate(multiPaidSettlements);


            } catch (IllegalStateException e) {
                log.error("fail to calculate settlements:{}, error:{}", mappedSettlements.get(paymentCode), e.getMessage());
                // 这里集体挂账
                doRecordIncorrect(mappedSettlements.get(paymentCode), e.getMessage());

            } catch (Exception e) {
                log.error("fail to calculate settlements:{}, cause:{}", mappedSettlements.get(paymentCode), Throwables.getStackTraceAsString(e));
                doRecordIncorrect(mappedSettlements.get(paymentCode), "settlement.fail");
                return false;
            }
        }

        int current = settlements.size();
        return current == size;  // 判断是否存在下一个要处理的批次
    }



    private void doAlipayFeeSharing(Collection<Settlement> multiPaidSettlements, Long outcomeOfAlipay, Long totalOfAlipay) {
        // 计算订单
        int count = multiPaidSettlements.size();
        int index = 0;
        Long sum = 0L;
        Long totalFee = 0L;

        for (Settlement settlement : multiPaidSettlements) {     // 前n-1笔按比例计算,第n笔算差值
            index ++;

            if (index == count) {
                settlement.setThirdPartyCommission(outcomeOfAlipay - sum);

            } else {
                checkState(notNull(settlement.getFee()), "settlement.multi.fee.empty");
                Double ratio = settlement.getFee() * 1.0 / totalOfAlipay;
                Double rest = Math.ceil(outcomeOfAlipay * ratio);
                Long outcomeOfCurrent = rest.longValue();
                settlement.setThirdPartyCommission(outcomeOfCurrent);
                sum += outcomeOfCurrent;
            }

            totalFee += settlement.getFee();
        }


        checkState(equalWith(totalFee, totalOfAlipay), "settlement.multi.total.not.match");
    }


    private List<Long> idOfSettlements(Collection<Settlement> multiPaidSettlements) {
        List<Long> ids = Lists.newArrayListWithCapacity(multiPaidSettlements.size());
        for (Settlement settlement : multiPaidSettlements) {
            ids.add(settlement.getOrderId());
        }
        return ids;
    }


    private List<Long> convertToLong(List<String> identities) {
        List<Long> ids = Lists.newArrayListWithCapacity(identities.size());
        for (String identity : identities) {
            ids.add(Long.valueOf(identity));
        }
        return ids;
    }



    private <T> boolean containOf(Collection<T> sources, Collection<T> targets) {

        for (T target : targets) {
            boolean contain = sources.contains(target);
            if (!contain) {
                return Boolean.FALSE;
            }
        }

        return Boolean.TRUE;
    }


    /**
     * 批量更新(T-1日)符合条件的订单支付宝手续费
     *
     * @param paidAt   交易时间
     * @param size      批次数量
     *
     * @return 是否存在下一批待处理数据
     */
    private boolean batchUpdateAlipayFee(Date paidAt, int pageSize, int size) {
        Settlement criteria = new Settlement();
        criteria.setPaidAt(paidAt);
        Paging<Settlement> settlementPaging = settlementDao.findOfNoMultiPaid(criteria, pageSize, size);
        long total = settlementPaging.getTotal();
        if (total == 0L) { return false; }

        List<Settlement> settlements = settlementPaging.getData();
        for (Settlement settlement : settlements) {    // 子订单不用计算手续费，也不用显示
            try {

                if (settlement.getThirdPartyCommission() != null) {  // 已更新的跳过
                    continue;
                }

                if (settlement.getPayType() == Settlement.PayType.COD.value()
                        && settlement.getType() == Settlement.Type.PLAIN.value()) {  // 货到付款无手续费
                    settlement.setThirdPartyCommission(0L);
                    settlementDao.update(settlement);
                    continue;
                }


                if (Objects.equal(settlement.getType(), Settlement.Type.PRE_SELL.value())) {
                    doCalculatePreSaleCommission(settlement);
                } else {
                    doCalculatePlainCommission(settlement);
                }
                settlementDao.update(settlement);

            } catch (IllegalStateException e) {
                log.error("fail to update fee or settlement:{}, error:{}", settlement, e.getMessage());
                doRecordIncorrect(Lists.newArrayList(settlement), e.getMessage());
            } catch (Exception e) {
                log.error("fail to update fee or settlement:{}, cause:{}", settlement, Throwables.getStackTraceAsString(e));
                doRecordIncorrect(Lists.newArrayList(settlement), "settlement.fail");
            }
        }

        int current = settlements.size();
        return current == size;  // 判断是否存在下一个要处理的批次
    }


    /**
     * 计算每笔预售订单的第三方手续费,预售需要根据子订单来计算
     *
     * @param settlement 订单结算信息
     */
    private void doCalculatePreSaleCommission(Settlement settlement) {
        ItemSettlement criteria = new ItemSettlement();
        criteria.setType(ItemSettlement.Type.PRESELL_DEPOSIT.value());
        criteria.setOrderId(settlement.getOrderId());

        // 定金单
        ItemSettlement orderOfDeposit = itemSettlementDao.getBy(criteria);
        String paymentCode = orderOfDeposit.getPaymentCode();
        checkState(notEmpty(paymentCode), "settlement.deposit.payment.code.empty");

        AlipayTrans transOfDeposit = getAlipayTrans(paymentCode);
        Long commission = transOfDeposit.getOutcomeOfFen();


        // 尾款单
        criteria.setType(ItemSettlement.Type.PRESELL_REST.value());
        ItemSettlement orderOfRest = itemSettlementDao.getBy(criteria);

        if (Objects.equal(orderOfRest.getPayType(), Settlement.PayType.COD.value())) {  // 预售的货到付款手续费0
            //总结算
            settlement.setThirdPartyCommission(0L);
            //更新定金子结算中得手续费
            ItemSettlement itemSettlementDeposit = new ItemSettlement();
            itemSettlementDeposit.setId(orderOfDeposit.getId());
            itemSettlementDeposit.setThirdPartyCommission(0l);
            itemSettlementDao.update(itemSettlementDeposit);
            //更新尾款子结算中得手续费
            ItemSettlement itemSettlementRest = new ItemSettlement();
            itemSettlementRest.setId(orderOfRest.getId());
            itemSettlementRest.setThirdPartyCommission(0l);
            itemSettlementDao.update(itemSettlementRest);
        } else if (orderOfRest==null||buyerNotPayRest(orderOfRest)||orderOfDeposit.getPaidAt()==null){   //不存在尾款或 在线支付但没有支付尾款的情况
            // 没有支付尾款的情况
            commission += 0;
            settlement.setThirdPartyCommission(commission);
            //更新子结算中得手续费
            ItemSettlement itemSettlementDeposit = new ItemSettlement();
            itemSettlementDeposit.setId(orderOfDeposit.getId());
            itemSettlementDeposit.setThirdPartyCommission(commission);
            itemSettlementDao.update(itemSettlementDeposit);

        } else {
            paymentCode = orderOfRest.getPaymentCode();
            checkState(notEmpty(paymentCode), "settlement.deposit.payment.code.empty");
            AlipayTrans transOfRest = getAlipayTrans(paymentCode);
            commission += transOfRest.getOutcomeOfFen();

            settlement.setThirdPartyCommission(commission);
            //更新定金子结算中得手续费
            ItemSettlement itemSettlementDeposit = new ItemSettlement();
            itemSettlementDeposit.setId(orderOfDeposit.getId());
            itemSettlementDeposit.setThirdPartyCommission(commission);
            itemSettlementDao.update(itemSettlementDeposit);
            //更新尾款子结算中得手续费
            ItemSettlement itemSettlementRest = new ItemSettlement();
            itemSettlementRest.setId(orderOfRest.getId());
            itemSettlementRest.setThirdPartyCommission(transOfRest.getOutcomeOfFen());
            itemSettlementDao.update(itemSettlementRest);
        }
    }


    private boolean buyerNotPayRest(ItemSettlement rest) {
        return equalWith(rest.getTradeStatus(), Settlement.TradeStatus.CANCELED_BY_REMAIN_EXPIRE.value())
                || equalWith(rest.getTradeStatus(), Settlement.TradeStatus.CANCELED_PRESALE_DEPOSIT_BY_BUYER.value());
    }

    private AlipayTrans getAlipayTrans(String paymentCode) {
        List<AlipayTrans> transes = alipayTransDao.findByTradeNo(paymentCode);
        checkState(transes.size() > 0, "settlement.alipay.trans.not.found");

        if (transes.size() == 1) {
            return transes.get(0);
        }

        AlipayTrans payTrans = null;

        for (AlipayTrans trans : transes) {
            String subTransCodeMsg = trans.getSubTransCodeMsg();
            if (notEmpty(subTransCodeMsg) && equalWith(subTransCodeMsg, "收费")) {
                return trans;
            }

            String transCodeMsg = trans.getTransCodeMsg();
            if (notEmpty(transCodeMsg) && equalWith(transCodeMsg, "在线支付")) {
                payTrans = trans;
            }
        }

        if (payTrans != null) {
            return payTrans;
        }

        throw new IllegalStateException("settlement.third.commission.not.match");
    }

    /**
     * 计算每笔普通订单的第三方手续费
     *
     * @param settlement 订单结算信息
     */
    private void doCalculatePlainCommission(Settlement settlement) {

        String paymentCode = settlement.getPaymentCode();
        checkState(notEmpty(paymentCode), "settlement.payment.code.empty");
        AlipayTrans trans = getAlipayTrans(paymentCode);

        Long commission = trans.getOutcomeOfFen();
        settlement.setThirdPartyCommission(commission);

        Long totalFee = trans.getTotalFeeOfFen();
        checkState(equalWith(totalFee, settlement.getFee()), "settlement.total.fee.not.match");
    }

}
