package com.aixforce.rrs.settle.jde;

import com.aixforce.rrs.jde.JdeClientImpl;
import com.aixforce.rrs.jde.JdeVoteResponse;
import com.aixforce.rrs.jde.JdeWriteResponse;
import com.aixforce.rrs.settle.model.DepositFee;
import com.aixforce.rrs.settle.model.SellerAlipayCash;
import com.aixforce.rrs.settle.model.SellerSettlement;
import com.google.common.collect.Lists;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

/**
 * Mail: remindxiao@gmail.com <br>
 * Date: 2014-03-03 2:33 PM  <br>
 * Author: xiao
 */
@Ignore
public class JdeClientImplTest {

    private SellerSettlement sellerSettlement;
    private DepositFee depositFee;
    private SellerAlipayCash sellerAlipayCash;
    private JdeClientImpl client;

    @Before
    public void setUp() {
        Long id = 5000L;
        String outerCode = "88888888";
        String writeUrl = "http://58.56.128.84:9001/EAI/RoutingProxyService/EAI_REST_POST_ServiceRoot?INT_CODE=EAI_INT_26192";
        String voteUrl = "http://58.56.128.84:9001/EAI/RoutingProxyService/EAI_REST_POST_ServiceRoot?INT_CODE=EAI_INT_26195";
//        String voteUrl = "http://58.56.128.10:19001/EAI/RoutingProxyService/EAI_REST_POST_ServiceRoot?INT_CODE=EAI_INT_1475";


        client = new JdeClientImpl(writeUrl, voteUrl);
        sellerSettlement = new SellerSettlement();
        sellerSettlement.setId(id);
        sellerSettlement.setSellerEarning(99999L);
        sellerSettlement.setOuterCode(outerCode);
        sellerSettlement.setThirdPartyCommission(999L);
        sellerSettlement.setRrsCommission(222L);
        sellerSettlement.setScoreEarning(111L);
        sellerSettlement.setBusiness(1L);
        sellerSettlement.setTotalExpenditure(99999L);
        sellerSettlement.setTotalEarning(99999L);
        sellerSettlement.setPresellDeposit(1000L);
        sellerSettlement.setPresellCommission(100L);



        depositFee = new DepositFee();
        depositFee.setId(id);
        depositFee.setDeposit(1020L);
        depositFee.setOuterCode(outerCode);

        sellerAlipayCash = new SellerAlipayCash();
        sellerAlipayCash.setId(id);
        sellerAlipayCash.setCashFee(2000122L);
        sellerAlipayCash.setOuterCode(outerCode);
    }



    @Ignore
    public void testSyncSellerEarningOk() {
        JdeWriteResponse response = client.syncSellerEarning(sellerSettlement);
        assertThat(response.isSuccess(), is(Boolean.TRUE));
    }

    @Ignore
    public void testSyncCommissionAndThirdOk(){
        JdeWriteResponse response = client.syncCommissionAndThird(sellerSettlement);
        assertThat(response.isSuccess(), is(Boolean.TRUE));
    }

    /**
     * 无佣金仅手续费的场景
     */
    @Ignore
    public void testSyncCommissionAndThirdNoCommissionOk(){
        sellerSettlement.setRrsCommission(0L);
        sellerSettlement.setId(sellerSettlement.getId() - 1);
        JdeWriteResponse response = client.syncCommissionAndThird(sellerSettlement);
        assertThat(response.isSuccess(), is(Boolean.TRUE));
    }

    @Ignore
    public void testSyncScoreEarningOk() {
        JdeWriteResponse response = client.syncScoreEarning(sellerSettlement);
        assertThat(response.isSuccess(), is(Boolean.TRUE));
    }

    @Ignore
    public void testSyncPaymentRefund() {
        JdeWriteResponse response = client.syncPaymentRefund(sellerSettlement);
        assertThat(response.isSuccess(), is(Boolean.TRUE));
    }


    @Test
    public void testSyncPresellDepositOk() {
        JdeWriteResponse response = client.syncPresellDeposit(sellerSettlement);
        assertThat(response.isSuccess(), is(Boolean.TRUE));
    }

    @Test
    public void testSyncDepositRefundOk() {
        depositFee.setType(DepositFee.Type.REFUND.value());
        JdeWriteResponse response = client.syncDepositRefund(depositFee);
        assertThat(response.isSuccess(), is(Boolean.TRUE));
    }


    @Ignore
    public void testSyncDepositPayOk() {
        depositFee.setType(DepositFee.Type.INCREMENT.value());
        JdeWriteResponse response = client.syncDepositPay(depositFee);
        assertThat(response.isSuccess(), is(Boolean.TRUE));
    }

    @Ignore
    public void testSyncTechFeeOrderOk() {
        depositFee.setType(DepositFee.Type.TECH_SERVICE.value());
        JdeWriteResponse response = client.syncTechFeeOrder(depositFee);
        assertThat(response.isSuccess(), is(Boolean.TRUE));
    }

    @Ignore
    public void testSyncTechFeeSettlementOk() {
        depositFee.setType(DepositFee.Type.TECH_SERVICE.value());
        JdeWriteResponse response = client.syncTechFeeSettlement(depositFee);
        assertThat(response.isSuccess(), is(Boolean.TRUE));
    }

    @Ignore
    public void testSyncAlipayCashOk() {
        JdeWriteResponse response = client.syncSellerAlipayCash(sellerAlipayCash);
        assertThat(response.isSuccess(), is(Boolean.TRUE));
    }


    @Ignore
    public void testSyncOrderTotalOk() {
        JdeWriteResponse response = client.syncSellerOrderTotal(sellerSettlement);
        assertThat(response.isSuccess(), is(Boolean.TRUE));
    }

    @Test
    public void testBatchSyncedDepositFees() {
        depositFee.setType(DepositFee.Type.REFUND.value());
        JdeWriteResponse response = client.batchSyncedDepositFees(Lists.newArrayList(depositFee));
        assertThat(response.isSuccess(), is(Boolean.TRUE));
    }

    @Ignore
    public void testBatchSyncedTechFees() {
        depositFee.setType(DepositFee.Type.TECH_SERVICE.value());
        JdeWriteResponse response = client.batchSyncedTechFees(Lists.newArrayList(depositFee));
        assertThat(response.isSuccess(), is(Boolean.TRUE));
    }

    @Test
    public void testSyncSellerSettlement() {
        JdeWriteResponse response = client.syncSellerSettlement(sellerSettlement);
        assertThat(response.isSuccess(), is(Boolean.TRUE));
    }

    @Test
    public void testSyncDeduction() {
        JdeWriteResponse response = client.syncDepositDeduction(depositFee);
        assertThat(response.isSuccess(), is(Boolean.TRUE));
    }


    @Test
    public void testPullVoteJde() {
        JdeVoteResponse response = client.pullVoucher("R10000000000002");
        assertThat(response, notNullValue());
        assertThat(response.getResult(), notNullValue());
        assertThat(response.getResult().isSuccess(), is(Boolean.TRUE));
    }


}
