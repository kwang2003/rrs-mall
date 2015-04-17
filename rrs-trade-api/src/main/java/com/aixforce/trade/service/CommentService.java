/*
 * Copyright (c) 2012 杭州端点网络科技有限公司
 */

package com.aixforce.trade.service;

import com.aixforce.common.model.Paging;
import com.aixforce.trade.model.Comment;

/**
 * Author:  <a href="mailto:jlchen.cn@gmail.com">jlchen</a>
 * Date: 2012-10-30
 */
public interface CommentService {

    void create(Comment comment);

    void update(Comment comment);

    void delete(Comment comment);

    Comment findById(Long id);

    Paging<Comment> findByTargetTypeAndTargetId(Integer offset, Integer limitN, Integer targetType, Long targetId);
}
