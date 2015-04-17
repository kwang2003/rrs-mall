package com.aixforce.trade.service;

import com.aixforce.common.model.PageInfo;
import com.aixforce.common.model.Paging;
import com.aixforce.common.model.Response;
import com.aixforce.exception.ServiceException;
import com.aixforce.trade.dao.ExpressInfoDao;
import com.aixforce.trade.dao.ExpressInfoRedisDao;
import com.aixforce.trade.model.ExpressInfo;
import com.aixforce.user.base.BaseUser;
import com.google.common.base.Objects;
import com.google.common.base.Predicate;
import com.google.common.base.Throwables;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.Collections2;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 快递信息服务实现
 * Author: haolin
 * On: 9/22/14
 */
@Service @Slf4j
public class ExpressInfoServiceImpl implements ExpressInfoService {

    @Autowired
    private ExpressInfoDao expressInfoDao;

    @Autowired
    private ExpressInfoRedisDao expressInfoRedisDao;

    private static final Integer ENABLED_EXPRESS_INFO_KEY = 1;

    // cache
    private LoadingCache<Integer, List<ExpressInfo>> enableExpressInfoCache;

    private final Map<Long, ExpressInfo> enableExpressInfoMapCache = Maps.newHashMapWithExpectedSize(200);

    public ExpressInfoServiceImpl(){
        enableExpressInfoCache = CacheBuilder.newBuilder().expireAfterWrite(5, TimeUnit.MINUTES).build(new CacheLoader<Integer, List<ExpressInfo>>(){
            @Override
            public List<ExpressInfo> load(Integer unused) throws Exception {
                Map<String, Object> criteria = Maps.newHashMapWithExpectedSize(1);
                criteria.put("status", ExpressInfo.Status.ENABLED.value());
                List<ExpressInfo> expressInfos = expressInfoDao.list(criteria);
                enableExpressInfoMapCache.clear();
                for (ExpressInfo expressInfo : expressInfos){
                    enableExpressInfoMapCache.put(expressInfo.getId(), expressInfo);
                }
                return expressInfos;
            }
        });
    }

    /**
     * 创建快递信息
     *
     * @param expressInfo 快递信息
     * @return 快递信息id
     */
    @Override
    public Response<Long> create(ExpressInfo expressInfo) {
        Response<Long> resp = new Response<Long>();
        try {
            checkNameExist(expressInfo);
            checkCodeAndInterfaceExist(expressInfo);
            expressInfo.setStatus(ExpressInfo.Status.DISABLED.value());
            expressInfoDao.create(expressInfo);
            resp.setResult(expressInfo.getId());
        } catch (ServiceException e){
          resp.setError(e.getMessage());
        } catch (Exception e){
            log.error("failed to create express info({}), cause: {}", expressInfo, Throwables.getStackTraceAsString(e));
            resp.setError("express.info.create.fail");
        }
        return resp;
    }

    /**
     * 检查名称是否已存在
     */
    private void checkNameExist(ExpressInfo expressInfo) {
        ExpressInfo existed = expressInfoDao.findByName(expressInfo.getName());
        if (existed != null){
            log.error("ExpressInfo name({}) has existed.", expressInfo.getName());
            throw new ServiceException("express.info.existed");
        }
    }

    /**
     * 检查【代码+接口】是否已经存在
     */
    private void checkCodeAndInterfaceExist(ExpressInfo expressInfo) {
        ExpressInfo existed = expressInfoDao.findByCodeAndInterface(expressInfo);
        if (existed != null ) {
            log.error("ExpressInfo (code={}, interfaceName={}) has existed.", expressInfo.getCode(), expressInfo.getInterfaceName());
            throw new ServiceException("express.info.existed");
        }
    }

