package com.aixforce.rrs.jde;

import com.aixforce.rrs.settle.model.DepositFee;
import com.aixforce.rrs.settle.model.DepositFeeCash;
import com.aixforce.rrs.settle.model.SellerAlipayCash;
import com.aixforce.rrs.settle.model.SellerSettlement;
import com.github.kevinsawicki.http.HttpRequest;
import com.google.common.base.Objects;
import com.google.common.collect.Lists;
import com.thoughtworks.xstream.XStream;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;

import java.util.List;

import static com.aixforce.common.utils.Arguments.equalWith;
import static com.aixforce.common.utils.Arguments.positive;

/**
 * Mail: remindxiao@gmail.com <br>
 * Date: 2014-02-19 2:08 PM  <br>
 * Author: xiao
 */
@Slf4j
public class JdeClientImpl implements JdeClient {
    private final static XStream voteMapper = new XStream();
    private final static XStream writeMapper = new XStream();
    private final static XStream requestMapper = new XStream();
    private static final String XML_HEADER = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n";

    static {
        voteMapper.autodetectAnnotations(true);
        voteMapper.processAnnotations(JdeResult.class);
        voteMapper.processAnnotations(JdeVoteResponse.class);

        writeMapper.autodetectAnnotations(true);
        writeMapper.aliasType("response", JdeWriteResponse.class);


        requestMapper.autodetectAnnotations(true);
        requestMapper.alias("dataList", List.class);
        requestMapper.setMode(XStream.NO_REFERENCES);
        requestMapper.processAnnotations(Jde.class);
    }

    private String eaiWriteUrl;      // JDE财务系统同步服务请求地址

    private String eaiVoteUrl;       // JDE财务系统获取凭证服务请求地址

    @SuppressWarnings("unused")
    public JdeClientImpl() {}

    public JdeClientImpl(String eaiWriteUrl, String eaiVoteUrl) {
        this.eaiWriteUrl = eaiWriteUrl;
        this.eaiVoteUrl = eaiVoteUrl;
    }

    /**
     * 通过EAI向JDE库中写入数据
     *
     * @param jde   JDE对象
     * @return      操作是否成功
     */
    private JdeWriteResponse write(Jde jde) {
        List<Jde> jdes = Lists.newArrayList(jde);
        return batchWrite(jdes);
    }

    private JdeWriteResponse batchWrite(List<Jde> jdes) {
        JdeWriteResponse result;

        try {
            String requestXml = requestMapper.toXML(jdes);
            requestXml = XML_HEADER + requestXml;
            String body = HttpRequest.
                    post(eaiWriteUrl, false).send(requestXml).connectTimeout(5000).readTimeout(5000).body();
            result = (JdeWriteResponse) writeMapper.fromXML(body);
            if (!result.isSuccess()) {
                log.info("jde ack: {}", body);
            }

            return result;
        } catch (Exception e) {
            log.error("batch write jde fail with url = {}, jde = {}", eaiWriteUrl, jdes, e);
            result = new JdeWriteResponse();
            result.setError("jde.batch.write.fail");
            return result;
        }
    }


    @Override
    public JdeWriteResponse syncSellerEarning(SellerSettlement sellerSettlement){
        Jde sellerEarning = Jde.sellerEarning(sellerSettlement.getId(), sellerSettlement.getOuterCode(),
                sellerSettlement.getSellerEarning(), DateTime.now().toDate(),
                sellerSettlement.getBusiness());
        return write(sellerEarning);
    }

    @Override
    public JdeWriteResponse syncCommissionAndThird(SellerSettlement sellerSettlement){
        Jde commissionAndThird = Jde.commissionAndThird(sellerSettlement.getId(), sellerSettlement.getOuterCode(),
                sellerSettlement.getRrsCommission(), sellerSettlement.getThirdPartyCommission(),
                DateTime.now().toDate(), sellerSettlement.getBusiness());
        return write(commissionAndThird);
    }


    @Override
    public JdeWriteResponse syncScoreEarning(SellerSettlement sellerSettlement) {
        Jde scoreEarning = Jde.score(sellerSettlement.getId(), sellerSettlement.getOuterCode(),
                sellerSettlement.getScoreEarning(), DateTime.now().toDate(), sellerSettlement.getBusiness());
        return write(scoreEarning);
    }

    @Override
    public JdeWriteResponse syncPresellDeposit(SellerSettlement sellerSettlement) {
        Jde presellDeposit = Jde.presell(sellerSettlement.getId(), sellerSettlement.getOuterCode(),
                sellerSettlement.getPresellDeposit(), sellerSettlement.getPresellCommission(),
                DateTime.now().toDate(), sellerSettlement.getBusiness());
        return write(presellDeposit);
    }


