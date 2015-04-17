/*
 * Copyright (c) 2012 杭州端点网络科技有限公司
 */

package com.aixforce.trade.dao;

import com.aixforce.trade.model.Comment;
import com.google.common.base.Objects;
import com.google.common.collect.ImmutableMap;
import org.mybatis.spring.support.SqlSessionDaoSupport;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Author:  <a href="mailto:jlchen.cn@gmail.com">jlchen</a>
 * Date: 2012-10-30
 */
@Repository
public class CommentDao extends SqlSessionDaoSupport {
    public Comment findById(Long id) {
        return getSqlSession().selectOne("Comment.findById", id);
    }

    public List<Comment> findByTargetTypeAndTargetId(Integer targetType, Long targetId, Integer offset, Integer limit) {
        return getSqlSession().selectList("Comment.findByTargetTypeAndTargetId", ImmutableMap.of("targetType", targetType,
                "targetId", targetId, "offset", offset, "limit", limit));
    }

    public Integer countOf(Integer targetType, Long targetId) {
        Integer count = getSqlSession().selectOne("Comment.countOf", ImmutableMap.of("targetType", targetType,
                "targetId", targetId));
        return Objects.firstNonNull(count, 0);
    }

    public void create(Comment comment) {
        getSqlSession().insert("Comment.create", comment);
    }

    public void delete(Long id) {
        getSqlSession().delete("Comment.delete", id);
    }

    public void update(Comment comment) {
        getSqlSession().update("Comment.update", comment);
    }
}