    /**
     * 更新快递信息
     *
     * @param expressInfo 快递信息
     * @return 更新成功返回true, 反之false
     */
    @Override
    public Response<Boolean> update(ExpressInfo expressInfo) {
        Response<Boolean> resp = new Response<Boolean>();
        try {
            ExpressInfo existed = expressInfoDao.findByName(expressInfo.getName());
            if (existed != null && !Objects.equal(existed.getId(), expressInfo.getId())){
                throw new ServiceException("express.info.existed");
            }
            existed = expressInfoDao.findByCodeAndInterface(expressInfo);
            if (existed != null && !Objects.equal(existed.getId(), expressInfo.getId())){
                throw new ServiceException("express.info.existed");
            }
            expressInfoDao.update(expressInfo);
            resp.setResult(Boolean.TRUE);
        } catch (ServiceException e) {
            resp.setError(e.getMessage());
        } catch (Exception e){
            log.error("failed to update express info({}), cause: {}", expressInfo, Throwables.getStackTraceAsString(e));
            resp.setError("express.info.update.fail");
        }
        return resp;
    }

    /**
     * 逻辑删除快递信息
     *
     * @param id 快递信息id
     * @return 删除成功返回true, 反之false
     */
    @Override
    public Response<Boolean> delete(Long id) {
        Response<Boolean> resp = new Response<Boolean>();
        try {
            expressInfoDao.delete(id);
            resp.setResult(Boolean.TRUE);
        } catch (Exception e){
            log.error("failed to delete express info(id={}), cause: {}", id, Throwables.getStackTraceAsString(e));
            resp.setError("express.info.delete.fail");
        }
        return resp;
    }

    /**
     * 通过名称查询快递信息
     *
     * @param name 快递名称
     * @return ExpressInfo或者Null
     */
    @Override
    public Response<ExpressInfo> findByName(String name) {
        Response<ExpressInfo> resp = new Response<ExpressInfo>();
        try {
            resp.setResult(expressInfoDao.findByName(name));
        } catch (Exception e){
            log.error("failed to find express info by name({}), cause: {}", name, Throwables.getStackTraceAsString(e));
            resp.setError("express.info.find.fail");
        }
        return resp;
    }

    /**
     * 获取快递信息列表, admin调用
     *
     * @param criteria 查询条件
     * @return 快递信息列表
     */
    @Override
    public Response<List<ExpressInfo>> list(Map<String, Object> criteria) {
        Response<List<ExpressInfo>> resp = new Response<List<ExpressInfo>>();
        try {
            if (Objects.equal("", criteria.get("status"))){
                criteria.put("status", null);
            }
            resp.setResult(expressInfoDao.list(criteria));
        } catch (Exception e){
            log.error("failed to list express infoes, criteria={}, cause: {}", criteria, Throwables.getStackTraceAsString(e));
            resp.setError("express.info.list.fail");
        }
        return resp;
    }

    /**
     * 获取所有启用的快递信息, web调用
     *
     * @return 启用的快递信息
     */
    @Override
    public Response<Collection<ExpressInfo>> listEnables(BaseUser user) {
        Response<Collection<ExpressInfo>> resp = new Response<Collection<ExpressInfo>>();
        try {
            final List<Long> usualIds = expressInfoRedisDao.usualExpressInfoIds(user.getId());
            List<ExpressInfo> enables = enableExpressInfoCache.get(ENABLED_EXPRESS_INFO_KEY);
            Collection<ExpressInfo> myEnables = Collections2.filter(enables, new Predicate<ExpressInfo>() {
                @Override
                public boolean apply(ExpressInfo expressInfo) {
                    return !usualIds.contains(expressInfo.getId());
                }
            });
            resp.setResult(myEnables);
        } catch (Exception e){
            log.error("failed to list all enabled expresses, cause: {}", Throwables.getStackTraceAsString(e));
            resp.setError("express.info.list.fail");
        }
        return resp;
    }

    /**
     * 分页获取快递信息
     *
     * @param pageNo   页号
     * @param pageSize 分页大小
     * @param criteria 查询条件
     * @return 快递分页信息
     */
    @Override
    public Response<Paging<ExpressInfo>> paging(Integer pageNo, Integer pageSize, Map<String, Object> criteria) {
        Response<Paging<ExpressInfo>> resp = new Response<Paging<ExpressInfo>>();
        try {
            PageInfo page = new PageInfo(pageNo, pageSize);
            resp.setResult(expressInfoDao.paging(page.getOffset(), page.getLimit(), criteria));
        } catch (Exception e){
            log.error("failed to paging express infoes(pageNo={}, pageSize={}, criteria={}), cause: {}",
                    pageNo, pageSize, criteria, Throwables.getStackTraceAsString(e));
            resp.setError("express.info.paging.fail");
        }
        return resp;
    }

