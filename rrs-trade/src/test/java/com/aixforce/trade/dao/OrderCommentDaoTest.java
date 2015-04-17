package com.aixforce.trade.dao;

import com.aixforce.common.model.Paging;
import com.aixforce.trade.model.OrderComment;
import com.google.common.collect.Lists;
import org.joda.time.DateTime;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * Date: 14-2-12
 * Time: PM5:40
 * Author: 2014年 <a href="mailto:dong.worker@gmail.com">张成栋</a>
 */

public class OrderCommentDaoTest extends BaseDaoTest {

    @Autowired
    private OrderCommentDao commentDao;

    private OrderComment comment;

    private OrderComment generator (Long orderId, String comment) {
        OrderComment c = new OrderComment();
        c.setOrderItemId(orderId);
        c.setComment(comment);

        c.setOrderType(1);
        c.setBuyerId(1l);
        c.setItemId(1l);
        c.setShopId(2l);
        c.setRDescribe(3);
        return c;
    }

    @Before
    public void setup() throws Exception{
        comment = generator(1l, "hehe");
        commentDao.create(comment);
        this.create();
    }

    public void create(){
        OrderComment c = new OrderComment();
        c.setOrderItemId(1l);
        c.setComment("ni hao");
        c.setOrderType(1);
        c.setBuyerId(1l);
        c.setItemId(1l);
        c.setShopId(1l);
        c.setRDescribe(3);
        c.setIsBaskOrder(true);
        commentDao.create(c);
    }

    @Test
    public void shouldFindById() {
        OrderComment c = commentDao.findById(1l);
        assertEquals(comment, c);
    }

    @Test
    public void shouldFindItemComment() {
        comment = generator(2l, "haha");
        commentDao.create(comment);

        Paging<OrderComment> comments = commentDao.viewItemComments(1l, 0, 20);
        assertEquals(Long.valueOf(2l), comments.getTotal());
    }

    @Test
    public void shouldFindAnyInIds() {
        OrderComment p = generator(3l, "haha");
        commentDao.create(p);

        List<String> ids = Lists.newArrayList("1", "2", "3");
        List<OrderComment> c = commentDao.findAnyByOrderItemId(ids);
        assertEquals(2, c.size());
    }

    @Test
    public void shouldFindByShopId() {
        OrderComment p = generator(3l, "haha");
        p.setShopId(44l);
        commentDao.create(p);

        Paging<OrderComment> comments = commentDao.findByShopId(44l, 0, 20);
        assertEquals(Long.valueOf(1l), comments.getTotal());
    }

    @Test
    public void shouldFindYesterday() {
        threeYesterdayComment();
        Paging<OrderComment> ocp = commentDao.getYesterdayCommentForShopId(3L, 0, 200);

        assertEquals((long)ocp.getTotal(), 3L);
    }

    @Test
    public void shouldFindMaxIdForShop() {
        Long c = commentDao.maxId(1l);

        assertEquals(c, comment.getId());
    }

    @Test
    public void shouldSumUpShopsScore() {
        threeYesterdayComment();

        OrderComment oc = commentDao.sumUpShopScore(3l);
        assertEquals(15L, (long)oc.getRExpress());
        assertEquals(3L, (long)oc.getTradeQuantity());
    }

    private void threeYesterdayComment() {
        OrderComment oc = generator(1L, "good");
        Date dt = DateTime.now().withTimeAtStartOfDay().minusDays(1).plusHours(3).toDate();

        oc.setBuyerId(16L);
        oc.setShopId(3L);
        oc.setItemId(1L);
        oc.setRExpress(5);
        oc.setRService(4);
        oc.setRDescribe(3);
        oc.setRQuality(2);
        oc.setCreatedAt(dt);
        oc.setUpdatedAt(dt);

        commentDao.create(oc);
        oc.setId(null);
        commentDao.create(oc);
        oc.setId(null);
        commentDao.create(oc);
    }

    @Test
    public void testSumUpForYesterdayGroupByShop(){
        List<OrderComment> orderCommentList=commentDao.sumUpForYesterdayGroupByShop();
        for(OrderComment orderComment : orderCommentList){

            System.out.print(orderComment.getShopId()+"---------"+orderComment.getRDescribe());
        }
        Assert.assertNotNull(orderCommentList);

    }

    @Test
    public void testCommentReply(){

        Boolean isUpdate = commentDao.commentReply(comment.getId(),"回复1");
        Assert.assertTrue(isUpdate);

        OrderComment c = commentDao.findById(comment.getId());
        Assert.assertEquals("回复1",c.getCommentReply());


    }
}
