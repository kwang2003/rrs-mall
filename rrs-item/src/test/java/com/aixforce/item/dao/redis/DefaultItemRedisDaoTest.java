package com.aixforce.item.dao.redis;

import com.aixforce.item.BaseDaoTest;
import com.aixforce.item.model.DefaultItem;
import com.google.common.collect.Lists;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.assertThat;

/**
 * Created by yangzefeng on 13-12-18
 */
public class DefaultItemRedisDaoTest extends BaseDaoTest {

    @Autowired
    private DefaultItemRedisDao defaultItemDetailRedisDao;

    private long count = 1;

    @Before
    public void setUp() {
        newDefaultItem();
    }

    @Test
    @Ignore
    public void testCreateOrUpdate() throws Exception {

    }

    @Test
    public void testFindBySpuId() throws Exception {
        assertThat(defaultItemDetailRedisDao.findBySpuId(1l), notNullValue());
    }

    @Test
    public void testFindBySpuIds() {
        newDefaultItem();
        List<DefaultItem> defaultItems = defaultItemDetailRedisDao.findBySpuIds(Lists.newArrayList(1l, 2l));
        assertThat(defaultItems.size(), is(2));
        assertThat(defaultItems.get(0).getName(), is("testName1"));
    }

    private void newDefaultItem() {
        DefaultItem d = new DefaultItem();
        d.setSpuId(count);
        d.setMainImage("test.png");
        d.setName("testName"+count++);
        d.setPrice(10);
        defaultItemDetailRedisDao.create(d, null);
    }
}
