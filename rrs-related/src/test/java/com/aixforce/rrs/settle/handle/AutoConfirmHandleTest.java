package com.aixforce.rrs.settle.handle;

import com.aixforce.common.model.Paging;
import com.aixforce.rrs.settle.dao.SettleJobDao;
import com.aixforce.rrs.settle.dao.SettlementDao;
import com.aixforce.rrs.settle.enums.JobStatus;
import com.aixforce.rrs.settle.manager.DepositManager;
import com.aixforce.rrs.settle.model.SettleJob;
import com.aixforce.rrs.settle.model.Settlement;
import com.google.common.collect.Lists;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Date;
import java.util.List;

import static org.mockito.Matchers.anyMap;
import static org.mockito.Mockito.*;

/**
 * Mail: xiao@terminus.io <br>
 * Date: 2014-08-01 3:36 PM  <br>
 * Author: xiao
 */
@SuppressWarnings("all")
public class AutoConfirmHandleTest {

    @InjectMocks
    private AutoConfirmHandle autoConfirmHandle;

    @Mock
    private SettlementDao settlementDao;

    @Mock
    private SettleJobDao settleJobDao;

    @Mock
    private DepositManager depositManager;

    private Date now = DateTime.now().toDate();


    private SettleJob getSettleJob() {
        SettleJob settleJob = new SettleJob();
        settleJob.setId(1L);
        settleJob.setDoneAt(DateTime.parse("2014-07-14").toDate());
        settleJob.setTradedAt(DateTime.parse("2014-07-13").toDate());
        settleJob.setStatus(JobStatus.NOT.value());
        return settleJob;
    }

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);
        autoConfirmHandle.setPermitDay(7);
        autoConfirmHandle.setThreshold(0);
    }

    private Settlement mock(Long id) {
        Settlement settlement = new Settlement();
        settlement.setId(id);
        settlement.setConfirmedAt(now);
        settlement.setConfirmed(Settlement.Confirmed.NOT.value());
        settlement.setSettleStatus(Settlement.SettleStatus.CONFIRMING.value());
        settlement.setTradeStatus(Settlement.TradeStatus.DONE.value());


        return settlement;
    }

    private Paging<Settlement> getSettlementPaging() {
        Paging<Settlement> paging = new Paging<Settlement>();
        List<Settlement> settlements = Lists.newArrayList();

        // 已经确认
        Settlement hasConfirmed = mock(1L);
        hasConfirmed.setConfirmed(Settlement.Confirmed.DONE.value());
        hasConfirmed.setSellerId(1L);
        settlements.add(hasConfirmed);

        // 普通在线支付完成的订单-待确认-账户被锁定
        Settlement plainOnlineSuccess = mock(2L);
        plainOnlineSuccess.setSellerId(2L);
        plainOnlineSuccess.setType(Settlement.Type.PLAIN.value());
        plainOnlineSuccess.setPayType(Settlement.PayType.ONLINE.value());
        plainOnlineSuccess.setTradeStatus(Settlement.TradeStatus.DONE.value());
        settlements.add(plainOnlineSuccess);


        // 普通在线支付完成的订单-待确认-账户被锁定
        Settlement presellSuccess = mock(3L);
        presellSuccess.setSellerId(3L);
        presellSuccess.setType(Settlement.Type.PRE_SELL.value());
        presellSuccess.setPayType(Settlement.PayType.ONLINE.value());
        presellSuccess.setTradeStatus(Settlement.TradeStatus.DONE.value());
        settlements.add(presellSuccess);

        // 应该确认的订单
        Settlement shouldConfirm = mock(4L);
        shouldConfirm.setSellerId(4L);
        shouldConfirm.setType(Settlement.Type.PLAIN.value());
        shouldConfirm.setTradeStatus(Settlement.TradeStatus.DONE.value());
//        when(depositManager.isAccountLocked(3L, anyInt())).thenReturn(Boolean.TRUE);
        settlements.add(shouldConfirm);


        paging.setTotal((long)settlements.size());
        paging.setData(settlements);
        return paging;
    }





    @Test
    public void testAutoConfirmSuccess() {
        when(settlementDao.findBy(anyMap())).thenReturn(getSettlementPaging());

        when(depositManager.isAccountLocked(2L, 0)).thenReturn(Boolean.TRUE);
        when(depositManager.isAccountLocked(3L, 0)).thenReturn(Boolean.TRUE);
        when(depositManager.isAccountLocked(4L, 0)).thenReturn(Boolean.FALSE);

        when(settlementDao.confirmed(anyLong())).thenReturn(Boolean.TRUE);
        autoConfirmHandle.autoConfirm(getSettleJob());

        verify(settlementDao, times(0)).confirmed(1L);
        verify(settlementDao, times(0)).confirmed(2L);
        verify(settlementDao, times(0)).confirmed(3L);
        verify(settlementDao, times(1)).confirmed(4L);
    }

    @Test
    public void testAutoConfirmWithUnExceptedException() {
        when(settlementDao.findBy(anyMap())).thenReturn(getSettlementPaging());
        when(depositManager.isAccountLocked(2L, null)).thenThrow(new NullPointerException("12345"));
        autoConfirmHandle.autoConfirm(getSettleJob());
    }


    @Test
    public void testAutoConfirmWithExceptedException() {
        when(settlementDao.findBy(anyMap())).thenReturn(getSettlementPaging());
        SettleJob jobNotDone = getSettleJob();
        jobNotDone.setStatus(JobStatus.NOT.value());
        when(settleJobDao.get(anyLong())).thenReturn(jobNotDone);

        SettleJob job = getSettleJob();
        job.setDependencyId(1L);
        autoConfirmHandle.autoConfirm(job);
    }





}
