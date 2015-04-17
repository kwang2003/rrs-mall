package com.aixforce.rrs.coupon.dao;

import com.aixforce.common.model.Paging;
import com.aixforce.rrs.BaseDaoTest;
import com.aixforce.rrs.coupon.dao.CouponUsageDao;
import com.aixforce.rrs.coupon.model.CouponUsage;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.Assert.assertThat;

/**
 * Created by Effet on 4/22/14.
 */
public class CouponUsageDaoTest extends BaseDaoTest {

    @Autowired
    private CouponUsageDao couponUsageDao;

    private CouponUsage cu;

    public CouponUsage newCouponUsage(Long couponId, String couponName,
                                      Long buyerId, Long sellerId, String shopName,
                                      Integer amount, Date endAt) {
        CouponUsage cu = new CouponUsage();
        cu.setCouponId(couponId);
        cu.setCouponName(couponName);
        cu.setBuyerId(buyerId);
        cu.setSellerId(sellerId);
        cu.setShopName(shopName);
        cu.setAmount(amount);
        cu.setUnused(10);
        cu.setUsed(0);
        cu.setEndAt(endAt);
        return cu;
    }


    public CouponUsage oneCouponUsage() {
        return newCouponUsage(1L, "A Best Coupon", 30L, 1L, "Hot Shop", 300, DateTime.parse("2014-05-30").toDate());
    }

    public CouponUsage anotherCouponUsage() {
        return newCouponUsage(2L, "B Best CCC", 30L, 1L, "Hot Shop", 100, DateTime.parse("2015-06-30").toDate());
    }

    @Before
    public void setUp() throws Exception {
        cu = oneCouponUsage();
        couponUsageDao.create(cu);
    }

    @Test
    public void testDelete() {
        couponUsageDao.delete(cu.getId());
        assertThat(couponUsageDao.findById(cu.getId()), nullValue());
    }

    @Test
    public void testUpdate() {
        CouponUsage cus = new CouponUsage();
        cus.setId(cu.getId());
        cus.setUsed(cu.getUsed() + 1);
        cus.setUnused(cu.getUnused() - 1);
        couponUsageDao.update(cus);

        CouponUsage actual = couponUsageDao.findById(cu.getId());
        assertThat(actual.getUsed(), is(cus.getUsed()));
        assertThat(actual.getUnused(), is(cus.getUnused()));
        assertThat(actual.getSellerId(), is(cu.getSellerId()));
        assertThat(actual.getBuyerId(), is(cu.getBuyerId()));
    }

    @Test
    public void testFindById() {
        assertThat(couponUsageDao.findById(cu.getId()).getId(), equalTo(cu.getId()));
    }

    @Test
    public void testFindByCouponIdAndBuyerId() {
        CouponUsage cu2 = couponUsageDao.findByCouponIdAndBuyerId(cu.getCouponId(), cu.getBuyerId());
        assertThat(cu2.getId(), equalTo(cu.getId()));
        assertThat(cu2.getCouponId(), equalTo(cu.getCouponId()));
        assertThat(cu2.getBuyerId(), equalTo(cu.getBuyerId()));
    }

    @Test
    public void testFindBy() {
        CouponUsage another = anotherCouponUsage();
        couponUsageDao.create(another);

        CouponUsage criteria = new CouponUsage();
        criteria.setCouponId(2L);
        criteria.setBuyerId(30L);

        Paging<CouponUsage> couponUsages = couponUsageDao.findBy(criteria, 0, 5);
        assertThat(couponUsages.getTotal(), is(1L));
        assertThat(couponUsages.getData().get(0).getAmount(), equalTo(another.getAmount()));
    }

    @Test
    public void testFindByOrderBy() {
        CouponUsage another = anotherCouponUsage();
        couponUsageDao.create(another);

        CouponUsage criteria = new CouponUsage();
        criteria.setBuyerId(30L);
        criteria.setCouponName("Best");

        Paging<CouponUsage> couponUsages = couponUsageDao.findByOrderBy(criteria, "amount", 0, 5);
        assertThat(couponUsages.getTotal(), is(2L));
        assertThat(couponUsages.getData().get(0).getAmount(), is(cu.getAmount()));

        couponUsages = couponUsageDao.findByOrderBy(criteria, "endAt", 0, 5);
        assertThat(couponUsages.getTotal(), is(2L));
        assertThat(couponUsages.getData().get(0).getAmount(), is(another.getAmount()));
    }


}