    @Override
    public JdeWriteResponse syncDepositRefund(DepositFee depositFee) {
        Jde depositRefund = Jde.depositRefund(depositFee.getId(), depositFee.getOuterCode(),
                depositFee.getDeposit(), DateTime.now().toDate(), depositFee.getBusiness());
        return write(depositRefund);
    }

    @Override
    public JdeWriteResponse syncPaymentRefund(SellerSettlement sellerSettlement) {
        Jde depositRefund = Jde.paymentRefund(sellerSettlement.getId(), sellerSettlement.getOuterCode(),
                0 - (sellerSettlement.getTotalExpenditure() == null ? 0 : sellerSettlement.getTotalExpenditure()),
                DateTime.now().toDate(), sellerSettlement.getBusiness());
        return write(depositRefund);
    }

    @Override
    public JdeWriteResponse syncDepositPay(DepositFee depositFee) {
        Jde depositPay = Jde.depositPay(depositFee.getId(), depositFee.getOuterCode(),
                depositFee.getDeposit(), DateTime.now().toDate(), depositFee.getBusiness(), depositFee.getPaymentType());
        return write(depositPay);

    }

    @Override
    public JdeWriteResponse syncTechFeeOrder(DepositFee techFee) {
        Jde depositPay = Jde.techFeeOrder(techFee.getId(), techFee.getOuterCode(),
                techFee.getDeposit(), DateTime.now().toDate(), techFee.getBusiness(), techFee.getPaymentType());
        return write(depositPay);
    }

    @Override
    public JdeWriteResponse syncTechFeeSettlement(DepositFee techFee) {
        Jde depositPay = Jde.techFeeSettlement(techFee.getId(), techFee.getOuterCode(),
                techFee.getDeposit(), DateTime.now().toDate(), techFee.getBusiness(), techFee.getPaymentType());
        return write(depositPay);
    }

    @Override
    public JdeWriteResponse syncSellerAlipayCash(SellerAlipayCash sellerAlipayCash) {
        Jde depositPay = Jde.alipayCash(sellerAlipayCash.getId(), sellerAlipayCash.getOuterCode(),
                sellerAlipayCash.getCashFee(), DateTime.now().toDate(), sellerAlipayCash.getBusiness());
        return write(depositPay);
    }

    @Override
    public JdeWriteResponse syncSellerOrderTotal(SellerSettlement sellerSettlement) {
        Jde orderTotal = Jde.orderTotal(sellerSettlement.getId(), sellerSettlement.getOuterCode(),
                sellerSettlement.getTotalEarning(), DateTime.now().toDate(), sellerSettlement.getBusiness());
        return write(orderTotal);
    }

    @Override
    public JdeWriteResponse syncDepositDeduction(DepositFee deduction) {
        Jde orderTotal = Jde.depositDeduction(deduction.getId(), deduction.getOuterCode(),
                deduction.getDeposit(), DateTime.now().toDate(), deduction.getBusiness());
        return write(orderTotal);
    }


    /**
     * 同步商户的汇总至JDE
     *
     * @param sellerSettlement 商户汇总信息
     * @return 是否同步成功
     */
    @Override
    public JdeWriteResponse syncSellerSettlement(SellerSettlement sellerSettlement) {
        List<Jde> jdes = Lists.newArrayListWithCapacity(10);
        // 订单总额
        if (positive(sellerSettlement.getTotalEarning())) {     // 订单总金额有一个不为0则需要同步
            if (!equalWith(sellerSettlement.getSellerEarning(), 0L)) {   // 只有返款不为0才同步总订单 2DZ
                Jde orderTotal = Jde.orderTotal(sellerSettlement.getId(), sellerSettlement.getOuterCode(),
                        sellerSettlement.getTotalEarning(), DateTime.now().toDate(), sellerSettlement.getBusiness());
                jdes.add(orderTotal);
            }
        }

        // 商家收入（返款）
        if (positive(sellerSettlement.getSellerEarning())) {  // 商户收入为正数时同步JDE 2FK
            Jde sellerEarning = Jde.sellerEarning(sellerSettlement.getId(), sellerSettlement.getOuterCode(),
                    sellerSettlement.getSellerEarning(), DateTime.now().toDate(),
                    sellerSettlement.getBusiness());
            jdes.add(sellerEarning);

        }


        if (positive(sellerSettlement.getRrsCommission()) || positive(sellerSettlement.getThirdPartyCommission())) {  // 手续费或佣金有一个为整数就要同步
            if (!equalWith(sellerSettlement.getSellerEarning(), 0L)) {   // 只有返款不为0才同步手续费和佣金  1FYD
                // 佣金及手续费
                Jde commissionAndThird = Jde.commissionAndThird(sellerSettlement.getId(), sellerSettlement.getOuterCode(),
                        sellerSettlement.getRrsCommission(), sellerSettlement.getThirdPartyCommission(),
                        DateTime.now().toDate(),sellerSettlement.getBusiness());
                jdes.add(commissionAndThird);
            }
        }


        if (sellerSettlement.getTotalExpenditure() != 0L) {
            // 退货款
            Jde depositRefund = Jde.paymentRefund(sellerSettlement.getId(), sellerSettlement.getOuterCode(),
                    0 - (sellerSettlement.getTotalExpenditure() == null ? 0 : sellerSettlement.getTotalExpenditure()),
                    DateTime.now().toDate(), sellerSettlement.getBusiness());
            jdes.add(depositRefund);
        }


        // 预售定金扣除
        if (sellerSettlement.getPresellDeposit() > 0L) {

            Jde presell = Jde.presell(sellerSettlement.getId(), sellerSettlement.getOuterCode(),
                    sellerSettlement.getPresellDeposit(), sellerSettlement.getPresellCommission(),
                    DateTime.now().toDate(), sellerSettlement.getBusiness());
            jdes.add(presell);
        }

        return batchWrite(jdes);
    }

