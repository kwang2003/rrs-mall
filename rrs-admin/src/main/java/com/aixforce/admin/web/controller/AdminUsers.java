/*
 * Copyright (c) 2012 杭州端点网络科技有限公司
 */

package com.aixforce.admin.web.controller;

import com.aixforce.common.model.Paging;
import com.aixforce.common.model.Response;
import com.aixforce.exception.JsonResponseException;
import com.aixforce.user.base.BaseUser;
import com.aixforce.user.base.UserUtil;
import com.aixforce.user.model.LoginType;
import com.aixforce.user.model.User;
import com.aixforce.user.service.AccountService;
import com.aixforce.user.service.ImageService;
import com.aixforce.web.misc.MessageSources;
import com.google.common.base.Objects;
import com.google.common.base.Strings;
import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static com.aixforce.user.base.BaseUser.TYPE;

/**
 * Author:  <a href="mailto:jlchen.cn@gmail.com">jlchen</a>
 * Date: 2012-09-18
 */
@Controller
@RequestMapping("/api/admin/users")
public class AdminUsers {
    private final static List<Integer> changableTypes =
            Arrays.asList(
                    TYPE.ADMIN.toNumber(),TYPE.BUYER.toNumber(),
                    TYPE.SELLER.toNumber(),TYPE.SITE_OWNER.toNumber(),
                    TYPE.WHOLESALER.toNumber(), TYPE.FINANCE.toNumber());

    private final static Logger log = LoggerFactory.getLogger(AdminUsers.class);
    @Autowired
    private AccountService<User> accountService;

    @Autowired
    private ImageService imageService;
    @Autowired
    private MessageSources messageSources;

    @RequestMapping(method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Paging<User> index(@RequestParam(value = "q", defaultValue = "") String keywords,
                              @RequestParam(value = "queryType", defaultValue = "byName") String queryType,
                              @RequestParam(value = "p", defaultValue = "1") Integer pageNo,
                              @RequestParam(value = "status", defaultValue = "1") Integer status
    ) {
        try {
            if (!Strings.isNullOrEmpty(keywords)) {
                User user;
                if (Objects.equal(queryType, "byName")) {
                    user = accountService.findUserBy(keywords, LoginType.NAME).getResult();
                } else {
                    user = accountService.findUserById(Long.parseLong(keywords)).getResult();
                }
                if (user == null) {
                    return new Paging<User>(0l, Collections.<User>emptyList());
                } else {
                    return new Paging<User>(1l, ImmutableList.of(user));
                }
            } else {
                return accountService.list(status, pageNo, 20).getResult();
            }
        } catch (RuntimeException ex) {
            log.error("failed to find user for keywords {},cause:{}", keywords, Throwables.getStackTraceAsString(ex));
            throw new JsonResponseException(500, messageSources.get("users.query.fail"));
        }
    }

    @RequestMapping(method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public String create(User user) {
        Response<Long> result = accountService.createUser(user);
        if (result.isSuccess()) {
            return "ok";
        } else {
            log.error("failed to create user for userEmail {},cause:{}", user.getEmail(), result.getError());
            throw new JsonResponseException(500, messageSources.get("users.create.fail"));
        }
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.PUT)
    public String update(@PathVariable("id") Long id, User user) {
        user.setId(id);
        Response<Boolean> result = accountService.updateUser(user);
        if (result.isSuccess()) {
            return "ok";
        } else {
            log.error("failed to create user for userId {},cause:{}", id, result.getError());
            throw new JsonResponseException(500, messageSources.get("users.update.fail"));
        }
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public User view(@PathVariable("id") Long id) {
        Response<User> result = accountService.findUserById(id);
        if (result.isSuccess()) {
            User user = result.getResult();
            if (user == null) {
                throw new IllegalStateException("can not found user with id=" + id);
            }
            return user;
        } else {
            log.error("failed to find user with userId {},cause:{}", id, result.getError());
            throw new JsonResponseException(500, messageSources.get("users.not.found"));
        }
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
    public String delete(@PathVariable("id") Long id) {
        try {
            Response<User> result = accountService.findUserById(id);
            if (result.isSuccess()) {
                accountService.deleteUser(id);
                imageService.deleteByUserId(id);
                return "ok";
            } else {
                throw new JsonResponseException(500, messageSources.get("user.not.found"));
            }
        } catch (RuntimeException ex) {
            log.error(ex.getMessage() + "userId: {},cause:{}", id, Throwables.getStackTraceAsString(ex));
            throw new JsonResponseException(500, messageSources.get("users.delete.fail"));
        }
    }

    @RequestMapping(value = "/{id}/edit", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public User edit(@PathVariable("id") Long id) {
        Response<User> result = accountService.findUserById(id);
        if (!result.isSuccess()) {
            throw new JsonResponseException(500, messageSources.get(result.getError()));
        } else {
            return result.getResult();
        }
    }

    @RequestMapping(value = "/bulk-update-type", method = RequestMethod.PUT, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
//    @Authentication(access_group = "site-owner")
    public String bulkUpdateType(@RequestParam("ids") Long[] ids,
                                 @RequestParam("type") Integer type,
                                 @RequestParam(value = "businessId", required = false) Integer businessId) {
        // use @Authentication instead later
        if (!changableTypes.contains(type)) {
            throw new JsonResponseException(messageSources.get("user.type.update.invalid.type"));
        }
        BaseUser currentUser = UserUtil.getCurrentUser();
        if (currentUser==null || (!Objects.equal(currentUser.getTypeEnum(), TYPE.ADMIN)
                && !Objects.equal(currentUser.getTypeEnum(), TYPE.SITE_OWNER))) {
            throw new JsonResponseException("user not authorized access resource.");
        }

        Response<Boolean> updateResult = accountService.bulkUpdateUserType(Arrays.asList(ids), type, businessId);
        if (updateResult.isSuccess()) {
            return "OK";
        }

        throw new JsonResponseException(messageSources.get(updateResult.getError()));
    }
}
