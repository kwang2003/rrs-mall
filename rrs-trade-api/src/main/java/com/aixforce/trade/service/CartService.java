package com.aixforce.trade.service;

import com.aixforce.annotations.ParamInfo;
import com.aixforce.common.model.Response;
import com.aixforce.trade.dto.PreOrder;
import com.aixforce.trade.model.UserCart;
import com.aixforce.user.base.BaseUser;

import java.util.List;

/**
 * Author:  <a href="mailto:jlchen.cn@gmail.com">jlchen</a>
 * Date: 2013-03-08
 */
public interface CartService {

    /**
     * 获取永久购物车中的物品
     *
     * @param baseUser 系统自动注入的用户
     * @return 永久购物车中的物品
     */
    Response<List<UserCart>> getPermanent(@ParamInfo("baseUser") BaseUser baseUser);

    /**
     * 获取永久购物车中的sku的种类个数
     *
     * @param baseUser 系统自动注入的用户
     * @return sku的种类个数
     */
    Response<Integer> getPermanentCount(@ParamInfo("baseUser") BaseUser baseUser);

    /**
     * 获取临时购物车中的sku的种类个数
     *
     * @param cookie cartCookie
     * @return sku的种类个数
     */
    Response<Integer> getTemporaryCount(String cookie);

    /**
     * 增减临时购物车中的物品
     *
     * @param key      cart cookie key
     * @param skuId    sku id
     * @param quantity 变化数量
     */
    Response<Integer> changeTemporaryCart(String key, Long skuId, Integer quantity);

    /**
     * 增减永久购物车中的物品
     *
     * @param userId   userId
     * @param skuId    sku id
     * @param quantity 变化数量
     */
    Response<Integer> changePermanentCart(Long userId, Long skuId, Integer quantity);

    /**
     * 将临时购物车的物品合并到永久购物车中,并删除临时购物车
     *
     * @param key    cookie中带过来了的key
     * @param userId 用户id
     */
    Response<Boolean> merge(String key, Long userId);

    /**
     * 用户提交sku id及数量,显示将要生成的订单的详情,这一步不会真的写入数据库
     *
     * @param skus skuId and quantity
     * @return preOrder
     */
    Response<List<PreOrder>> preOrder(@ParamInfo("baseUser") BaseUser baseUser,@ParamInfo("skus") String skus);


    /**
     * 批量删除用户购物车中的skuIds
     *
     * @param userId 用户id
     * @param skuIds 待删除skuId列表,删空
     */
    Response<Boolean> batchDeletePermanent(Long userId, Iterable<Long> skuIds);

    /**
     * 清空用户的购物车
     *
     * @param key   cookie中的key，或者用户id
     */
    Response<Boolean> empty(String key);


//    /**
//     * 用户提交sku id及数量,显示将要生成的订单的详情,这一步不会真的写入数据库
//     *
//     * @param skus skuId and quantity
//     * @return preOrder
//     */
//    Response<List<PreOrder>> preCouponsOrder(@ParamInfo("baseUser") BaseUser baseUser,@ParamInfo("skus") String skus);
}
