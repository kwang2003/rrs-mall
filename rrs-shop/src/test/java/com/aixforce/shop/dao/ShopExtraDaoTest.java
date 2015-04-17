package com.aixforce.shop.dao;

import com.aixforce.common.model.Paging;
import com.aixforce.shop.model.ShopExtra;
import com.google.common.collect.Maps;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.Assert.assertThat;

/**
 * Created by IntelliJ IDEA.
 * User: AnsonChan
 * Date: 14-1-23
 */
public class ShopExtraDaoTest extends BaseDaoTest {
    @Autowired
    private ShopExtraDao shopExtraDao;

    private ShopExtra shopExtra;

    @Before
    public void init() {
        shopExtra = new ShopExtra();
        shopExtra.setShopId(1L);
        shopExtra.setRate(0.0000);
        shopExtra.setRateUpdating(0.0000);
        shopExtra.setOuterCode("wtf");
        shopExtra.setNtalkerId("hr01");
        shopExtra.setTechFeeNeed(0L);
        shopExtra.setDepositNeed(0L);
        shopExtraDao.create(shopExtra);
        ShopExtra actual = shopExtraDao.findById(shopExtra.getId());
        shopExtra.setCreatedAt(actual.getCreatedAt());
        shopExtra.setUpdatedAt(actual.getUpdatedAt());
        assertThat(shopExtra, is(actual));

    }

    @Test
    public void testFindBy() {
        Map<String, Object> params = Maps.newHashMap();
        Paging<ShopExtra> paging = shopExtraDao.findBy(params, 0, 10);
        assertThat(paging.getTotal(), is(1L));
        assertThat(paging.getData().size(), is(1));
    }

    @Test
    public void testCreate() {
        assertThat(shopExtra.getId(), notNullValue());
    }

    @Test
    public void testFindById() {
        assertThat(shopExtraDao.findById(shopExtra.getId()), notNullValue());
    }

    @Test
    public void testFindByShopId() {
        assertThat(shopExtraDao.findByShopId(shopExtra.getShopId()), notNullValue());
    }

    @Test
    public void testFindByOuterCode() {
        assertThat(shopExtraDao.findByOuterCode(shopExtra.getOuterCode()), notNullValue());
    }

    @Test
    public void testUpdate() {
        ShopExtra p = new ShopExtra();
        p.setId(shopExtra.getId());
        p.setOuterCode("wtf2");
        p.setNtalkerId("hr02");
        p.setRate(0.0000);
        p.setRateUpdating(0.0000);



        shopExtraDao.update(p);
        ShopExtra r = shopExtraDao.findById(p.getId());
        assertThat(r.getOuterCode(), is("wtf2"));
        assertThat(r.getNtalkerId(), is("hr02"));
        assertThat(r.getRate(), is(0.0000));
        assertThat(r.getRateUpdating(), is(0.0000));

        p.setRate(0.0000);
        p.setRateUpdating(null);
        shopExtraDao.update(p);
        r = shopExtraDao.findById(p.getId());
        assertThat(r.getRate(), is(0.0000));
        assertThat(r.getRateUpdating(), nullValue());

    }

    @Test
    public void testUpdateByShopId() {
        ShopExtra p = new ShopExtra();
        p.setShopId(shopExtra.getShopId());
        p.setOuterCode("wtf3");
        p.setNtalkerId("hr03");
        p.setDepositNeed(1000L);
        p.setTechFeeNeed(1000L);




        shopExtraDao.updateByShopId(p);
        ShopExtra r = shopExtraDao.findByShopId(p.getShopId());
        assertThat(r.getOuterCode(), is("wtf3"));
        assertThat(r.getNtalkerId(), is("hr03"));
        assertThat(r.getDepositNeed(), is(1000L));
        assertThat(r.getTechFeeNeed(), is(1000L));
    }
}
