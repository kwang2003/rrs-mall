package com.aixforce.shop.service;

import com.aixforce.common.model.Response;

import java.util.List;

/**
 * Created by jack.yang on 14-8-11.
 */
public interface ChannelShopsService {

    /**
     * 获取渠道对应的店铺ID一览
     *
     * @param channel 渠道ID
     * @return 店铺ID一览
     */
    Response<List<Long>> findShops(String channel);

    /**
     * 获取渠道对应的密钥
     * @param channel 渠道ID
     * @return 密钥
     */
    Response<String> findKey(String channel);

    /**
     * 获取渠道对应的卖家ID一览
     * @param channel 渠道ID
     * @return 卖家ID一览
     */
    Response<List<Long>> findUserIds(String channel);

    /**
     *  检查该角色是否需要授权认证
     * @return true：需要验证，false：不需要
     */
    Response<Boolean> isAuthRole(String method, String channel);

    /**
     * 检查是否可以发送手机短信
     * @param mobile 手机号码
     * @return 可否的真假值
     */
    Response<Boolean> checkMobileSendable(final String mobile);

    /**
     * 验证验证码
     * @param mobile 手机号码
     * @param code 验证码
     * @return 可否的真假值
     */
    Response<Boolean> validateMobileCode(final String mobile, final String code);

    /**
     * 将手机信息存入redis
     * @param mobile 手机号
     * @param code 短信内容
     * @return 操作是否执行成功
     */
    Response<Boolean> setMobileSent(final String mobile, final String code, final Long id);

    /**
     * 根据消息ID获得消息内容
     * @param id 消息ID
     * @return 消息内容
     */
    Response<String> findSmsMessage(Long id);

    /**
     *获取频道信息
     * @param channel 渠道ID
     * @return 频道信息
     */
    Response<List<Long>> findBusinessIds(String channel);

    /**
     * 获得渠道的角色
     * @param channel 渠道ID
     * @return 渠道的角色
     */
    Response<Long> findRole1(String channel);
}
