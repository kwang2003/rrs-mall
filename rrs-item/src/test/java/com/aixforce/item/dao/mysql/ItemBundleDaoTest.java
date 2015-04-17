package com.aixforce.item.dao.mysql;

import com.aixforce.item.BaseDaoTest;
import com.aixforce.item.model.ItemBundle;
import org.hamcrest.core.Is;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.Assert.assertThat;

/**
 * Created by yangzefeng on 14-4-21
 */
public class ItemBundleDaoTest extends BaseDaoTest {

    private ItemBundle itemBundle;

    @Autowired
    private ItemBundleDao itemBundleDao;

    @Before
    public void init() {
        itemBundle = new ItemBundle();
        itemBundle.setItemId1(1l);
        itemBundle.setItemId2(2l);
        itemBundle.setItem1Quantity(2);
        itemBundle.setItem2Quantity(3);
        itemBundle.setName("test");
        itemBundle.setDesc("test");
        itemBundle.setSellerId(1l);
        itemBundle.setOriginalPrice(20);
        itemBundle.setPrice(10);
        itemBundle.setSellerId(1l);
        itemBundle.setStatus(ItemBundle.Status.OnShelf.toNumber());
        itemBundleDao.create(itemBundle);
    }

    @Test
    public void testCreate() throws Exception {
        assertThat(itemBundle.getId(), notNullValue());
    }

    @Test
    public void testUpdate() throws Exception {
        itemBundle.setStatus(ItemBundle.Status.OffShelf.toNumber());
        itemBundle.setOriginalPrice(200);
        itemBundle.setPrice(100);
        itemBundle.setDesc("test1");
        itemBundle.setName("name");
        itemBundleDao.update(itemBundle);
        ItemBundle actual = itemBundleDao.findById(itemBundle.getId());
        assertThat(actual.getStatus(), Is.is(ItemBundle.Status.OffShelf.toNumber()));
    }

    @Test
    public void testDelete() throws Exception {
        itemBundleDao.delete(itemBundle.getId());
        assertThat(itemBundleDao.findById(itemBundle.getId()), nullValue());
    }

    @Test
    public void testFindBySellerId() throws Exception {
        List<ItemBundle> itemBundles = itemBundleDao.findBySellerId(itemBundle.getSellerId());
        assertThat(itemBundles.size(), is(1));
        assertThat(itemBundles.get(0).getId(), Is.is(itemBundle.getId()));
    }
}
