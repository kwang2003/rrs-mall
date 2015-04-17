package com.aixforce.item.service;

import com.aixforce.category.service.SpuService;
import com.aixforce.common.model.PageInfo;
import com.aixforce.common.model.Paging;
import com.aixforce.common.model.Response;
import com.aixforce.item.dao.mysql.SpuBundleDao;
import com.aixforce.item.model.SpuBundle;
import com.aixforce.user.base.BaseUser;
import com.google.common.base.Throwables;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * 套餐模版服务实现类
 * CREATED BY: IntelliJ IDEA
 * AUTHOR: haolin
 * ON: 14-4-21
 */
@Service
@Slf4j
public class SpuBundleServiceImpl implements SpuBundleService {

    @Autowired
    private SpuBundleDao spuBundleDao;

    @Autowired
    private SpuService spuService;


    @Override
    public Response<Long> create(SpuBundle sb) {
        Response<Long> result = new Response<Long>();

        paramsValidate(result, sb, "create");

        if (!result.isSuccess()) {
            return result;
        }

        try {
            // 设置SPU冗余名称
            redSpusName(sb);
            spuBundleDao.create(sb);
            result.setResult(sb.getId());
            return result;
        } catch (Exception e) {
            log.error("fail to create spubundle{}, cause:{}", sb, Throwables.getStackTraceAsString(e));
            result.setError("spubundle.create.fail");
            return result;
        }
    }

    /**
     * 设置SpuBundle的各个spu名称
     *
     * @param sb SpuBundle对象
     */
    private void redSpusName(SpuBundle sb) {
        sb.setNameOne(spuService.findById(sb.getIdOne()).getResult().getName());
        sb.setNameTwo(spuService.findById(sb.getIdTwo()).getResult().getName());
        if (sb.getIdThree() != null && sb.getIdThree() > 0) {
            sb.setNameThree(spuService.findById(sb.getIdThree()).getResult().getName());
        }
        if (sb.getIdFour() != null && sb.getIdFour() > 0) {
            sb.setNameFour(spuService.findById(sb.getIdFour()).getResult().getName());
        }
    }

    @Override
    public Response<Boolean> update(SpuBundle sb) {
        Response<Boolean> result = new Response<Boolean>();

        paramsValidate(result, sb, "update");

        if (!result.isSuccess()) {
            return result;
        }

        try {
            spuBundleDao.update(sb);
            result.setResult(Boolean.TRUE);
            return result;
        } catch (Exception e) {
            log.error("fail to update spubundle{}, cause:{}", sb, Throwables.getStackTraceAsString(e));
            result.setError("spubundle.update.fail");
            return result;
        }
    }

    @Override
    public Response<Boolean> delete(Long sbId) {
        Response<Boolean> result = new Response<Boolean>();
        if (sbId == null || sbId < 0) {
            log.error("spubundle id({}) is illegal when delete", sbId);
            result.setError("illegal.param");
            return result;
        }
        try {
            spuBundleDao.delete(sbId);
            result.setResult(Boolean.TRUE);
        } catch (Exception e) {
            log.error("failed to delete spubundle(id={})", sbId, Throwables.getStackTraceAsString(e));
            result.setError("spubundle.delete.fail");
        }
        return result;
    }

    @Override
    public Response<Boolean> onOff(Long sbId, SpuBundle.Status status) {
        Response<Boolean> result = new Response<Boolean>();
        if (sbId == null) {
            log.error("spubundle id can't be null when set on or off");
            result.setError("illegal.param");
            return result;
        }
        try {
            spuBundleDao.onOff(sbId, status);
            result.setResult(Boolean.TRUE);
        } catch (Exception e) {
            log.error("failed to on or off spubundle(id={})", sbId, Throwables.getStackTraceAsString(e));
            result.setError("spubundle.onoff.fail");
        }
        return result;
    }

