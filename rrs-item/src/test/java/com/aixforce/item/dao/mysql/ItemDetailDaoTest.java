package com.aixforce.item.dao.mysql;

import com.aixforce.item.BaseDaoTest;
import com.aixforce.item.model.ItemDetail;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.Assert.assertThat;

/**
 * Author:  <a href="mailto:jlchen.cn@gmail.com">jlchen</a>
 * Date: 2013-01-31
 */
public class ItemDetailDaoTest extends BaseDaoTest {

    @Autowired
    private ItemDetailDao itemDetailDao;

    private ItemDetail itemDetail;

    @Before
    public void setUp() throws Exception {
        itemDetail = new ItemDetail();
        itemDetail.setItemId(11L);
        itemDetailDao.create(itemDetail);
    }

    @Test
    public void testFindById() throws Exception {
        ItemDetail actual = itemDetailDao.findById(itemDetail.getId());
        assertThat(actual, notNullValue());
        assertThat(actual.getId(), is(itemDetail.getId()));
    }

    @Test
    public void testFindByItemId() throws Exception {
        ItemDetail actual = itemDetailDao.findById(itemDetail.getId());
        assertThat(actual, notNullValue());
        assertThat(actual.getItemId(), is(itemDetail.getItemId()));
    }

    @Test
    public void testCreate() throws Exception {
        assertThat(itemDetail.getId(), notNullValue());
    }

    @Test
    public void testUpdate() throws Exception {
        itemDetail.setImage1("world");
        itemDetail.setImage2("world");
        itemDetail.setImage3("world");
        itemDetail.setImage4("world");
        itemDetail.setFreightSize(100);
        itemDetail.setFreightWeight(200);
        itemDetail.setPackingList("world");
        itemDetailDao.update(itemDetail);
        assertThat(itemDetailDao.findById(itemDetail.getId()).getImage1(), is("world"));
        assertThat(itemDetailDao.findById(itemDetail.getId()).getImage2(), is("world"));
        assertThat(itemDetailDao.findById(itemDetail.getId()).getImage3(), is("world"));
        assertThat(itemDetailDao.findById(itemDetail.getId()).getImage4(), is("world"));
        assertThat(itemDetailDao.findById(itemDetail.getId()).getFreightSize(), is(100));
        assertThat(itemDetailDao.findById(itemDetail.getId()).getFreightWeight(), is(200));
        assertThat(itemDetailDao.findById(itemDetail.getId()).getPackingList(), is("world"));
    }

    @Test
    public void testUpdateByItemId() throws Exception {
        itemDetail.setImage1("image1");
        itemDetail.setImage2("image2");
        itemDetail.setImage3("image3");
        itemDetail.setImage4("image4");
        itemDetail.setFreightSize(100);
        itemDetail.setFreightWeight(200);
        itemDetail.setPackingList("packingList");
        itemDetailDao.updateByItemId(itemDetail);
        assertThat(itemDetailDao.findById(itemDetail.getId()).getImage1(), is("image1"));
        assertThat(itemDetailDao.findById(itemDetail.getId()).getImage2(), is("image2"));
        assertThat(itemDetailDao.findById(itemDetail.getId()).getImage3(), is("image3"));
        assertThat(itemDetailDao.findById(itemDetail.getId()).getImage4(), is("image4"));
        assertThat(itemDetailDao.findById(itemDetail.getId()).getFreightSize(), is(100));
        assertThat(itemDetailDao.findById(itemDetail.getId()).getFreightWeight(), is(200));
        assertThat(itemDetailDao.findById(itemDetail.getId()).getPackingList(), is("packingList"));
    }

    @Test
    public void testDeleteByItemId() throws Exception {
        itemDetailDao.deleteByItemId(itemDetail.getItemId());
        ItemDetail detail = itemDetailDao.findByItemId(itemDetail.getItemId());
        assertThat(detail, nullValue());
    }

    @Test
    public void testDelete() throws Exception {
        itemDetailDao.delete(itemDetail.getId());
        assertThat(itemDetailDao.findById(itemDetail.getId()), nullValue());
    }
}
