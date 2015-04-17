package com.aixforce.rrs.presale.dao;

import com.aixforce.rrs.BaseDaoTest;
import com.aixforce.rrs.presale.model.StorageStock;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.*;

public class StorageStockDaoTest extends BaseDaoTest{

    @Autowired
    private StorageStockDao storageStockDao;

    private StorageStock storageStock;

    @Before
    public void setUp() throws Exception {
        storageStock = new StorageStock();
        storageStock.setItemId(11L);
        storageStock.setStorageId(22L);
        storageStock.setInitStock(33);
        storageStock.setSoldCount(2);
        storageStockDao.create(storageStock);
    }

    @Test
    public void testFindByItemIdAndStorageId() throws Exception {
        StorageStock actual = storageStockDao.findByItemIdAndStorageId(storageStock.getItemId(), storageStock.getStorageId());
        assertThat(actual, is(storageStock));
    }

    @Test
    public void testChangeSoldCount() throws Exception {
        storageStockDao.changeSoldCount(2, storageStock.getItemId(), storageStock.getStorageId());
        StorageStock actual = storageStockDao.findByItemIdAndStorageId(storageStock.getItemId(), storageStock.getStorageId());
        assertThat(actual.getSoldCount(), is(storageStock.getSoldCount()+2));
    }

    @Test
    public void testFindById() throws Exception {
        assertThat(storageStockDao.findById(storageStock.getId()), notNullValue());
    }

    @Test
    public void testChangeInitStockAndSoldCount() throws Exception {
        StorageStock u = new StorageStock();
        u.setInitStock(100);
        u.setSoldCount(60);
        u.setItemId(storageStock.getItemId());
        u.setStorageId(storageStock.getStorageId());
        storageStockDao.changeInitStockAndSoldCount(u);

        StorageStock actual = storageStockDao.findByItemIdAndStorageId(storageStock.getItemId(), storageStock.getStorageId());
        assertThat(actual.getSoldCount(), is(u.getSoldCount()));
        assertThat(actual.getInitStock(), is(u.getInitStock()));
    }
}