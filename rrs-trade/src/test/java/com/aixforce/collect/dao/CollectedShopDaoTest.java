package com.aixforce.collect.dao;

import com.aixforce.collect.model.CollectedShop;
import com.aixforce.common.model.Paging;
import com.aixforce.trade.dao.BaseDaoTest;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.contains;
import static org.junit.Assert.assertThat;

/**
 * Mail: xiao@terminus.io <br>
 * Date: 2014-10-10 4:27 PM  <br>
 * Author: xiao
 */
public class CollectedShopDaoTest extends BaseDaoTest {

    @Autowired
    private CollectedShopDao collectedShopDao;

    private CollectedShop c;


    @Before
    public void setUp() throws Exception {
        c = mock();
        collectedShopDao.create(c);

        CollectedShop actual = collectedShopDao.get(c.getId());
        c.setCreatedAt(actual.getCreatedAt());
        c.setUpdatedAt(actual.getUpdatedAt());
        assertThat(actual, is(c));
    }

    private CollectedShop mock() {
        CollectedShop mock = new CollectedShop();
        mock.setBuyerId(1L);
        mock.setSellerId(2L);
        mock.setShopId(1L);
        mock.setShopNameSnapshot("收藏商品");
        return mock;
    }

    private void tearDown(Long id) {
        collectedShopDao.delete(id);
    }


    @Test
    public void testFindBy() {
        tearDown(c.getId());
        CollectedShop shop1 = mock();
        shop1.setShopId(1L);
        shop1.setShopNameSnapshot("卡萨蒂");
        collectedShopDao.create(shop1);
        shop1 = collectedShopDao.get(shop1.getId());


        CollectedShop shop2 = mock();
        shop2.setShopId(2L);
        shop2.setShopNameSnapshot("海尔");
        collectedShopDao.create(shop2);
        shop2 = collectedShopDao.get(shop2.getId());

        // 测试根据用户名筛选
        CollectedShop criteria = new CollectedShop();
        criteria.setBuyerId(1L);
        Paging<CollectedShop> actual = collectedShopDao.findBy(criteria, 0, 10);
        assertThat(actual.getTotal(), is(2L));
        assertThat(actual.getData(), contains(shop2, shop1));


        // 测试根据名称筛选
        criteria = new CollectedShop();
        criteria.setShopNameSnapshot("海尔");
        actual = collectedShopDao.findBy(criteria, 0, 10);
        assertThat(actual.getTotal(), is(1L));
        assertThat(actual.getData(), contains(shop2));
    }

    @Test
    public void testGetByUserIdAndItemId() {
        CollectedShop actual = collectedShopDao.getByUserIdAndShopId(1L, c.getShopId());
        assertThat(actual, is(c));
    }

    @Test
    public void testCountOf() {
        Long actual = collectedShopDao.countOf(1L);
        assertThat(actual, is(1L));
        actual = collectedShopDao.countOf(-1L);
        assertThat(actual, is(0L));
    }

}
