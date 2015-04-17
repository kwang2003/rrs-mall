package com.aixforce.item.service;

import com.aixforce.common.model.Paging;
import com.aixforce.common.model.Response;
import com.aixforce.item.model.TitleKeyword;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Map;


@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {
        "classpath*:/spring/mysql-dao-context-test.xml",
        "classpath*:/spring/item-service-context.xml"
})
@Component
@Slf4j
public class TitleKeywordServiceImplTest {

    @Autowired
    TitleKeywordService titleKeywordService;

    @Before
    public void setUp() throws Exception {
        testCreate();
    }

    @After
    public void tearDown() throws Exception {

    }

    @Test
    public void testCreate() throws Exception {

        TitleKeyword bean = new TitleKeyword();
        bean.setNameId(2014l);
        bean.setTitle("title-front");
        bean.setKeyword("cellphone");
        bean.setDesc("He need a phone");
        Assert.assertTrue(titleKeywordService.create(bean).getResult() == 1);
        log.info("[test] successfully insert into title_keyword with:{}",bean);

    }

//    @Test
//    public void testDeleteByCondition() throws Exception {
//
//        TitleKeyword bean = new TitleKeyword();
//        bean.setNameId(2014l);
////        bean.setId(1l);
//        Response<Long> longResponse = titleKeywordService.deleteById(bean);
//        System.out.println("delete num: "+longResponse.getResult());
//        Assert.assertEquals("One record must be delete", longResponse.getResult());
//        log.info("delte by id:[{}] finished",1l);
//
//    }

    @Test
    public void testFindById() throws Exception {

        Response<TitleKeyword> byId = titleKeywordService.findById(3l);
        System.out.println("find by id result:"+byId.getResult());
    }

    @Test
    public void testFindByNameId() throws Exception {

        testCreate();

        Response<TitleKeyword> response = titleKeywordService.findByNameId(2014l);

        System.out.println(response.getResult());

    }

    @Test
    public void testFindAll() throws Exception {
        Map<String, Object> params = Maps.newHashMap();
        Response<Paging<TitleKeyword>> pagingResponse = titleKeywordService.findAll(params, 0, 2);

        System.out.println("count: "+pagingResponse.getResult().getTotal());
        System.out.println("result: "+pagingResponse.getResult().getData());

    }

    @Test
    public void testUpdate() throws Exception {

        TitleKeyword bean = new TitleKeyword();
        bean.setId(8l);
        bean.setNameId(2014l);
        bean.setTitle("title-front[modified]");
        bean.setKeyword("cellphone[modified]");
        bean.setDesc("He need a phone[modified]");

        Response<Long> update = titleKeywordService.update(bean);

        Assert.assertTrue(update.getResult() == 1l);

        System.out.println("updated num: "+update.getResult());

    }
}