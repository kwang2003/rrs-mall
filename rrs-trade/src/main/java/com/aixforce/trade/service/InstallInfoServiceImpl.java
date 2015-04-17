package com.aixforce.trade.service;

import com.aixforce.common.model.PageInfo;
import com.aixforce.common.model.Paging;
import com.aixforce.common.model.Response;
import com.aixforce.trade.dao.InstallInfoDao;
import com.aixforce.trade.model.InstallInfo;
import com.google.common.base.Objects;
import com.google.common.base.Throwables;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 安装信息服务实现
 * Author: haolin
 * On: 9/22/14
 */
@Service @Slf4j
public class InstallInfoServiceImpl implements InstallInfoService {

    @Autowired
    private InstallInfoDao installInfoDao;

    private static final Integer ENABLED_INSTALL_INFO_KEY = 1;

    // enable install infos cache   <type, list>
    private LoadingCache<Integer, List<InstallInfo>> enableInstallInfoCache;

    public InstallInfoServiceImpl(){
        enableInstallInfoCache = CacheBuilder.newBuilder().expireAfterWrite(5, TimeUnit.MINUTES).build(new CacheLoader<Integer, List<InstallInfo>>(){
            @Override
            public List<InstallInfo> load(Integer type) throws Exception {
                Map<String, Object> criteria = Maps.newHashMapWithExpectedSize(2);
                criteria.put("status", InstallInfo.Status.ENABLED.value());
                criteria.put("type", type);
                List<InstallInfo> expressInfos = installInfoDao.list(criteria);
                return expressInfos;
            }
        });
    }
    /**
     * 创建安装信息
     *
     * @param installInfo 安装信息
     * @return 安装信息id
     */
    @Override
    public Response<Long> create(InstallInfo installInfo) {
        Response<Long> resp = new Response<Long>();
        try {
            InstallInfo existed = installInfoDao.findByName(installInfo.getName());
            if (existed == null){
                installInfo.setStatus(InstallInfo.Status.DISABLED.value());
                installInfoDao.create(installInfo);
            } else if (Objects.equal(InstallInfo.Status.DELETED.value(), existed.getStatus())){
                // 被逻辑删除过, 将其状态==>DISABLED
                existed.setStatus(InstallInfo.Status.DISABLED.value());
                installInfoDao.update(existed);
                installInfo = existed;
            } else {
                // 已经存在
                log.error("InstallInfo name({}) has existed.", installInfo.getName());
                resp.setError("install.info.existed");
                return resp;
            }
            resp.setResult(installInfo.getId());
        } catch (Exception e){
            log.error("failed to create install info({}), cause: {}", installInfo, Throwables.getStackTraceAsString(e));
            resp.setError("install.info.create.fail");
        }
        return resp;
    }

    /**
     * 更新安装信息
     *
     * @param installInfo 安装信息
     * @return 更新成功返回true, 反之false
     */
    @Override
    public Response<Boolean> update(InstallInfo installInfo) {
        Response<Boolean> resp = new Response<Boolean>();
        try {
            installInfoDao.update(installInfo);
            resp.setResult(Boolean.TRUE);
        } catch (Exception e){
            log.error("failed to update install info({}), cause: {}", installInfo, Throwables.getStackTraceAsString(e));
            resp.setError("install.info.update.fail");
        }
        return resp;
    }

    /**
     * 逻辑删除安装信息
     *
     * @param id 安装信息id
     * @return 删除成功返回true, 反之false
     */
    @Override
    public Response<Boolean> delete(Long id) {
        Response<Boolean> resp = new Response<Boolean>();
        try {
            installInfoDao.delete(id);
            resp.setResult(Boolean.TRUE);
        } catch (Exception e){
            log.error("failed to delete install info(id={}), cause: {}", id, Throwables.getStackTraceAsString(e));
            resp.setError("install.info.delete.fail");
        }
        return resp;
    }

    /**
     * 通过名称查询安装信息
     *
     * @param name 安装名称
     * @return InstallInfo或者Null
     */
    @Override
    public Response<InstallInfo> findByName(String name) {
        Response<InstallInfo> resp = new Response<InstallInfo>();
        try {
            resp.setResult(installInfoDao.findByName(name));
        } catch (Exception e){
            log.error("failed to find install info by name({}), cause: {}", name, Throwables.getStackTraceAsString(e));
            resp.setError("install.info.find.fail");
        }
        return resp;
    }

    /**
     * 获取安装信息列表, admin调用
     *
     * @param criteria 查询条件
     * @return 安装信息列表
     */
    @Override
    public Response<List<InstallInfo>> list(Map<String, Object> criteria) {
        Response<List<InstallInfo>> resp = new Response<List<InstallInfo>>();
        try {
            if (Objects.equal("", criteria.get("status"))){
                criteria.put("status", null);
            }
            resp.setResult(installInfoDao.list(criteria));
        } catch (Exception e){
            log.error("failed to list install infoes, criteria={}, cause: {}", criteria, Throwables.getStackTraceAsString(e));
            resp.setError("install.info.list.fail");
        }
        return resp;
    }

    /**
     * 获取所有启用的安装信息, web调用
     *
     * @return 启用的安装信息
     */
    @Override
    public Response<List<InstallInfo>> listEnables(Integer type) {
        Response<List<InstallInfo>> resp = new Response<List<InstallInfo>>();
        try {
            InstallInfo.Type t = InstallInfo.Type.from(type);
            if (t == null){
                log.error("invalid install info type({})", type);
                resp.setError("install.info.type.invalid");
                return resp;
            }
            resp.setResult(enableInstallInfoCache.get(type));
        } catch (Exception e){
            log.error("failed to list all enabled installes, cause: {}", Throwables.getStackTraceAsString(e));
            resp.setError("install.info.list.fail");
        }
        return resp;
    }

    /**
     * 分页获取安装信息
     *
     * @param pageNo   页号
     * @param pageSize 分页大小
     * @param criteria 查询条件
     * @return 安装分页信息
     */
    @Override
    public Response<Paging<InstallInfo>> paging(Integer pageNo, Integer pageSize, Map<String, Object> criteria) {
        Response<Paging<InstallInfo>> resp = new Response<Paging<InstallInfo>>();
        try {
            PageInfo page = new PageInfo(pageNo, pageSize);
            if (Objects.equal("", criteria.get("status"))){
                criteria.put("status", null);
            }
            resp.setResult(installInfoDao.paging(page.getOffset(), page.getLimit(), criteria));
        } catch (Exception e){
            log.error("failed to paging install infoes(pageNo={}, pageSize={}, criteria={}), cause: {}",
                    pageNo, pageSize, criteria, Throwables.getStackTraceAsString(e));
            resp.setError("install.info.paging.fail");
        }
        return resp;
    }
}
