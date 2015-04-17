package com.aixforce.rrs.purify.service;

import com.aixforce.item.dao.mysql.ItemDao;
import com.aixforce.item.model.Item;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Transactional;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {
        "classpath:/spring/related-service-test.xml"
})
@TransactionConfiguration(transactionManager = "transactionManager", defaultRollback = true)
@Transactional
public class PurifyPageServiceTest {
    @Autowired
    private PurifyPageService purifyPageService;

    @Autowired
    private ItemDao itemDao;

    @Before
    public void setUp() throws Exception {
        Item item = new Item();
        item.setUserId(1L);
        item.setQuantity(2);
        item.setStatus(1);
        item.setName("aaa");
        item.setSpuId(1l);
        item.setRegion("1");
        item.setShopId(1l);
        item.setRegion("测试产品");
        itemDao.create(item);
    }

    @Test
    public void step0(){
        purifyPageService.findPurifyPageInfo(1l , null);
    }

    @Test
    public void step1(){
        purifyPageService.findPurifyPageInfo(1l , new Long[]{1l});
    }

    @Test
    public void step2(){
        purifyPageService.findPurifyPageInfo(1l , new Long[]{1l , 6l});
    }

    @Test
    public void step3(){
        purifyPageService.findPurifyPageInfo(1l , new Long[]{1l , 6l, 8l});
    }

    @Test
    public void step4(){
        purifyPageService.findPurifyPageInfo(1l , new Long[]{1l , 6l, 8l, 11l});
    }

    @Test
    public void step5(){
        purifyPageService.findPurifyPageInfo(1l , new Long[]{1l , 6l, 8l, 11l, 14l});
    }
}
