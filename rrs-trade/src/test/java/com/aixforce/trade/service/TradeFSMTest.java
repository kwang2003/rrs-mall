package com.aixforce.trade.service;

import com.aixforce.trade.model.Order;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

/**
 * Author:  <a href="mailto:jlchen.cn@gmail.com">jlchen</a>
 * Date: 2013-03-07
 */
public class TradeFSMTest {
    TradeFSM tradeFSM;

    @Before
    public void setUp() throws Exception {
        tradeFSM = new TradeFSM(Order.Status.WAIT_FOR_PAY);
    }

    @Test
    public void testBuyerPaid() throws Exception {
        tradeFSM.buyerPaid();
        assertThat(tradeFSM.getCurrentState(), is(Order.Status.PAID));
    }

    @Test
    public void testSellerDelivered() throws Exception {
        tradeFSM.buyerPaid();
        tradeFSM.sellerDelivered();
        assertThat(tradeFSM.getCurrentState(), is(Order.Status.DELIVERED));
    }

    @Test
    public void testConfirmed() throws Exception {
        tradeFSM.buyerPaid();
        tradeFSM.sellerDelivered();
        tradeFSM.confirmed();
        assertThat(tradeFSM.getCurrentState(), is(Order.Status.DONE));
    }

    @Test(expected = Exception.class)
    public void testSellerDeliveredFailed() throws Exception {
        tradeFSM.sellerDelivered();
    }
}
