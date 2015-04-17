package com.aixforce.shop.dao;

import com.aixforce.common.model.Paging;
import com.aixforce.shop.model.Shop;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.hamcrest.Matchers;
import org.hamcrest.core.Is;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.assertThat;

/**
 * Author:  <a href="mailto:jlchen.cn@gmail.com">jlchen</a>
 * Date: 2013-10-29
 */
public class ShopDaoTest extends BaseDaoTest {

    private int sequence = 0;

    @Autowired
    private ShopDao shopDao;

    private Shop shop;

    @Before
    public void setUp() throws Exception {
        shop = mock();
        shopDao.create(shop);
    }

    private Shop mock() {
        Shop shop = new Shop();
        shop.setUserId(2L);
        shop.setUserName("admin");
        shop.setName("ddbao");
        shop.setBusinessId(11L);
        shop.setStatus(Shop.Status.OK.value());
        shop.setTaxRegisterNo("12345678");
        shop.setIsCod(Boolean.TRUE);
        shop.setEInvoice(Boolean.TRUE);
        shop.setVatInvoice(Boolean.TRUE);
        return shop;
    }


    @Test
    public void testFindByUserId() throws Exception {
        assertThat(shopDao.findByUserId(shop.getUserId()), notNullValue());
    }

    @Test
    public void testCreate() throws Exception {
        assertThat(shop.getId(), notNullValue());
        assertThat(shop.getTaxRegisterNo(), is("12345678"));
    }

    @Test
    public void testUpdate() throws Exception {
//        Shop u = new Shop();
//        u.setId(shop.getId());
//        u.setName("dithub");
//        u.setImageUrl("http://www.google.com");
//        u.setStatus(Shop.Status.FROZEN.value());
//        shopDao.update(u);
        shop.setStatus(Shop.Status.FROZEN.value());
        shop.setIsCod(Boolean.FALSE);
        shop.setEInvoice(Boolean.FALSE);
        shop.setVatInvoice(Boolean.FALSE);
        shop.setTaxRegisterNo("AABBCCDDEE");
        shop.setBusinessId(8L);
        shopDao.update(shop);
        Shop actual = shopDao.findById(shop.getId());
        assertThat(actual.getStatus(), is(-1));
        assertThat(actual.getIsCod(), is(Boolean.FALSE));
        assertThat(actual.getEInvoice(), is(Boolean.FALSE));
        assertThat(actual.getVatInvoice(), is(Boolean.FALSE));
        assertThat(actual.getBusinessId(), is(8L));
        assertThat(actual.getTaxRegisterNo(), is("AABBCCDDEE"));
//        Shop actual = shopDao.findById(u.getId());
//        assertThat(actual.getName(),is(u.getName()));
//        assertThat(actual.getImageUrl(),is(u.getImageUrl()));
//        assertThat(actual.getStatus(),is(-1));
    }

    @Test
    public void testUpdateStatus() throws Exception {
        assertThat(shopDao.updateStatus(shop.getId(), Shop.Status.FROZEN), is(true));
        Shop actual = shopDao.findById(shop.getId());
        assertThat(actual.getStatus(), is(Shop.Status.FROZEN.value()));
    }

    @Test
    public void testDelete() throws Exception {
        assertThat(shopDao.delete(shop.getId()), is(true));
    }

    @Test
    public void testFindById() throws Exception {
        assertThat(shopDao.findById(shop.getId()), Matchers.notNullValue());
    }

    @Test
    public void testFindByStatus() {
        Paging<Shop> shopPaging = shopDao.findByStatus(0, 10, Shop.Status.OK.value());
        assertThat(shopPaging.getTotal(), is(1L));
    }

    @Test
    public void testBatchUpdateStatus() {
        Shop shop1 = new Shop();
        shop1.setStatus(Shop.Status.OK.value());
        shop1.setUserName("test");
        shop1.setName("test");
        shop1.setUserId(1l);
        shop1.setBusinessId(10L);
        shopDao.create(shop1);
        shopDao.batchUpdateStatus(Lists.newArrayList(shop.getId(), shop1.getId()), Shop.Status.FROZEN.value());
        Long count = shopDao.findByStatus(0, 1, Shop.Status.FROZEN.value()).getTotal();
        assertThat(count, is(2l));
    }


    @Test
    public void testFindByName() {
        Shop shop1 = shopDao.findByName(shop.getName());
        assertThat(shop1.getId(), notNullValue());
    }

    @Test
    public void testShops() {
        Paging<Shop> shopP = shopDao.shops(0,20,null,null,"admin");
        assertThat(shopP.getTotal(), is(1l));
        assertThat(shopP.getData().get(0).getId(),is(shop.getId()));
    }

    private Shop newShop() {
        int s = sequence++;
        shop = new Shop();
        shop.setUserId(2L);
        shop.setUserName("admin" + s);
        shop.setName("ddbao" + s);
        shop.setBusinessId(11L);
        shop.setStatus(Shop.Status.OK.value());
        shop.setIsCod(Boolean.TRUE);
        return shop;
    }


    @Test
    public void testFindBy() {
        assertThat(shopDao.delete(shop.getId()), is(Boolean.TRUE));

        Shop fail = newShop();
        fail.setStatus(Shop.Status.FAIL.value());
        shopDao.create(fail);


        Shop frozen = newShop();
        frozen.setStatus(Shop.Status.FROZEN.value());
        shopDao.create(frozen);

        Shop ok = newShop();
        ok.setStatus(Shop.Status.OK.value());
        shopDao.create(ok);

        Shop init = newShop();
        init.setStatus(Shop.Status.INIT.value());
        shopDao.create(init);

        Map<String, Object> params = Maps.newHashMap();
        params.put("offset", 0);
        params.put("limit", 100);


        Paging<Shop> actual = shopDao.findBy(params);
        assertThat(actual.getTotal(), Is.is(4L));
        assertThat(actual.getData().size(), Is.is(4));


        params.put("statuses", Lists.newArrayList(1, -1, -2));
        actual = shopDao.findBy(params);
        assertThat(actual.getTotal(), Is.is(3L));
        assertThat(actual.getData().size(), Is.is(3));
    }


    @Test
    public void testFindByTaxRegisterNo() {
        Shop shop1 = mock();
        shop1.setTaxRegisterNo("888888888888888");
        shop1.setName("shop1");
        shop1.setUserId(8L);
        shop1.setUserName("user1");
        shopDao.create(shop1);

        Shop shop2 = mock();
        shop2.setTaxRegisterNo("888888888888888");
        shop2.setUserId(9L);
        shop2.setUserName("user2");
        shop2.setName("shop2");
        shopDao.create(shop2);

        List<Shop> actual = shopDao.findByTaxRegisterNo("888888888888888");
        assertThat(actual, contains(shop1, shop2));
    }

    @Test
    public void testFindExtraWithTaxNo() {
        Shop shopWithoutTaxNo = mock();
        shopWithoutTaxNo.setTaxRegisterNo(null);
        shopWithoutTaxNo.setName("没有税号的店铺");
        shopDao.create(shopWithoutTaxNo);

        Paging<Shop> paging = shopDao.findWithTaxNo(0, 100);

        assertThat(paging.getTotal(), is(1L));
        assertThat(paging.getData().get(0), is(shop));
    }

}
