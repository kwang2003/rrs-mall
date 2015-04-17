package com.aixforce.rrs.buying.dao;

import com.aixforce.common.model.Paging;
import com.aixforce.common.utils.JsonMapper;
import com.aixforce.rrs.BaseDaoTest;
import com.aixforce.rrs.buying.model.BuyingTempOrder;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;

public class BuyingTempOrderDaoTest extends BaseDaoTest {

    @Autowired
    private BuyingTempOrderDao buyingTempOrderDao;

    private BuyingTempOrder buyingTempOrder;

    private final static JsonMapper jsonMapper = JsonMapper.nonEmptyMapper();

    @Before
    public void setUp() throws Exception {
        buyingTempOrder = new BuyingTempOrder();
        buyingTempOrder.setOrderId(1l);
        buyingTempOrder.setBuyingActivityId(1l);
        buyingTempOrder.setItemId(2l);
        buyingTempOrder.setItemImage("image");
        buyingTempOrder.setItemName("name");
        buyingTempOrder.setSkuAttributeJson("json");
        buyingTempOrder.setSkuId(4l);
        buyingTempOrder.setBuyerId(2l);
        buyingTempOrder.setSellerId(2l);
        buyingTempOrder.setShopId(4l);
        buyingTempOrder.setSkuQuantity(3);
        buyingTempOrder.setBuyingPrice(34);
        buyingTempOrder.setTradeInfoId(3l);
        buyingTempOrder.setStatus(0);
        buyingTempOrder.setOrderCreatedAt(new Date());
        buyingTempOrder.setRegionId(1);
        buyingTempOrder.setCreatedAt(new Date());
        buyingTempOrder.setUpdatedAt(new Date());

        this.testCreate();
        this.testFindById();
    }


    public void testCreate() throws Exception {
       Long id = buyingTempOrderDao.create(buyingTempOrder);
       Assert.assertNotNull(id);

    }

    @Test
    public void testDelete() throws Exception {
       Boolean isDel = buyingTempOrderDao.delete(buyingTempOrder.getId());
       Assert.assertTrue(isDel);
    }

    @Test
    public void testUpdate() throws Exception {
        BuyingTempOrder newBuyingTempOrder = new BuyingTempOrder();
        newBuyingTempOrder.setBuyingActivityId(1l);
        newBuyingTempOrder.setId(buyingTempOrder.getId());
        Boolean isUpdate =  buyingTempOrderDao.update(newBuyingTempOrder);
        Assert.assertTrue(isUpdate);

    }

    @Test
    public void testFindById() throws Exception {
        BuyingTempOrder actual = buyingTempOrderDao.findById(buyingTempOrder.getId());
        buyingTempOrder.setCreatedAt(actual.getCreatedAt());
        buyingTempOrder.setUpdatedAt(actual.getUpdatedAt());
        Assert.assertThat(actual, is(buyingTempOrder));
    }


    @Test
    public void testPaging(){
        Map<String, Object> param = new HashMap<String, Object>();
        param.put("buyerId",2l);
        param.put("offset", 0);
        param.put("limit", 20);
        Paging<BuyingTempOrder> paging = buyingTempOrderDao.paging(param);
        Assert.assertNotNull(paging.getData());
    }

    @Test
    public void testGetSaleQuantity(){
        Integer total = buyingTempOrderDao.getSaleQuantity(buyingTempOrder.getBuyingActivityId(),buyingTempOrder.getItemId());
        Assert.assertNotNull(total);
    }

    @Test
    public void testMapToJson(){
        Map<String,String> map = new HashMap<String, String>();
        map.put("key1","value1");
        map.put("key2","value2");

        String json =  jsonMapper.toJson(map);
        System.out.print(json);
        Assert.assertNotNull(json);
    }

    @Test
    public void testFindByOrderId(){
        BuyingTempOrder buyingTempOrder1 = buyingTempOrderDao.findByOrderId(buyingTempOrder.getOrderId());
        Assert.assertNotNull(buyingTempOrder1);
    }

    @Test
    public void testFindInOrderIds() {
        List<BuyingTempOrder> actual = buyingTempOrderDao.findInOrderIds(Lists.newArrayList(buyingTempOrder.getId()));
        Assert.assertThat(actual.size(), is(1));
        Assert.assertThat(actual.get(0), is(buyingTempOrder));

    }
}