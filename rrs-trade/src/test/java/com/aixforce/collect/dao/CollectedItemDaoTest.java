package com.aixforce.collect.dao;

import com.aixforce.collect.model.CollectedItem;
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
public class CollectedItemDaoTest extends BaseDaoTest {

    @Autowired
    private CollectedItemDao collectedItemDao;

    private CollectedItem c;


    @Before
    public void setUp() throws Exception {
        c = mock();
        collectedItemDao.create(c);

        CollectedItem actual = collectedItemDao.get(c.getId());
        c.setCreatedAt(actual.getCreatedAt());
        c.setUpdatedAt(actual.getUpdatedAt());
        assertThat(actual, is(c));
    }

    private CollectedItem mock() {
        CollectedItem mock = new CollectedItem();
        mock.setBuyerId(1L);
        mock.setItemId(1L);
        mock.setItemNameSnapshot("收藏商品");
        return mock;
    }

    private void tearDown(Long id) {
        collectedItemDao.delete(id);
    }


    @Test
    public void testFindBy() {
        tearDown(c.getId());
        CollectedItem item1 = mock();
        item1.setItemId(1L);
        item1.setItemNameSnapshot("卡萨蒂");
        collectedItemDao.create(item1);
        item1 = collectedItemDao.get(item1.getId());


        CollectedItem item2 = mock();
        item2.setItemId(2L);
        item2.setItemNameSnapshot("海尔");
        collectedItemDao.create(item2);
        item2 = collectedItemDao.get(item2.getId());

        // 测试根据用户名筛选
        CollectedItem criteria = new CollectedItem();
        criteria.setBuyerId(1L);
        Paging<CollectedItem> actual = collectedItemDao.findBy(criteria, 0, 10);
        assertThat(actual.getTotal(), is(2L));
        assertThat(actual.getData(), contains(item2, item1));


        // 测试根据名称筛选
        criteria = new CollectedItem();
        criteria.setItemNameSnapshot("海尔");
        actual = collectedItemDao.findBy(criteria, 0, 10);
        assertThat(actual.getTotal(), is(1L));
        assertThat(actual.getData(), contains(item2));
    }

    @Test
    public void testGetByUserIdAndItemId() {
        CollectedItem actual = collectedItemDao.getByUserIdAndItemId(1L, c.getItemId());
        assertThat(actual, is(c));
    }

    @Test
    public void testCountOf() {
        Long actual = collectedItemDao.countOf(1L);
        assertThat(actual, is(1L));
        actual = collectedItemDao.countOf(-1L);
        assertThat(actual, is(0L));
    }


}
