package com.aixforce.rrs.presale.dao;

import com.aixforce.common.model.Paging;
import com.aixforce.rrs.BaseDaoTest;
import com.aixforce.rrs.presale.dao.PreSaleDao;
import com.aixforce.rrs.presale.model.PreSale;
import com.google.common.collect.Lists;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.assertThat;

/**
 * Created by yangzefeng on 14-2-12
 */
public class PreSaleDaoTest extends BaseDaoTest {

    @Autowired
    private PreSaleDao preSaleDao;

    private PreSale p;

    private Date now = DateTime.now().toDate();

    @Before
    public void setUp() {
        p = new PreSale();
        p.setAdvertise("test");
        p.setSpuId(1l);
        p.setItemId(1L);
        p.setEarnest(100);
        p.setItemId(11l);

        p.setStatus(PreSale.Status.RELEASED.value());
        p.setByStorage(Boolean.FALSE);
        p.setReleasedAt(now);
        p.setEarnestTimeLimit(1);

        p.setPreSaleStartAt(now);
        p.setPreSaleFinishAt(now);
        p.setRemainStartAt(now);
        p.setRemainFinishAt(now);

        p.setStatus(PreSale.Status.NOT_RELEASED.value());

        p.setFakeSoldQuantity(100);

        preSaleDao.create(p);
        assertThat(p.getId(), notNullValue());
        PreSale actual = preSaleDao.get(p.getId());

        p.setCreatedAt(actual.getCreatedAt());
        p.setUpdatedAt(actual.getUpdatedAt());
        assertThat(p, is(actual));

    }


    @Test
    public void testFindById() throws Exception {
        assertThat(preSaleDao.get(p.getId()), notNullValue());
    }

    @Test
    public void testFindBySpuId() throws Exception {
        assertThat(preSaleDao.getBySpuId(1l), notNullValue());
    }

    @Test
    public void testUpdate() throws Exception {
        Date updateNow = DateTime.now().toDate();
        p.setShopIds("1,2,3");
        p.setPlainBuyLimit(1);
        p.setByStorage(Boolean.TRUE);
        p.setEarnest(100);
        p.setRemainMoney(900);
        p.setAdvertise("广告词");

        p.setPrice(1500);
        p.setStatus(PreSale.Status.RELEASED.value());
        p.setEarnestTimeLimit(8);

        p.setPreSaleStartAt(updateNow);
        p.setPreSaleFinishAt(updateNow);
        p.setRemainStartAt(updateNow);
        p.setRemainFinishAt(updateNow);
        p.setEarnest(200);

        p.setFakeSoldQuantity(1000);

        preSaleDao.update(p);
        PreSale actual = preSaleDao.get(p.getId());
        p.setUpdatedAt(actual.getUpdatedAt());

        assertThat(p, is(actual));
    }

    @Test
    public void testFindByIds() {
        PreSale preSale1 = new PreSale();
        preSale1.setAdvertise("test");
        preSale1.setSpuId(1l);
        preSale1.setItemId(1L);
        preSale1.setEarnest(100);
        preSaleDao.create(preSale1);
        assertThat(preSaleDao.findByIds(Lists.newArrayList(p.getId(), preSale1.getId())).size(), is(2));
    }

    @Test
    public void testFindByCriterion() {
        PreSale criterion = new PreSale();
        criterion.setStatus(PreSale.Status.RELEASED.value());
        assertThat(preSaleDao.findByCriterion(criterion, 0, 10).size(), is(0));
        criterion.setStatus(PreSale.Status.NOT_RELEASED.value());
        assertThat(preSaleDao.findByCriterion(criterion, 0, 10).size(), is(1));
        criterion.setStatus(null);
        assertThat(preSaleDao.findByCriterion(criterion, 0, 10).size(), is((1)));
    }

    @Test
    public void testCountBy() {
        PreSale criterion = new PreSale();
        criterion.setStatus(PreSale.Status.NOT_RELEASED.value());
        assertThat(preSaleDao.countBy(criterion), is(1l));
    }

    @Test
    public void testFindBy() {
        PreSale criteria = new PreSale();
        criteria.setStatus(PreSale.Status.NOT_RELEASED.value());
        criteria.setCreatedAt(now);
        criteria.setItemId(11L);
        Paging<PreSale> actual = preSaleDao.findBy(criteria, 0, 10);
        assertThat(actual.getData().size(), is(1));
        assertThat(actual.getData().get(0), is(p));


    }


}
