package com.aixforce.item.dao.mysql;

import com.aixforce.item.BaseDaoTest;
import com.aixforce.item.model.Sku;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.Assert.assertThat;

/**
 * Author:  <a href="mailto:jlchen.cn@gmail.com">jlchen</a>
 * Date: 2013-02-01
 */
public class SkuDaoTest extends BaseDaoTest {

    @Autowired
    private SkuDao skuDao;


    @Test
    public void testCreate() throws Exception {
        long itemId = 1L;
        List<Sku> skuList = ImmutableList.of(newSku(itemId, 88, "/image1", "black", "metal"),
                newSku(itemId, 99, "/image2", "yellow", "plastic"));
        skuDao.create(skuList);

        List<Sku> result = skuDao.findByItemId(itemId);
        assertThat(result.size(), is(2));
    }

    @Test
    public void testDelete() throws Exception {
        Sku sku = newSku(1L, 88, "/image1", "black", "metal");
        skuDao.create(sku);

        assertThat(sku.getId(), notNullValue());

        skuDao.delete(sku.getId());

        assertThat(skuDao.findById(sku.getId()), nullValue());
    }

    @Test
    public void testChangeStock() throws Exception {
        Sku sku = newSku(1L, 88, "/image1", "black", "metal");
        sku.setStock(100);
        skuDao.create(sku);

        assertThat(sku.getId(), notNullValue());
        skuDao.changeStock(sku.getId(), -2);

        assertThat(skuDao.findById(sku.getId()).getStock(), is(98));

        skuDao.changeStock(sku.getId(), 5);
        assertThat(skuDao.findById(sku.getId()).getStock(), is(103));
    }

    @Test
    public void testFindByIds() throws Exception{
        Sku sku = newSku(1L, 88, "/image1", "black", "metal");
        skuDao.create(sku);
        Sku sku1 = newSku(1L, 89, "/image1", "black", "metal");
        skuDao.create(sku1);

        List<Sku> skus = skuDao.findByIds(Lists.newArrayList(1l, 2l));
        assertThat(skus.size(), is(2));
        assertThat(skus.get(0).getPrice(), is(88));
    }

    @Test
    public void testFindByItemIds() throws Exception {
        Sku sku = newSku(1L, 88, "/image1", "black", "metal");
        skuDao.create(sku);
        Sku sku1 = newSku(2L, 89, "/image1", "black", "metal");
        skuDao.create(sku1);
        Sku sku2 = newSku(1L, 88, "/image1", "black", "metal");
        skuDao.create(sku2);

        List<Sku> skus = skuDao.findByItemIds(Lists.newArrayList(sku.getItemId(), sku1.getItemId()));
        assertThat(skus.size(), is(3));
    }

    private Sku newSku(long itemId, int price, String image, String attributeName1, String attributeName2) {
        Sku sku = new Sku();
        sku.setItemId(itemId);
        sku.setPrice(price);
        sku.setImage(image);
        sku.setAttributeName1(attributeName1);
        sku.setAttributeName2(attributeName2);
/*
        sku.setOuterId(new Random().nextInt(100)+"");
*/
        return sku;
    }
}
