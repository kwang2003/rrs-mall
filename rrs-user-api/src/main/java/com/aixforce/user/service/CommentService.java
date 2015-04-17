package com.aixforce.user.service;

import com.aixforce.common.model.Paging;
import com.aixforce.user.model.Comment;

import java.util.Map;

/*
 * Author: jlchen
 * Date: 2012-12-27
 */
public interface CommentService {

    /**
     * 评论列表
     *
     * @param p      页码,从1开始
     * @param size   每页数目
     * @param params 查询参数
     * @return 评论列表
     */
    Paging<Comment> commentsOf(Integer p, Integer size, Map<String, String> params);

    /**
     * 添加评论,包括回复
     *
     * @param comment 评论
     */
    void create(Comment comment);

    /**
     * 根据id查找评论
     *
     * @param id 评论id
     * @return 评论
     */
    Comment findById(Long id);
}
