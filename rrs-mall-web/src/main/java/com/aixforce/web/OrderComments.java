/*
 * Copyright (c) 2012 杭州端点网络科技有限公司¸
 */

package com.aixforce.web;

import com.aixforce.common.model.Paging;
import com.aixforce.common.model.Response;
import com.aixforce.common.utils.JsonMapper;
import com.aixforce.exception.JsonResponseException;
import com.aixforce.trade.model.OrderComment;
import com.aixforce.trade.service.OrderCommentService;
import com.aixforce.user.base.UserUtil;
import com.aixforce.web.misc.MessageSources;
import com.fasterxml.jackson.databind.JavaType;
import com.google.common.base.Charsets;
import com.google.common.base.Strings;
import com.google.common.base.Throwables;
import com.google.common.collect.Sets;
import com.google.common.io.Files;
import com.google.common.io.LineProcessor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.annotation.PostConstruct;
import javax.servlet.ServletContext;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Date: 14-2-12
 * Time: PM4:20
 * Author: 2014年 <a href="mailto:dong.worker@gmail.com">张成栋</a>
 */

@Slf4j
@Controller
@RequestMapping("api/comment")
public class OrderComments {
    private final static JsonMapper jsonMapper = JsonMapper.nonEmptyMapper();

    private final static JavaType javaType= jsonMapper.createCollectionType(
                                                ArrayList.class, OrderComment.class);

    @Autowired
    private OrderCommentService orderCommentService;
    @Autowired
    private ServletContext servletContext;

    private AtomicReference<Set<String>> sensitiveWords;

    private  File sensitiveWordsDictionary;

    private volatile long lastModified;
    @Autowired
    private MessageSources messageSources;

    private static final ExecutorService executor = Executors.newFixedThreadPool(5);

    @PostConstruct
    public void init() {
        this.sensitiveWords = new AtomicReference<Set<String>>();
        sensitiveWordsDictionary = new File(servletContext.getRealPath("/")+
                File.separator + "WEB-INF" + File.separator + "sensor_words.txt");
        reload();
        lastModified = sensitiveWordsDictionary.lastModified();
    }

    private void checkIfNeedReload() {
        if (sensitiveWordsDictionary.lastModified() > lastModified) {
            lastModified = sensitiveWordsDictionary.lastModified();
            reload();
        }

    }

    private void reload() {
        try {
            final Set<String> result = Files.readLines(sensitiveWordsDictionary, Charsets.UTF_8, new LineProcessor<Set<String>>() {
                private Set<String> sensitveWords = Sets.newHashSet();

                @Override
                public boolean processLine(String line) throws IOException {
                    sensitveWords.add(line.trim());
                    return true;
                }

                @Override
                public Set<String> getResult() {
                    return sensitveWords;
                }
            });
            this.sensitiveWords.set(result);
        } catch (IOException e) {
            log.error("failed to load sensitive words file ({}), cause:{}", sensitiveWordsDictionary.getAbsolutePath(),
                    Throwables.getStackTraceAsString(e));
        }
    }


    /**
     * 检查敏感字内容 , 并做替换
     *
     * @param content 输入的内容
     */
    public String check(String content) {
        if (!sensitiveWordsDictionary.exists()) {
            return content;
        }
        checkIfNeedReload();
        for (String word : this.sensitiveWords.get()) {
            String target = Strings.repeat("*", word.length());
            content = content.replaceAll(word, target);
        }
        return content;
    }

    /**
     *
     * @param orderId   主订单ID
     * @param json      订单评价 json，多个评价以数组方式提交
     * @return          评价的 ID 列表
     */
    @RequestMapping(value = "/{orderId}", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public List<Long> create(@PathVariable("orderId") Long orderId, @RequestParam("detail") String json) {

        List<OrderComment> comments = jsonMapper.fromJson(json, javaType);

        if (comments.isEmpty()) {
            throw new JsonResponseException(500, messageSources.get("order.comment.invalid.argument"));
        }

        for (OrderComment comment : comments) {
            comment.setComment(check(comment.getComment()));
        }

        Response<List<Long>> result = orderCommentService.create(orderId, comments, UserUtil.getUserId());
        if (result.isSuccess()) {
            return result.getResult();
        }

        throw new JsonResponseException(500, messageSources.get(result.getError()));
    }

    /**
     *
     * @param   id  订单 ID
     * @return  根据 ID 返回评价
     */
    @RequestMapping(value = "/{id}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public OrderComment view(@PathVariable("id") Long id) {

        Response<OrderComment> result =  orderCommentService.findById(id);
        if (result.isSuccess()) {
            return result.getResult();
        }

        throw new JsonResponseException(500, result.getError());
    }

    @RequestMapping(value = "/item", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Paging<OrderComment> viewItemComments(@RequestParam("itemId") Long itemId,
                                                 @RequestParam(value = "pageNo", defaultValue = "1") Integer pageNo,
                                                 @RequestParam(value = "size", defaultValue = "20") Integer size) {
        Response<Paging<OrderComment>> result = orderCommentService.viewItemComments(itemId, pageNo, size);

        if (result.isSuccess()) {
            return result.getResult();
        }

        throw new JsonResponseException(500, messageSources.get(result.getError()));
    }

    @RequestMapping(value = "/", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Paging<OrderComment> index(@RequestParam(value = "pageNo", defaultValue = "1") Integer pageNo,
                                      @RequestParam(value = "size", defaultValue = "20") Integer size) {


        return null;
    }


    @RequestMapping(value = "/{id}/reply", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Boolean updateCommentReply(@PathVariable("id") Long id, @RequestParam("content") String content) {
       content=check(content);
       Response<Boolean> result  = orderCommentService.updateCommentReply(id,content);
        if (!result.isSuccess()) {
            throw new JsonResponseException(500, messageSources.get(result.getError()));
        }
        return result.getResult();
    }


}
