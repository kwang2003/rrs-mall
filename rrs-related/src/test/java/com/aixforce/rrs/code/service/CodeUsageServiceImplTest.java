package com.aixforce.rrs.code.service;

import com.aixforce.common.model.Paging;
import com.aixforce.common.model.Response;
import com.aixforce.rrs.BaseServiceTest;
import com.aixforce.rrs.code.dto.CodeOrderDto;
import com.aixforce.rrs.code.model.CodeUsage;
import com.aixforce.trade.model.Order;
import com.aixforce.trade.service.OrderWriteService;
import com.google.common.collect.Lists;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.unitils.spring.annotation.SpringBean;

import java.util.Date;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;

public class CodeUsageServiceImplTest extends BaseServiceTest {

    @SpringBean(value = "codeUsageServiceImpl")
    private CodeUsageService codeUsageService;
    @SpringBean(value = "orderWriteServiceImpl")
    private OrderWriteService orderWriteService;
    private CodeUsage codeUsage;
    private  Order order;
    @Before
    public void setUp() throws Exception {

        order=new Order();
        order.setCreatedAt(new Date());
        order.setBusiness(1l);
        order.setBuyerId(1l);
        order.setCanceledAt(new Date());
        order.setDeliveredAt(new Date());
        order.setDoneAt(new Date());
        order.setDeliverFee(100);
        order.setFee(20);
        order.setPaymentCode("222222222");
        order.setStatus(2);
        orderWriteService.createOrder(order);

        codeUsage=new CodeUsage();
        codeUsage.setCode("测试优惠码");
        codeUsage.setActivityId(1l);
        codeUsage.setActivityName("测试活动");
        codeUsage.setBusinessId(1l);
        codeUsage.setBuyerId(1l);
        codeUsage.setBuyerName("songrenfei");
        codeUsage.setChannelType(1);
        codeUsage.setActivityType(1);
        codeUsage.setSellerId(1l);
        codeUsage.setSellerName("test");
        codeUsage.setDiscount(1000);
        codeUsage.setOrderId(order.getId());
        codeUsage.setOriginPrice(200);
        codeUsage.setPrice(2000);
        codeUsage.setUsedCount(1);
        codeUsageService.create(codeUsage);



    }

    @Test
    public void testCreate() throws Exception {
        Assert.assertEquals(codeUsageService.findByName("测试优惠码").getResult().getCode(),"测试优惠码");

    }

    @Test
    public void testUpdate() throws Exception {
        testCreate();
        CodeUsage codeUsage=codeUsageService.findByName("测试优惠码").getResult();
        codeUsage.setCode("测试优惠码2");
       codeUsageService.update(codeUsage);
        Assert.assertEquals(codeUsageService.findByName("测试优惠码2").getResult().getCode(),"测试优惠码2");

    }

    @Test
    public void  testBatchUpdate() throws Exception{
        List<CodeUsage> usageList = Lists.newArrayList();
        codeUsage.setCode("111111111");
        usageList.add(codeUsage);
        Response<Boolean> b= codeUsageService.batchUpdateCodeUsage(usageList);

        assertThat(b.getResult(), is(Boolean.TRUE));
    }

    @Test
    public void  testBatchCreate() throws Exception{
        List<CodeUsage> usageList = Lists.newArrayList();
        CodeUsage codeUsage2= new CodeUsage();
        codeUsage2.setCode("测试优惠码2222");
        codeUsage2.setActivityId(1l);
        codeUsage2.setActivityName("测试活动");
        codeUsage2.setBusinessId(1l);
        codeUsage2.setBuyerId(1l);
        codeUsage2.setBuyerName("songrenfei");
        codeUsage2.setChannelType(1);
        codeUsage2.setSellerId(1l);
        codeUsage.setActivityType(1);
        codeUsage2.setSellerName("test");
        codeUsage2.setDiscount(1000);
        codeUsage2.setOrderId(1l);
        codeUsage2.setOriginPrice(200);
        codeUsage2.setPrice(2000);
        codeUsage2.setUsedCount(1);
        usageList.add(codeUsage);
        usageList.add(codeUsage2);
        Response<Boolean> b= codeUsageService.batchCreateCodeUsage(usageList);

        assertThat(b.getResult(), is(Boolean.TRUE));
    }

    @Test
    public void testGetCodeOrderDtoByActivityId(){

        Response<Paging<CodeOrderDto>> codeOrderDtoP =codeUsageService.getCodeOrderDtoByActivityId("1", 0, 5);
        Assert.assertNotNull(codeOrderDtoP);
    }


}
