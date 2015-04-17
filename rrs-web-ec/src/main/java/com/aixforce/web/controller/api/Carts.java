/*
 * Copyright (c) 2013 杭州端点网络科技有限公司
 */

package com.aixforce.web.controller.api;

import com.aixforce.common.model.Response;
import com.aixforce.common.utils.CommonConstants;
import com.aixforce.common.utils.CookieBuilder;
import com.aixforce.exception.JsonResponseException;
import com.aixforce.trade.model.UserCart;
import com.aixforce.trade.service.CartService;
import com.aixforce.user.base.BaseUser;
import com.aixforce.user.base.UserUtil;
import com.aixforce.web.misc.MessageSources;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.util.JSONPObject;
import com.google.common.base.Charsets;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * Author:  <a href="mailto:jlchen.cn@gmail.com">jlchen</a>
 * Date: 2013-03-07
 */
@Controller
@RequestMapping("/api/cart")
public class Carts {

    private final static Logger log = LoggerFactory.getLogger(Carts.class);

    private final static HashFunction md5 = Hashing.md5();

    private final static int ONE_YEAR = (int) TimeUnit.DAYS.toSeconds(365);

    private final CartService cartService;

    private final CommonConstants commonConstants;

    private final MessageSources messageSources;

    @Autowired
    public Carts(CartService cartService, CommonConstants commonConstants, MessageSources messageSources) {
        this.cartService = cartService;
        this.commonConstants = commonConstants;
        this.messageSources = messageSources;
    }

    @RequestMapping(value = "/batchDelete", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public String batchDelete(@RequestParam("skuIds") String skuIds) {
        BaseUser user = UserUtil.getCurrentUser();

        List<String> parts = Splitter.on(',').omitEmptyStrings().trimResults().splitToList(skuIds);
        List<Long> ids = Lists.newArrayListWithCapacity(parts.size());
        for(String id : parts) {
            ids.add(Long.parseLong(id));
        }
        if (ids.isEmpty()) {
            return "ok";
        }
        Response<Boolean> result = cartService.batchDeletePermanent(user.getId(), ids);
        if (!result.isSuccess()) {
            log.error("failed to batch delete cart for user {},skuIds:{},error code :{}",
                    user, skuIds, result.getError());
            throw new JsonResponseException(500, messageSources.get(result.getError()));
        }
        return "ok";
    }

    @RequestMapping(method = RequestMethod.POST, produces = "application/javascript")
    @ResponseBody
    public Integer changeCart(@RequestParam("skuId") Long skuId, @RequestParam(value = "quantity", defaultValue = "1") Integer quantity,
                              @CookieValue(value = "cart", required = false) String cartCookie, HttpServletResponse response) {
        BaseUser user = UserUtil.getCurrentUser();
        if (user == null) { //用户没有登录,通过cookie操作购物车
            if (Strings.isNullOrEmpty(cartCookie)) {
                //new cart cookie
                cartCookie = md5.hashString(UUID.randomUUID().toString(), Charsets.UTF_8).toString();
                Cookie cookie = CookieBuilder.from(CommonConstants.COOKIE_SHOP_CART, cartCookie, commonConstants.getDomain())
                        .httpOnly().maxAge(ONE_YEAR).build();
                response.addCookie(cookie);

            }
            Response<Integer> result = cartService.changeTemporaryCart(cartCookie, skuId, quantity);
            if (!result.isSuccess()) {
                log.error("change temporary cart failed, skuId={},cause:{}", skuId, result.getError());
                throw new JsonResponseException(500, messageSources.get(result.getError()));
            }
            return result.getResult();
        } else { //用户已登录
            Response<Integer> result = cartService.changePermanentCart(user.getId(), skuId, quantity);
            if (!result.isSuccess()) {
                log.error("change permanent cart failed, skuId={},cause:{}", skuId, result.getError());
                throw new JsonResponseException(500, messageSources.get(result.getError()));
            }
            return result.getResult();
        }
    }

    @RequestMapping(value = "/count", method = RequestMethod.GET)
    @ResponseBody
    public JSONPObject count(@RequestParam("callback") String callback, @CookieValue(value = "cart", required = false) String cartCookie) throws JsonProcessingException {
        BaseUser user = UserUtil.getCurrentUser();
        Integer cartCount = 0;
        if (user == null) {
            if (!Strings.isNullOrEmpty(cartCookie)) {
                Response<Integer> result = cartService.getTemporaryCount(cartCookie);
                if (!result.isSuccess()) {
                    log.error("get temporary count failed, cause:{}", result.getError());
                    throw new JsonResponseException(500, messageSources.get(result.getError()));
                }
                cartCount = result.getResult();
            }
        } else {
            Response<Integer> result = cartService.getPermanentCount(user);
            if (!result.isSuccess()) {
                log.error("get permanent count failed, cause:{}", result.getError());
                throw new JsonResponseException(500, messageSources.get(result.getError()));
            }
            cartCount = result.getResult();
        }
        return new JSONPObject(callback, cartCount);
    }

    /**
     * 获取用户当前购物车中的信息
     * @return
     */
    @RequestMapping(value = "/queryCarts", method = RequestMethod.GET)
    @ResponseBody
    public List<UserCart> queryCarts() {
        BaseUser user = UserUtil.getCurrentUser();

        if(user == null){
            log.warn("query cart need user login!");
            throw new JsonResponseException(401 , "user.not.login");
        }

        Response<List<UserCart>> response  = cartService.getPermanent(user);

        if (!response.isSuccess()) {
            log.error("failed to query cart for user {},error code :{}", user, response.getError());
            throw new JsonResponseException(500, messageSources.get(response.getError()));
        }

        return response.getResult();
    }
}