    /**
     * 获取用户常用的快递列表
     * @param user 用户
     * @return 常用快递信息列表
     */
    @Override
    public Response<List<ExpressInfo>> usual(BaseUser user) {
        Response<List<ExpressInfo>> resp = new Response<List<ExpressInfo>>();
        try {
            Long userId = user.getId();
            List<Long> expressInfoIds = expressInfoRedisDao.usualExpressInfoIds(userId);
            if (Iterables.isEmpty(expressInfoIds)){
                resp.setResult(Collections.<ExpressInfo>emptyList());
                return resp;
            }
            // make cache enable
            enableExpressInfoCache.get(ENABLED_EXPRESS_INFO_KEY);

            List<ExpressInfo> enableExpressInfos = Lists.newArrayList();
            List<Long> invalidExpressInfoIds = Lists.newArrayList();
            ExpressInfo expressInfo;
            for (Long expressInfoId : expressInfoIds){
                expressInfo = enableExpressInfoMapCache.get(expressInfoId);
                if (expressInfo != null && Objects.equal(ExpressInfo.Status.ENABLED.value(), expressInfo.getStatus())){
                    enableExpressInfos.add(expressInfo);
                } else {
                    // 无效了
                    invalidExpressInfoIds.add(expressInfoId);
                }
            }
            if (!Iterables.isEmpty(invalidExpressInfoIds)){
                expressInfoRedisDao.rmFromUsual(userId, invalidExpressInfoIds);
            }
            resp.setResult(enableExpressInfos);
        } catch (Exception e){
            log.error("failed to find usual expresses of user({}), cause: {}", user, Throwables.getStackTraceAsString(e));
            resp.setError("express.info.usual.find.fail");
        }
        return resp;
    }

    /**
     * 为用户添加常用快递
     *
     * @param userId        用户id
     * @param expressInfoId 常用快递id
     * @return 添加成功返回true, 反之false
     */
    @Override
    public Response<Boolean> add2Usual(Long userId, Long expressInfoId) {
        Response<Boolean> resp = new Response<Boolean>();
        try {
            List<Long> usualIds = expressInfoRedisDao.usualExpressInfoIds(userId);
            if (usualIds.contains(expressInfoId)){
                resp.setResult(Boolean.TRUE);
                return resp;
            }
            ExpressInfo expressInfo = enableExpressInfoMapCache.get(expressInfoId);
            if(!Objects.equal(ExpressInfo.Status.ENABLED.value(), expressInfo.getStatus())){
                log.error("express info({}) status isn't enabled.", expressInfo);
                resp.setError("express.info.status.disabled");
                return resp;
            }
            expressInfoRedisDao.add2Usual(userId, expressInfoId);
            resp.setResult(Boolean.TRUE);
        } catch (Exception e){
            log.error("failed to add express info(id={}) to user(id={})'s usual, cause: {}",
                    expressInfoId, userId, Throwables.getStackTraceAsString(e));
            resp.setError("express.info.usual.add.fail");
        }
        return resp;
    }

    /**
     * 移除用户的常用快递
     *
     * @param userId        用户id
     * @param expressInfoId 常用快递id
     * @return 移除成功返回true, 反之false
     */
    @Override
    public Response<Boolean> rmFromUsual(Long userId, Long expressInfoId) {
        Response<Boolean> resp = new Response<Boolean>();
        try {
            expressInfoRedisDao.rmFromUsual(userId, expressInfoId);
            resp.setResult(Boolean.TRUE);
        } catch (Exception e){
            log.error("failed to remove express info(id={}) from user(id={})'s usual, cause: {}",
                    expressInfoId, userId, Throwables.getStackTraceAsString(e));
            resp.setError("express.info.usual.rm.fail");
        }
        return resp;
    }
}
