package com.aixforce.rrs.coupon.dao;

import com.aixforce.common.model.Paging;
import com.aixforce.rrs.BaseDaoTest;
import com.aixforce.rrs.coupon.dao.CouponDao;
import com.aixforce.rrs.coupon.model.Coupon;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;
import java.util.List;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.Assert.assertThat;

/**
 * Created by Effet on 4/22/14.
 */
public class CouponDaoTest extends BaseDaoTest {

    @Autowired
    private CouponDao couponDao;

    private Coupon coupon;

    public Coupon newCoupon(String name, Long shopId, String shopName, Long sellerId,
                            Integer amount, Integer useLimit, Integer type, Integer status,
                            Date startAt, Date endAt) {
        Coupon cp = new Coupon();
        cp.setName(name);
        cp.setShopId(shopId);
        cp.setShopName(shopName);
        cp.setSellerId(sellerId);
        cp.setAmount(amount);
        cp.setUseLimit(useLimit);
        cp.setType(type);
        cp.setStatus(status);
        cp.setType(0);
        cp.setTaken(0);
        cp.setUsed(0);
        cp.setClicked(0);
        cp.setStartAt(startAt);
        cp.setEndAt(endAt);
        return cp;
    }

    public Coupon oneCoupon() {
        return newCoupon("A Better Coupon", 10L, "Best Shop", 20L,
                300, 400, Coupon.Type.OBTAINED.value(), Coupon.Status.INIT.value(),
                DateTime.parse("2014-04-05").toDate(), DateTime.parse("2014-05-30").toDate());
    }

    public Coupon anotherCoupon() {
        return newCoupon("B Better Coupon", 10L, "Best Shop", 20L,
                300, 400, Coupon.Type.OBTAINED.value(), Coupon.Status.INIT.value(),
                DateTime.parse("2014-04-05").toDate(), DateTime.parse("2014-05-30").toDate());
    }

    @Before
    public void setUp() throws Exception {
        coupon = oneCoupon();
        couponDao.create(coupon);
    }

    @Test
    public void testDelete() {
        couponDao.delete(coupon.getId());
        assertThat(couponDao.findById(coupon.getId()), nullValue());
    }

    @Test
    public void testUpdate() {
        Coupon cp = new Coupon();
        cp.setId(coupon.getId());
        cp.setStatus(Coupon.Status.SUSPEND.value());
        couponDao.update(cp);

        Coupon actual = couponDao.findById(coupon.getId());
        assertThat(actual.getStatus(), is(cp.getStatus()));
        assertThat(actual.getUpdatedAt(), notNullValue());
        assertThat(actual.getSellerId(), is(coupon.getSellerId()));
        assertThat(actual.getAmount(), is(coupon.getAmount()));
    }

    @Test
    public void testFindById() {
        assertThat(couponDao.findById(coupon.getId()).getShopName(), equalTo(coupon.getShopName()));
    }

    @Test
    public void testFindBy() {
        Coupon another = anotherCoupon();
        couponDao.create(another);

        Coupon criteria = new Coupon();
        criteria.setName("Bet");
        criteria.setStatus(Coupon.Status.INIT.value());
        criteria.setAmount(300);

        Paging<Coupon> coupons = couponDao.findBy(criteria, 0, 3);

        assertThat(coupons.getTotal(), is(2L));
        assertThat(coupons.getData().get(0).getSellerId(), equalTo(coupon.getSellerId()));
    }

    @Test
    public void testFindByAll() {
        Coupon another = anotherCoupon();
        couponDao.create(another);

        Coupon criteria = new Coupon();
        criteria.setName("Bet");
        criteria.setStatus(Coupon.Status.INIT.value());
        criteria.setAmount(300);

        List<Coupon> coupons = couponDao.findAllBy(criteria);

        assertThat(coupons.size(), is(2));
    }
}
