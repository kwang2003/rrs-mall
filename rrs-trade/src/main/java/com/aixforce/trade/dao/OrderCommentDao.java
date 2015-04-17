package com.aixforce.trade.dao;

import com.aixforce.common.model.Paging;
import com.aixforce.trade.model.OrderComment;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.mybatis.spring.support.SqlSessionDaoSupport;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Date: 14-2-12
 * Time: PM5:35
 * Author: 2014年 <a href="mailto:dong.worker@gmail.com">张成栋</a>
 */

@Repository
public class OrderCommentDao extends SqlSessionDaoSupport {

    private static final DateTimeFormatter DATE_TIME_FORMAT = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");


    public Long create(OrderComment comment) {
        getSqlSession().insert("OrderComment.create", comment);
        return comment.getId();
    }

    public OrderComment findById(Long id) {
        return getSqlSession().selectOne("OrderComment.findById", id);
    }

    public OrderComment findByOrderItemId(Long id) {
        return getSqlSession().selectOne("OrderComment.findByOrderId", id);
    }

    public List<OrderComment> findAnyByOrderItemId(List<String> idList) {

        return getSqlSession().selectList("OrderComment.findAnyByOrderItemId", idList.toArray());
    }

    public Paging<OrderComment> viewItemComments(Long itemId, Integer offset, Integer size) {
        Map<String, Object> params = Maps.newHashMap();
        params.put("itemId", itemId);
        params.put("offset", offset);
        params.put("limit", size);

        Long count = getSqlSession().selectOne("OrderComment.countOf", params);
        if (count==0) {
            return new Paging<OrderComment>(0L, Collections.<OrderComment>emptyList());
        }

        List<OrderComment> comments  = getSqlSession().selectList("OrderComment.findBy", params);
        return new Paging<OrderComment>(count, comments);
    }

    public Paging<OrderComment> findByBuyerId(Long buyerId, Integer offset, Integer size) {
        Map<String, Object> params = Maps.newHashMap();
        params.put("buyerId", buyerId);
        params.put("offset", offset);
        params.put("limit", size);
        Long count = getSqlSession().selectOne("OrderComment.countOf", params);
        if (count == 0) {
            return new Paging<OrderComment>(0L, Collections.<OrderComment>emptyList());
        }
        List<OrderComment> comments = getSqlSession().selectList("OrderComment.findBy", params);
        return new Paging<OrderComment>(count, comments);
    }

    public Paging<OrderComment> findByShopId(Long shopId, Integer offset, Integer size) {
        Map<String, Object> params = Maps.newHashMap();
        params.put("shopId", shopId);
        params.put("offset", offset);
        params.put("limit", size);

        List<OrderComment> comments;
        Long count = getSqlSession().selectOne("OrderComment.countOf", params);
        if (count==0) {
            return new Paging<OrderComment>(0L, Collections.<OrderComment>emptyList());
        }

        comments = getSqlSession().selectList("OrderComment.findBy", params);
        return new Paging<OrderComment>(count, comments);
    }

    public Paging<OrderComment> getYesterdayCommentForShopId(Long shopId, Integer offset, Integer size) {
        final DateTime now = DateTime.now().withTimeAtStartOfDay();
        String today = DATE_TIME_FORMAT.print(now);
        String yesterday = DATE_TIME_FORMAT.print(now.minusDays(1));
        ImmutableMap params = ImmutableMap.of(
                                "shopId", shopId, "today", today,
                                "yesterday", yesterday,"limit", size,
                                "offset", offset);

        Long count = getSqlSession().selectOne("OrderComment.countOfYesterdayOfShop", params);
        if (count==0) {
            return new Paging<OrderComment>(0L, Collections.<OrderComment>emptyList());
        }

        List<OrderComment> oc = getSqlSession().selectList("OrderComment.findYesterday", params);
        return new Paging<OrderComment>(count, oc);
    }

    public List<OrderComment> getYesterdayComment() {
        final DateTime now = DateTime.now().withTimeAtStartOfDay();
        String today = DATE_TIME_FORMAT.print(now);
        String yesterday = DATE_TIME_FORMAT.print(now.minusDays(1));
        ImmutableMap params = ImmutableMap.of(
                "today", today,
                "yesterday", yesterday);
        List<OrderComment> oc = getSqlSession().selectList("OrderComment.findYesterdayData", params);
        return oc;
    }

    public List<OrderComment> sumUpForYesterdayGroupByShop() {
        final DateTime now = DateTime.now().withTimeAtStartOfDay();
        String today = DATE_TIME_FORMAT.print(now);
        String yesterday = DATE_TIME_FORMAT.print(now.minusDays(1));
        ImmutableMap params = ImmutableMap.of(
                "today", today,
                "yesterday", yesterday);
        List<OrderComment> oc = getSqlSession().selectList("OrderComment.sumUpForYesterdayGroupByShop", params);
       // List<OrderComment> oc = getSqlSession().selectList("OrderComment.sumUpForYesterdayGroupByShop");
        return oc;
    }

    public Long maxId() {
        return getSqlSession().selectOne("OrderComment.maxId");
    }

    public Long maxId(Long shopId) {
        return getSqlSession().selectOne("OrderComment.maxId", ImmutableMap.of("shopId", shopId));
    }

    public List<OrderComment> forDump(Long lastId, Integer limit) {
        return getSqlSession().
                selectList("OrderComment.forDump", ImmutableMap.of("lastId", lastId, "limit", limit));
    }

    public List<OrderComment> forDump(Long shopId, Long lastId, Integer limit) {
        return getSqlSession().
                selectList("OrderComment.forDump", ImmutableMap.of("shopId", shopId, "lastId", lastId, "limit", limit));
    }

    public OrderComment sumUpShopScore(Long shopId) {
        return getSqlSession().selectOne("OrderComment.sumUpForShop", shopId);
    }

    public OrderComment sumUpShopScoreYesterday(Long shopId) {
        final DateTime now = DateTime.now().withTimeAtStartOfDay();
        String today = DATE_TIME_FORMAT.print(now);
        String yesterday = DATE_TIME_FORMAT.print(now.minusDays(1));
        ImmutableMap params = ImmutableMap.of(
                "shopId", shopId,
                "today", today,
                "yesterday", yesterday);
        OrderComment  oc = getSqlSession().selectOne("OrderComment.sumUpForShopYesterday", params);
        return oc;
    }

    public List<Long> forExpire(Long lastId, Integer limit) {
        return getSqlSession().
                selectList("OrderComment.forExpire", ImmutableMap.of("lastId", lastId, "limit", limit));
    }

    public boolean commentReply(Long id,String commentReply) {
        return getSqlSession().update("OrderComment.commentReply", ImmutableMap.of("id",id,"commentReply",commentReply)) == 1;
    }

    public boolean setIsBaskOrder(Long id) {
        return getSqlSession().update("OrderComment.setIsBaskOrder", id) == 1;
    }
}
