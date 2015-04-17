package com.aixforce.shop.dao;

import com.aixforce.shop.manager.ShopManager;
import com.aixforce.shop.model.ShopExtra;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.Assert.assertEquals;

/**
 * Date: 7/7/14
 * Time: 15:30
 * Author: 2014年 <a href="mailto:dong.worker@gmail.com">张成栋</a>
 */
public class ShopManagerTest extends BaseDaoTest {

    @Autowired
    ShopExtraDao shopExtraDao;

    @Autowired
    ShopManager shopManager;

    ShopExtra extra;

    @Before
    public void setup() {
        extra = new ShopExtra();
        extra.setShopId(1l);
        extra.setRExpress(5l);
        extra.setRDescribe(5l);
        extra.setRService(5l);
        extra.setRQuality(5l);
        extra.setTradeQuantity(1l);
        shopExtraDao.create(extra);
    }

    @Test
    public void shouldFullUpdateShopExtras() {
        extra = new ShopExtra();
        extra.setShopId(2l);
        extra.setRExpress(5l);
        extra.setRDescribe(5l);
        extra.setRService(5l);
        extra.setRQuality(5l);
        extra.setTradeQuantity(1l);
        shopManager.fullUpdateShopExtraScore(extra);

        ShopExtra find = shopExtraDao.findById(2l);
        assertEquals(1L, (long)find.getTradeQuantity());
    }
}