    @Override
    public Response<Boolean> incrUsedCount(Long sbId) {
        Response<Boolean> result = new Response<Boolean>();
        if (sbId == null) {
            log.error("spubundle id can't be null when increment usedCount");
            result.setError("illegal.param");
            return result;
        }
        try {
            spuBundleDao.incrUsedCount(sbId);
            result.setResult(Boolean.TRUE);
        } catch (Exception e) {
            log.error("failed to increment spubundle.usedCount(id={})", sbId, Throwables.getStackTraceAsString(e));
            result.setError("spubundle.incrusedcount.fail");
        }
        return result;
    }

    @Override
    public Response<SpuBundle> findById(Long sbId) {
        Response<SpuBundle> result = new Response<SpuBundle>();
        if (sbId == null || sbId < 0) {
            log.error("spubundle id({}) is illegal when findById", sbId);
            result.setError("illegal.param");
            return result;
        }
        try {
            result.setResult(spuBundleDao.findById(sbId));
            result.setSuccess(Boolean.TRUE);
        } catch (Exception e) {
            log.error("failed to find spubundle(id={})", sbId, Throwables.getStackTraceAsString(e));
            result.setError("spubundle.findById.fail");
        }
        return result;
    }

    @Override
    public Response<Paging<SpuBundle>> finds(BaseUser user, Map<String, Object> criterion, Integer pageNo, Integer pageSize) {
        PageInfo page = new PageInfo(pageNo, pageSize);
        Response<Paging<SpuBundle>> result = new Response<Paging<SpuBundle>>();

        try {
            Long userId = user.getId();
            if (userId == null) {
                log.error("user doesn't login.");
                result.setError("user.not.login");
                return result;
            }
            criterion.put("userId", userId);
            Paging<SpuBundle> sbSet = spuBundleDao.paging(criterion, page.getOffset(), page.getLimit());
            result.setResult(sbSet);
        } catch (Exception e) {
            log.error("failed to paging spubundle(pageNo={}, pageSize)",
                    page.getOffset(), page.getLimit(), Throwables.getStackTraceAsString(e));
            result.setError("spubundle.paging.fail");
        }
        return result;
    }

    /**
     * 根据用户id列表 找用户模板
     *
     * @param userIds  用户id列表
     * @param pageNo   页码
     * @param pageSize 分页大小
     * @return 套餐模板分页对象
     */
    @Override
    public Response<Paging<SpuBundle>> findByUserIds(List<Long> userIds, Integer pageNo, Integer pageSize) {
        Response<Paging<SpuBundle>> result = new Response<Paging<SpuBundle>>();
        if (userIds == null || userIds.isEmpty()) {
            result.setResult(new Paging<SpuBundle>(0L,Collections.<SpuBundle>emptyList()));
            return result;
        }
        PageInfo page = new PageInfo(pageNo, pageSize);
        try{
            Paging<SpuBundle> spuBundles =  spuBundleDao.pagingByUsers(userIds,page.getOffset(), page.getLimit());
            result.setResult(spuBundles);
            return result;
        }catch (Exception e){
            log.error("failed to find spuBundles by userIds({}), cause:{}", userIds, Throwables.getStackTraceAsString(e));
            result.setError("spubundle.paging.fail");
            return result;
        }
    }


    /**
     * 参数验证
     *
     * @param sb SpuBundle对象
     * @param op 操作
     * @parms result 返回对象
     */
    private void paramsValidate(Response<?> result, SpuBundle sb, String op) {

        if (sb == null) {
            log.error("spubundle can not be null when {} spubundle", op);
            result.setError("illegal.param");
            return;
        }

        if (sb.getName() == null) {
            log.error("spubundle name can not be null when {} spubundle", op);
            result.setError("illegal.param");
            return;
        }
        if (sb.getIdOne() == null || sb.getIdOne() < 0) {
            log.error("spubundle spu idOne can not be null when {} spubundle", op);
            result.setError("illegal.param");
            return;
        }
        if (sb.getIdTwo() == null || sb.getIdTwo() < 0) {
            log.error("spubundle spu idTwo can not be null when {} spubundle", op);
            result.setError("illegal.param");
            return;
        }
        result.setSuccess(Boolean.TRUE);
    }
}
