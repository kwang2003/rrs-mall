package com.aixforce.trade.dao;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

/**
 * Author:  <a href="mailto:jlchen.cn@gmail.com">jlchen</a>
 * Date: 2013-03-08
 */
public class CartDaoTest extends BaseDaoTest {
    @Autowired
    private CartDao cartDao;

    @Test
    public void testMarshal() throws Exception {
        Multiset<Long> skuIds = HashMultiset.create();
        skuIds.add(1L, 2);
        skuIds.add(3L, 1);
        String json = cartDao.marshal(skuIds);

        Multiset<Long> actual = cartDao.unmarshal(json);
        assertThat(actual, is(skuIds));
    }
}
