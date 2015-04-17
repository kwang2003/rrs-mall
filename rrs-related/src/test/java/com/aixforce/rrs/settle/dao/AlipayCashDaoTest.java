package com.aixforce.rrs.settle.dao;

import com.aixforce.common.model.Paging;
import com.aixforce.rrs.BaseDaoTest;
import com.aixforce.rrs.settle.model.AlipayCash;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.springframework.beans.factory.annotation.Autowired;
import uk.co.it.modular.hamcrest.date.DateMatchers;

import java.util.Date;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.Assert.assertThat;

/**
 * Mail: remindxiao@gmail.com <br>
 * Date: 2014-01-23 2:23 PM  <br>
 * Author: xiao
 */
public class AlipayCashDaoTest extends BaseDaoTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();


    @Autowired
    private AlipayCashDao alipayCashDao;
    private AlipayCash a;
    private DateTime now = DateTime.now();
    

    private AlipayCash mock() {
        AlipayCash mock = new AlipayCash();
        mock.setTotalFee(10000L);
        mock.setAlipayFee(1000L);
        mock.setSummedAt(now.toDate());
        mock.setCashFee(mock.getTotalFee() - mock.getAlipayFee());
        mock.setStatus(AlipayCash.Status.NOT.value());
        mock.setCashTotalCount(1);
        return mock;
    }

    private void tearDown() {
        alipayCashDao.delete(a.getId());
    }


    @Before
    public void setUp() {
        a = mock();
        alipayCashDao.create(a);
        assertThat(a.getId(), notNullValue());
        AlipayCash actual = alipayCashDao.get(a.getId());
        a.setCreatedAt(actual.getCreatedAt());
        a.setUpdatedAt(actual.getUpdatedAt());
        assertThat(actual, is(a));
    }


    @Test
    public void testCreateExceptedException() {
        // 测试不允许插入重复日期的逻辑
        AlipayCash alipayCash = mock();
        alipayCash.setSummedAt(now.toDate());
        thrown.expect(IllegalStateException.class);
        thrown.expectMessage("alipay.cash.record.duplicate");
        alipayCashDao.create(alipayCash);
    }


    @Test
    public void testFindBy() {
        DateTime startAt  = now.minusDays(1).withTimeAtStartOfDay();
        DateTime endAt = now.plusDays(1);
        Paging<AlipayCash> actual = alipayCashDao.findBy(startAt.toDate(), endAt.toDate(), 0, 10);
        assertThat(actual.getTotal(), is(1L));
    }

    @Test
    public void testDone() {
        DateTime startAt  = now.minusDays(1).withTimeAtStartOfDay();
        DateTime endAt = now.plusSeconds(1);
        Paging<AlipayCash> paging = alipayCashDao.findBy(startAt.toDate(), endAt.toDate(), 0, 10);
        assertThat(paging.getTotal(),is(1L));
        Long id = paging.getData().get(0).getId();
        alipayCashDao.cashing(id);
        AlipayCash cash = alipayCashDao.get(id);
        int actual = cash.getStatus();
        assertThat(actual, is(AlipayCash.Status.DONE.value()));
    }

    @Test
    public void testGetBySummedAt() {
        AlipayCash actual = alipayCashDao.getBySummedAt(a.getSummedAt());
        assertThat(actual, notNullValue());
        assertThat(actual, is(a));
    }

    @Test
    public void testDelete() {
        alipayCashDao.delete(a.getId());
        AlipayCash actual = alipayCashDao.get(a.getId());
        assertThat(actual, nullValue());
    }


    /**
     * 测试填充20日前至昨天的数据，共计20条
     * 其中T-N 当N为单数时设定提现状态为“未提现”
     * 其中T-N 当N为偶数时设定提现状态为“已提现”
     *
     */
    private void preparePagingData() {
        for (int i = 0; i < 10; i++) {
            AlipayCash mock = mock();
            mock.setSummedAt(now.minusDays(i + 1).toDate());

            alipayCashDao.create(mock);

            if ((i + 1) % 2 == 0)  {
                mock.setStatus(AlipayCash.Status.DONE.value());
                alipayCashDao.cashing(mock.getId());
            }
        }
    }


    @Test
    public void testFindByWithAll() {
        // 测试分页前先清空所有数据
        tearDown();
        preparePagingData();

        Date summedStartAt = now.minusDays(10).withTimeAtStartOfDay().toDate();
        Date summedEndAt = now.toDate();

        // 查10天前至现在的数据
        Paging<AlipayCash> actual = alipayCashDao.findBy(summedStartAt, summedEndAt, 0, 10);
        assertThat(actual.getTotal(), is(10L));
        assertThat(actual.getData().size(), is(10));
        for (AlipayCash entity : actual.getData()) {
            assertThat(entity.getSummedAt(), DateMatchers.before(summedEndAt));
            assertThat(entity.getSummedAt(), DateMatchers.after(summedStartAt));
        }

        // 查状态为已提现的记录
        AlipayCash criteria = new AlipayCash();
        criteria.setStatus(AlipayCash.Status.DONE.value());
        actual = alipayCashDao.findBy(criteria, 0, 10);
        assertThat(actual.getTotal(), is(5L));
        for (AlipayCash entity : actual.getData()) {
            assertThat(entity.getStatus(), is(AlipayCash.Status.DONE.value()));
        }
                                            
        // 查找两天前指定时间段内的数据
        Date twoDayBefore = now.minusDays(2).toDate();
        criteria.setSummedAt(twoDayBefore);
        criteria.setStatus(AlipayCash.Status.DONE.value());
        actual = alipayCashDao.findBy(criteria, 0, 10);
        assertThat(actual.getTotal(), is(1L));
        assertThat(actual.getData().get(0).getSummedAt(), DateMatchers.after(now.minusDays(2).withTimeAtStartOfDay().toDate()));
        assertThat(actual.getData().get(0).getSummedAt(), DateMatchers.before(now.minusDays(1).withTimeAtStartOfDay().toDate()));


        // 查找当天前的数据，用来测试返回空列表的场景
        criteria.setSummedAt(now.toDate());
        actual = alipayCashDao.findBy(criteria, 0, 10);
        assertThat(actual.getTotal(), is(0L));
    }

    @Test
    public void testUpdate() {
        AlipayCash updating = new AlipayCash();
        updating.setCashTotalCount(10000);
        updating.setId(a.getId());

        alipayCashDao.update(updating);
        AlipayCash actual = alipayCashDao.get(a.getId());

        assertThat(actual.getCashTotalCount(), is(10000));
    }

}