    @Override
    public JdeVoteResponse pullVoucher(String serial) {
        String body = HttpRequest.get(eaiVoteUrl).form("sz55BQC", serial)
                .readTimeout(2000).connectTimeout(2000).body();
        return (JdeVoteResponse) voteMapper.fromXML(body);
    }

    @Override
    public JdeWriteResponse batchSyncedDepositFees(List<DepositFee> depositFees) {
        List<Jde> jdes = Lists.newArrayListWithCapacity(depositFees.size());
        for (DepositFee depositFee : depositFees) {
            Jde jde;
            int type = depositFee.getType();
            if (Objects.equal(type, DepositFee.Type.INCREMENT.value())) {       // 缴纳保证金
                jde = Jde.depositPay(depositFee.getId(), depositFee.getOuterCode(),
                        depositFee.getDeposit(), DateTime.now().toDate(), depositFee.getBusiness(), depositFee.getPaymentType());
                jdes.add(jde);
            } else if (Objects.equal(type, DepositFee.Type.REFUND.value())) {   // 退保证金
                jde = Jde.depositRefund(depositFee.getId(), depositFee.getOuterCode(),
                        depositFee.getDeposit(), DateTime.now().toDate(), depositFee.getBusiness());
                jdes.add(jde);
            } else if (Objects.equal(type, DepositFee.Type.DEDUCTION.value())) {   // 扣保证金
                jde = Jde.depositDeduction(depositFee.getId(), depositFee.getOuterCode(),
                        depositFee.getDeposit(), DateTime.now().toDate(), depositFee.getBusiness());
                jdes.add(jde);
            }

            log.warn("can not handle depositFee:{}", depositFee);
        }

        return batchWrite(jdes);
    }

    @Override
    public JdeWriteResponse batchSyncedTechFees(List<DepositFee> techFees) {
        List<Jde> jdes = Lists.newArrayListWithCapacity(techFees.size() * 2);
        for (DepositFee techFee : techFees) {
            int type = techFee.getType();
            if (!Objects.equal(type, DepositFee.Type.TECH_SERVICE.value())) {
                continue;
            }

            Jde order = Jde.techFeeOrder(techFee.getId(), techFee.getOuterCode(),   // 技术服务费订单
                    techFee.getDeposit(), DateTime.now().toDate(), techFee.getBusiness(), techFee.getPaymentType());
            Jde settlement = Jde.techFeeSettlement(techFee.getId(), techFee.getOuterCode(),  // 技术服务费对账
                    techFee.getDeposit(), DateTime.now().toDate(), techFee.getBusiness(), techFee.getPaymentType());

            jdes.add(order);
            jdes.add(settlement);
        }

        return batchWrite(jdes);
    }

    @Override
    public JdeWriteResponse batchSyncedDepositCash(List<DepositFeeCash> depositFeeCashes) {
        List<Jde> jdes = Lists.newArrayListWithCapacity(depositFeeCashes.size() * 2);
        for (DepositFeeCash depositFeeCash : depositFeeCashes) {

            Jde cash = Jde.depositCash(depositFeeCash.getId(), depositFeeCash.getOuterCode(),   // 技术服务费订单
                    depositFeeCash.getCashFee(), DateTime.now().toDate(), depositFeeCash.getBusiness());
            jdes.add(cash);
        }

        return batchWrite(jdes);
    }



}
