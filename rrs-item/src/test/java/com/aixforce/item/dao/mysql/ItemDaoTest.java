package com.aixforce.item.dao.mysql;

import com.aixforce.common.model.Paging;
import com.aixforce.item.BaseDaoTest;
import com.aixforce.item.model.Item;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Map;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.Assert.assertThat;

/**
 * Author:  <a href="mailto:jlchen.cn@gmail.com">jlchen</a>
 * Date: 2013-01-31
 */
public class ItemDaoTest extends BaseDaoTest {

    @Autowired
    private ItemDao itemDao;

    private Item item;

    @Before
    public void setUp() throws Exception {
        item = new Item();
        item.setUserId(1L);
        item.setQuantity(2);
        item.setStatus(1);
        item.setName("aaa");
        item.setSpuId(1l);
        item.setRegion("1");
        item.setShopId(1l);
        item.setShopName("testshopname");
        item.setRegion("region1");
        itemDao.create(item);
    }

    @Test
    public void testFindById() throws Exception {
        Item actual = itemDao.findById(item.getId());
        assertThat(actual, notNullValue());
    }

    @Test
    public void testCreate() throws Exception {
        assertThat(item.getId(), notNullValue());
    }

    @Test
    public void testUpdate() throws Exception {
        item.setName("wat");
        itemDao.update(item);
        assertThat(itemDao.findById(item.getId()).getName(), is("wat"));
    }

    @Test
    public void testDelete() throws Exception {
        itemDao.delete(item.getId());
        assertThat(itemDao.findById(item.getId()), nullValue());
    }

    @Test
    public void testForDump() throws Exception {
        for (int i = 0; i < 20; i++) {
            itemDao.create(new Item());
        }
        Long maxId = itemDao.maxId();
        List<Item> firstPage = itemDao.forDump(maxId + 1, 10);
        assertThat(firstPage.size(), is(10));
        Long lastId = Iterables.getLast(firstPage).getId();
        List<Item> secondPage = itemDao.forDump(lastId, 10);
        assertThat(secondPage.size(), is(10));
        lastId = Iterables.getLast(secondPage).getId();
        List<Item> thirdPage = itemDao.forDump(lastId, 10);
        assertThat(thirdPage.size(), is(1));

    }

    @Test
    public void testBulkUpdateStatus() throws Exception {
        Item item2 = new Item();
        item2.setUserId(1L);
        itemDao.create(item2);
        itemDao.bulkUpdateStatus(1L, 1, ImmutableList.of(item.getId(), item2.getId()));

        List<Item> actual = itemDao.findByIds(ImmutableList.of(item.getId(), item2.getId()));
        for (Item i : actual) {
            assertThat(i.getStatus(), is(1));
            assertThat(i.getOnShelfAt(), notNullValue());
        }
    }

    @Test
    public void testChangeStock() throws Exception {
        itemDao.changeStock(item.getId(), 3);
        Item actual = itemDao.findById(item.getId());

        assertThat(actual.getQuantity(), is(5));

        itemDao.changeStock(item.getId(), -5);
        actual = itemDao.findById(item.getId());
        assertThat(actual.getQuantity(), is(0));
        assertThat(actual.getStatus(), is(-1));
    }

    @Test
    public void testSellerItems() throws Exception {
        for (int i = 0; i < 20; i++) {
            Item it = new Item();
            it.setUserId(11L);
            it.setStatus(Item.Status.ON_SHELF.toNumber());
            it.setQuantity(i);
            itemDao.create(it);
        }
        Map<String, Object> params = Maps.newHashMap();
        params.put("status", ImmutableList.of(Item.Status.ON_SHELF.toNumber()));
        Paging<Item> items = itemDao.sellerItems(11L, 0, 10, params);
        assertThat(items.getTotal(), is(20L));
    }

    @Test
    public void testUpdateItemStatus() {
        boolean update = itemDao.updateStatus(item.getId(), Item.Status.FROZEN.toNumber());
        assertThat(update, is(true));
        Item actual = itemDao.findById(item.getId());
        assertThat(actual.getStatus(), is(Item.Status.FROZEN.toNumber()));
    }

    @Test
    public void findAllItems() {
        List<Integer> status = Lists.newArrayList(0, 1, -1, -2);
        Paging<Item> item = itemDao.findAllItems(0, 20, status);
        assertThat(item.getTotal(), is(1L));
    }

    @Test
    public void findIdsBySellerId() {
        assertThat(itemDao.findIdsBySellerId(item.getUserId()).get(0), is(item.getId()));
    }

    @Test
    public void testCountBySpuId() {
        assertThat(itemDao.countBySpuId(1l), is(1));
    }

    @Test
    public void testFindBySpuId() {
        assertThat(itemDao.findBySpuId(1l), notNullValue());
    }

    @Test
    public void testFindByShopIdAndSpuId() {
        assertThat(itemDao.findBySellerIdAndSpuId(1l, 1l), notNullValue());
    }

    @Test
    public void testFindItemsByShopIdAndSpuId() {
        Item item1 = new Item();
        item1.setUserId(item.getUserId());
        item1.setSpuId(item.getSpuId());
        itemDao.create(item1);
        assertThat(itemDao.findItemsBySellerIdAndSpuId(item.getUserId(), item.getSpuId()).size(), is(2));
    }

    @Test
    public void testFindByShopId() {
        assertThat(itemDao.findByShopId(1l), notNullValue());
    }

    @Test
    public void testBatchUpdateItemRegion() {
        Item item2 = new Item();
        item2.setUserId(1l);
        item2.setShopId(1l);
        item2.setRegion("region2");
        itemDao.create(item2);
        itemDao.batchUpdateItemRegion(Lists.newArrayList(item.getId(), item2.getId()), "region3");
        assertThat(itemDao.findById(item.getId()).getRegion(), is("region3"));
        assertThat(itemDao.findById(item2.getId()).getRegion(), is("region3"));
    }

    @Test
    public void testCountOnShelfByShopId() {
        assertThat(itemDao.countOnShelfByShopId(1l), is(1l));
    }
}
