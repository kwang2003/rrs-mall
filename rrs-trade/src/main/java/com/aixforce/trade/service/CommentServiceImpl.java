/*
 * Copyright (c) 2012 杭州端点网络科技有限公司
 */

package com.aixforce.trade.service;

import com.aixforce.common.model.Paging;
import com.aixforce.exception.ServiceException;
import com.aixforce.trade.dao.CommentDao;
import com.aixforce.trade.model.Comment;
import com.google.common.base.Strings;
import com.google.common.base.Throwables;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.google.common.base.Objects.firstNonNull;
import static com.google.common.base.Preconditions.checkArgument;

/**
 * Author:  <a href="mailto:jlchen.cn@gmail.com">jlchen</a>
 * Date: 2012-10-30
 */
@Service
public class CommentServiceImpl implements CommentService {

    private final static Logger log = LoggerFactory.getLogger(CommentServiceImpl.class);

    @Autowired
    private CommentDao commentDao;

    @Override
    public void create(Comment comment) {
        checkArgument(comment != null, "comment can not be null");
        checkArgument(comment.getTargetType() != null, "comment target type can not be null");
        checkArgument(comment.getTargetId() != null, "comment target id can not be null");
        checkArgument(comment.getType() != null, "comment type can not be null");
        checkArgument(!Strings.isNullOrEmpty(comment.getContent()), "comment content can not be null");
        comment.setStatus(Comment.CommentStatus.NORMAL.getValue());
        try {
            commentDao.create(comment);
            log.debug("create {} successfully", comment);
        } catch (Exception e) {
            log.error("failed to create {},cause:{}", comment, e);
            throw new ServiceException("failed to create comment", e);
        }
    }

    @Override
    public void update(Comment comment) {
        checkArgument(comment != null, "comment can not be null");
        checkArgument(comment.getId() != null, "comment id can not be null");
        try {
            commentDao.update(comment);
            log.debug("update {} successfully", comment);
        } catch (Exception e) {
            log.error("failed to update {},cause:{}", comment, e);
            throw new ServiceException("failed to update comment", e);
        }
    }

    @Override
    public void delete(Comment comment) {
        checkArgument(comment != null, "comment can not be null");
        checkArgument(comment.getId() != null, "comment id can not be null");
        checkArgument(comment.getStatus() != null, "comment status can not be null");
        try {
            commentDao.update(comment);
            log.debug("delete {} successfully", comment);
        } catch (Exception e) {
            log.error("failed to delete {},cause:{}", comment, e);
            throw new ServiceException("failed to delete comment ", e);
        }
    }

    @Override
    public Comment findById(Long id) {
        checkArgument(id != null, "comment id can not be null");
        try {
            return commentDao.findById(id);
        } catch (Exception e) {
            log.error("failed to find comment whose id is {},cause");
            throw new ServiceException("failed to find comment by id", e);
        }
    }

    @Override
    public Paging<Comment> findByTargetTypeAndTargetId(Integer offset, Integer limit, Integer targetType, Long targetId) {
        checkArgument(targetType != null, "comment targetType can not be null");
        checkArgument(targetId != null, "comment targetId can not be null");
        try {
            List<Comment> comments = commentDao.findByTargetTypeAndTargetId(targetType, targetId, firstNonNull(offset, 0), firstNonNull(limit, 20));
            int count = commentDao.countOf(targetType, targetId);
            return new Paging<Comment>((long) count, comments);
        } catch (Exception e) {
            log.error("failed to find comments for (targetType={},targetId={}),cause:{}", targetType, targetId,
                    Throwables.getStackTraceAsString(Throwables.getRootCause(e)));
            throw new ServiceException("failed to find comments by targetType and targetId", e);
        }
    }
}
