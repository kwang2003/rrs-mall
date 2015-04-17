/*
 * Copyright (c) 2012 杭州端点网络科技有限公司
 */

package com.aixforce.trade.dao;

import com.aixforce.trade.model.Comment;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.Assert.assertThat;

/**
 * Author:  <a href="mailto:jlchen.cn@gmail.com">jlchen</a>
 * Date: 2012-10-30
 */
public class CommentDaoTest extends BaseDaoTest {
    @Autowired
    private CommentDao commentDao;

    private Comment comment;

    @Before
    public void setUp() throws Exception {
        comment = newComment(11L, 22L, 1, 1, "very good");
        commentDao.create(comment);
    }

    @Test
    public void testCreate() throws Exception {
        assertThat(comment.getId(), notNullValue());
    }

    @Test
    public void testUpdate() throws Exception {
        comment.setContent("not so good");
        comment.setStatus(Comment.CommentStatus.DELETE.getValue());
        comment.setType(Comment.CommentType.BAD.getValue());
        commentDao.update(comment);

        final Comment actual = commentDao.findById(comment.getId());
        assertThat(actual.getContent(), is(comment.getContent()));
        assertThat(actual.getStatus(), is(comment.getStatus()));
        assertThat(actual.getType(), is(comment.getType()));
    }

    @Test
    public void testFindByTargetTypeAndTargetId() throws Exception {
        final Comment another = newComment(12L, 22L, 1, 1, "nice");
        commentDao.create(another);

        final List<Comment> actual = commentDao.findByTargetTypeAndTargetId(1, 22L, 0, 20);
        assertThat(actual, hasItems(comment, another));
    }

    @Test
    public void testCountOf() throws Exception {
        final Comment another = newComment(12L, 22L, 1, 1, "nice again");
        commentDao.create(another);

        final Integer count = commentDao.countOf(1, 22L);
        assertThat(count, is(2));
    }

    @Test
    public void testDelete() throws Exception {
        commentDao.delete(comment.getId());
        final Comment actual = commentDao.findById(comment.getId());
        assertThat(actual, nullValue());
    }

    private Comment newComment(Long userId, Long targetId, Integer targetType, Integer type, String content) {
        Comment comment = new Comment();
        comment.setUserId(userId);
        comment.setTargetId(targetId);
        comment.setTargetType(targetType);
        comment.setType(type);
        comment.setContent(content);
        comment.setStatus(Comment.CommentStatus.NORMAL.getValue());
        return comment;
    }
}
