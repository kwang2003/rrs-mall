/*
 * Copyright (c) 2012 杭州端点网络科技有限公司
 */

package com.aixforce.user.service;

import com.aixforce.annotations.ParamInfo;
import com.aixforce.common.model.Paging;
import com.aixforce.common.model.Response;
import com.aixforce.user.base.BaseUser;
import com.aixforce.user.dto.BuyerDto;
import com.aixforce.user.model.LoginType;
import com.aixforce.user.model.User;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;

/**
 * Author:  <a href="mailto:jlchen.cn@gmail.com">jlchen</a>
 * Date: 2012-08-14
 */
public interface AccountService<T extends BaseUser> {

    /**
     * 从数据库中load用户,按照id逆序排列
     *
     * @param status 用户状态
     * @param pageNo 页码
     * @param count  每页返回条数
     * @return 分页结果
     */
    Response<Paging<T>> list(@ParamInfo("status") Integer status, @ParamInfo("pageNo") Integer pageNo, @ParamInfo("count") Integer count);

    /**
     * 根据id寻找user
     *
     * @param id 用户id
     * @return 用户对象
     */
    Response<T> findUserById(@ParamInfo("id") Long id);

    /**
     * 创建User对象
     *
     * @param user 用户
     * @return 新创建的id
     */
    Response<Long> createUser(T user);

    /**
     * 更新用户对象
     *
     * @param user 用户
     */
    Response<Boolean> updateUser(T user);

    /**
     * 更改密码
     *
     * @param userId      user id
     * @param oldPassword 老密码
     * @param newPassword 新密码
     * @return 是否更新成功
     */
    Response<Boolean> changePassword(Long userId, String oldPassword, String newPassword);

    /**
     * 重置密码
     *
     * @param userId   用户id
     * @param password 加密前的密码
     * @return 是否重置成功
     */
    Response<Boolean> resetPassword(Long userId, String password);

    /**
     * 删除用户,如果尝试删除超级管理员将抛出异常.
     *
     * @param userId 用户id
     * @return 是否删除成功
     */
    Response<Boolean> deleteUser(Long userId);

    /**
     * 根据loginId查找用户,如果用户不存在也会报错
     *
     * @param loginId 登陆id
     * @param type    id类型,如电子邮件,手机号码,昵称等
     * @return 用户
     */
    Response<T> findUserBy(@ParamInfo("loginId") String loginId, @ParamInfo("loginType") LoginType type);

    /**
     * 检查用户是否存在
     * @param loginId  登陆id
     * @param loginType id类型,如电子邮件,手机号码,昵称等
     * @return  是否存在
     */
    Response<Boolean> userExists(String loginId, LoginType loginType);

    /**
     * 用户登陆
     *
     * @param id       id
     * @param type     登陆类型
     * @param password 密码
     * @return 用户
     */
    Response<T> userLogin(String id, LoginType type, String password);


    /**
     * 用户登陆
     *
     * @param id       id
     * @param type     登陆类型
     * @param password 密码
     * @return 用户
     */
    Response<T> login(String id, LoginType type, String password);


    /**
     * 修改手机号码
     *
     * @param userId   用户Id
     * @param mobile   新的手机号码
     * @param password 用户密码
     */
    Response<Boolean> changeMobile(Long userId, String mobile, String password);

    /**
     * 分页返回所有的buyer, params为null时，默认查询所有的买家和卖家
     *
     * @param pageNo 页数
     * @return 买家dto列表
     */
    Response<Paging<BuyerDto>> listMembers(@ParamInfo("params") Map<String, String> params,
                                           @ParamInfo("pageNo") Integer pageNo,
                                           @ParamInfo("size") Integer size);

    /**
     * 分页返回所有的buyer, params为null时，默认查询所有的买家和卖家
     *
     * @param pageNo 页数
     * @return 买家dto列表
     */
    Response<Paging<BuyerDto>> listAllMembers(@ParamInfo("params") Map<String, String> params,
                                              @ParamInfo("pageNo") Integer pageNo,
                                              @ParamInfo("size") Integer size);

    /**
     * 分页查询user
     * @param params 查询参数
     * @param pageNo 页号
     * @param size   大小
     * @return 分页后买家列表
     */
    Response<Paging<User>> findUser(Map<String, String> params,
                                    Integer pageNo, Integer size);

    /**
     * 分页获取所有商户
     *
     * @param pageNo 页数
     * @param size   每页数据数
     * @return 分页数据
     */
    Response<Paging<T>> listSellers(Integer pageNo, Integer size);

    /**
     * 分页获取所有代理商
     *
     * @param name      账户名称，选填
     * @param pageNo    页数
     * @param size      每页大小
     * @return  满足条件的分页数据
     */
    Response<Paging<T>> listAgents(@ParamInfo("name") @Nullable String name,
                                   @ParamInfo("pageNo")Integer pageNo,
                                   @ParamInfo("size")Integer size);

    /**
     * 批量冻结用户
     *
     * @param ids    用户ids
     * @param status 用户状态，frozen或者normal
     * @return 操作是否成功
     */
    Response<Boolean> updateStatusByIds(List<Long> ids, Integer status);


    /**
     * 根据手机号获取用户信息
     *
     * @param mobile  手机号
     * @return  用户信息
     */
    Response<T> findUserByMobile(String mobile);

    /**
     * 根据用户id列表查找用户
     * @param ids 用户id列表
     * @return 用户列表
     */
    Response<List<User>> findByIds(List<Long> ids);


    /**
     * 更新用户的状态
     *
     * @param id        用户id
     * @param status    更新后的状态
     */
    Response<Boolean> updateStatus(Long id, Integer status);

    /**
     * 更改用户类型
     * @param ids        用户 ID
     * @param type      用户类型
     * @param businessId
     */
    Response<Boolean> bulkUpdateUserType(List<Long> ids, Integer type, Integer businessId);

    /**
   	 * 联合登录修改用户信息
   	 *
   	 * @param userId
   	 * @param mobile
   	 * @param newPassword
   	 * @return
   	 */
   	Response<Boolean> changeUserInfo(Long userId, String mobile, String newPassword);

   	/**
   	 * 查询用户信息
   	 *
   	 * @param userId
   	 * @param returnUrl
   	 * @return
   	 */
   	Response<Map> searchUserInfo(@ParamInfo("userId") String userId_md5str, @ParamInfo("targetUrl") String targetUrl);
}
