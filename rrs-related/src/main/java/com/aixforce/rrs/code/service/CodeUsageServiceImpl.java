package com.aixforce.rrs.code.service;

import com.aixforce.annotations.ParamInfo;
import com.aixforce.common.model.PageInfo;
import com.aixforce.common.model.Paging;
import com.aixforce.common.model.Response;
import com.aixforce.rrs.code.dao.CodeUsageDao;
import com.aixforce.rrs.code.dto.CodeOrderDto;
import com.aixforce.rrs.code.manager.CodeManager;
import com.aixforce.rrs.code.model.CodeUsage;
import com.aixforce.trade.model.Order;
import com.aixforce.trade.service.OrderQueryService;
import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Nullable;
import java.util.List;

import static com.aixforce.common.utils.Arguments.isNull;
import static com.aixforce.common.utils.Arguments.notNull;
import static com.google.common.base.Preconditions.checkArgument;

/**
 * 优惠码绑定service
 * <p/>
 * Mail: 964393552@qq.com <br>
 * Date: 2014-07-03 PM  <br>
 * Author: songrenfei
 */
@Slf4j
@Service
public class CodeUsageServiceImpl implements CodeUsageService {

    @Autowired
    private CodeUsageDao codeUsageDao;
    @Autowired
    private OrderQueryService orderQueryService;

    @Autowired
    private CodeManager codeManager;


    @Override
    public Response<CodeUsage> create(CodeUsage codeUsage) {

        Response<CodeUsage> result = new Response<CodeUsage>();

        try {
            checkArgument(notNull(codeUsage), "code.usage.can.not.be.empty");
            codeManager.createCodeUsage(codeUsage);
            result.setResult(codeUsage);
        } catch (Exception e) {
            log.error("failed to create codeUsage {}, cause:{}", codeUsage, Throwables.getStackTraceAsString(e));
            result.setError("codeUsage.create.failed");
        }

        return result;
    }



    @Override
    public Response<Boolean> update(CodeUsage codeUsage) {
        Response<Boolean> result = new Response<Boolean>();
        if (codeUsage == null) {
            log.error("params can not be null");
            result.setError("illegal.param");
            return result;
        }

        try {
            result.setResult(codeUsageDao.update(codeUsage));
            return result;
        } catch (Exception e) {
            log.error("failed to update codeUsage {}, cause:{}", codeUsage, Throwables.getStackTraceAsString(e));
            result.setError("codeUsage.update.failed");
        }
        return result;
    }

    @Override
    public Response<Boolean> delete(Long id) {
        Response<Boolean> result = new Response<Boolean>();
        try {
            result.setResult(codeUsageDao.delete(id));
            return result;
        } catch (Exception e) {
            log.error("failed to delete codeBind (id={}), cause:{}", id, Throwables.getStackTraceAsString(e));
            result.setError("codeBind.delete.failed");
        }
        return result;
    }

    @Override
    public Response<CodeUsage> findById(Long id) {

        Response<CodeUsage> result = new Response<CodeUsage>();
        try {
            CodeUsage cu = codeUsageDao.findById(id);
            if (cu == null) {
                log.error("no codeUsage(id = {}) found", id);
                result.setError("codeUsage.not.found");
                return result;
            }
            result.setResult(cu);
            return result;
        } catch (Exception e) {
            log.error("failed to find codeUsage(id = {}), cause:{}", id, Throwables.getStackTraceAsString(e));
            result.setError("codeUsage.query.failed");
            return result;
        }
    }

    @Override
    public Response<CodeUsage> findByName(String code) {
        Response<CodeUsage> result = new Response<CodeUsage>();
        if (code == null) {
            log.error("params can not be null");
            result.setError("illegal.param");
            return result;
        }

        try {
            CodeUsage cu = codeUsageDao.findByName(code);
            if (cu == null) {
                log.error("no codeUsage(code_name = {}) found", code);
                result.setError("codeUsage.not.found");
                return result;
            }
            result.setResult(cu);
            return result;
        } catch (Exception e) {
            log.error("failed to find codeUsage(code_name = {}), cause:{}", code, Throwables.getStackTraceAsString(e));
            result.setError("codeUsage.query.failed");
            return result;
        }
    }


    /**
     * 根据优惠活动id 查询关联的订单列表(CodeOrderDto)
     *
     * @param activityId 优惠活动id
     * @return CodeOrderDto列表
     */
    @Override
    public Response<Paging<CodeOrderDto>> getCodeOrderDtoByActivityId(@ParamInfo("activityId") @Nullable String activityId,
                                                                      @ParamInfo("pageNo") @Nullable Integer pageNo,
                                                                      @ParamInfo("count") @Nullable Integer count) {
        PageInfo pageInfo = new PageInfo(pageNo, count);
        Response<Paging<CodeOrderDto>> result = new Response<Paging<CodeOrderDto>>();
        if (activityId == null) {
            log.error("params can not be null");
            result.setError("illegal.param");
            return result;
        }
        try {
            Paging<CodeUsage> codeUsagePaging = codeUsageDao.getCodeUsageByActivityId(Long.valueOf(activityId), pageInfo.getOffset(), pageInfo.getLimit());
            List<CodeOrderDto> dtoList = Lists.newArrayList();
            for (CodeUsage codeUsage : codeUsagePaging.getData()) {
                Order order = orderQueryService.findById(codeUsage.getOrderId()).getResult();
                CodeOrderDto codeOrderDto = new CodeOrderDto();
                codeOrderDto.setPrice(codeUsage.getPrice());
                codeOrderDto.setCode(codeUsage.getCode());
                if (order != null) {
                    codeOrderDto.setCreatedAt(order.getCreatedAt());
                    codeOrderDto.setOrderId(order.getId());
                    codeOrderDto.setStatus(order.getStatus());
                    codeOrderDto.setBuyerId(order.getBuyerId());
                }
                codeOrderDto.setDiscount(codeUsage.getDiscount());
                codeOrderDto.setOriginPrice(codeUsage.getOriginPrice());
                dtoList.add(codeOrderDto);
            }
            result.setResult(new Paging<CodeOrderDto>(codeUsagePaging.getTotal(), dtoList));
            return result;
        } catch (Exception e) {
            log.error("failed to find codeOrderDto by activityId {}, cause:{}", activityId, Throwables.getStackTraceAsString(e));
            result.setError("get.codeOrderDto.by.activityId.query.failed");
            return result;
        }

    }

    /**
     * 批量更新优惠码使用情况
     *
     * @param codeUsageList 优惠码是有情况集合
     * @return true更新成功false更新失败
     */
    @Override
    public Response<Boolean> batchUpdateCodeUsage(List<CodeUsage> codeUsageList) {
        Response<Boolean> result = new Response<Boolean>();
        checkArgument(notNull(codeUsageList), "codeUsageList.is.empty");
        if(notNull(codeUsageList)){
            for(CodeUsage codeUsage : codeUsageList){
                try {
                    codeUsageDao.update(codeUsage);
                } catch (Exception e) {
                    log.error("update CodeUsage where codeUsage(id={}), cause:{}",codeUsage.getId(), Throwables.getStackTraceAsString(e));
                    result.setError("codeUsage.update.failed");
                    return result;
                }

            }
        }
        result.setResult(Boolean.TRUE);
        return result;
    }

    /**
     * 批量创建优惠码使用情况
     *
     * @param codeUsageList 优惠码是有情况集合
     * @return true更新成功false更新失败
     */
    @Override
    public Response<Boolean> batchCreateCodeUsage(List<CodeUsage> codeUsageList) {
        Response<Boolean> result = new Response<Boolean>();
        checkArgument(notNull(codeUsageList), "codeUsageList.is.empty");
        if (notNull(codeUsageList)) {
            for (CodeUsage codeUsage : codeUsageList) {
                try {
                    codeManager.createCodeUsage(codeUsage);
                } catch (Exception e) {
                    log.error("create CodeUsage where codeUsage(codeName={}), cause:{}", codeUsage.getCode(), Throwables.getStackTraceAsString(e));
                    result.setError("create CodeUsage failed");
                    return result;
                }
            }
        }
        result.setResult(Boolean.TRUE);
        return result;
    }

    /**
     * 查询优惠活动id为当前优惠活动id的所有codeUsage记录
     * @param activityId 优惠活动id
     * @return codeUsge集合
     */
    @Override
    public Response<List<CodeUsage>> getAllCodeUsageByActivityId(Long activityId){

        Response<List<CodeUsage>> result = new Response<List<CodeUsage>>();
        if (activityId == null) {
            log.error("params can not be null");
            result.setError("illegal.param");
            return result;
        }

        try {
            List<CodeUsage> codeUsageList  = codeUsageDao.getAllCodeUsageByActivityId(activityId);
            if (codeUsageList == null) {
                log.error("no CodeUsage found by activity_id={} ", activityId);
                result.setError("codeUsage.not.found");
                return result;
            }
            result.setResult(codeUsageList);
            return result;
        } catch (Exception e) {
            log.error("failed to find CodeUsage(activityId = {}), cause:{}", activityId, Throwables.getStackTraceAsString(e));
            result.setError("codeUsage.query.failed");
            return result;
        }
    }


    /**
     * 根据优惠活动id 查询所有关联的订单列表(CodeOrderDto) 无分页功能
     * @param activityId 优惠码名称
     * @return CodeOrderDto列表
     */
    public Response<List<CodeOrderDto>> getAllCodeOrderDtoByActivityId(@ParamInfo("activityId") @Nullable String activityId){
        Response<List<CodeOrderDto>> result = new Response<List<CodeOrderDto>>();

        if (activityId == null) {
            log.error("params can not be null");
            result.setError("illegal.param");
            return result;
        }
        List<CodeUsage> codeUsageList;
        try {
            codeUsageList = codeUsageDao.getAllCodeUsageByActivityId(Long.valueOf(activityId));
            if (codeUsageList == null) {
                log.error("no CodeUsage found by activity_id={} ", activityId);
                result.setError("codeUsage.not.found");
                return result;
            }

            List<CodeOrderDto> codeOrderDtoList  = Lists.newArrayList();

            Order order;
            for (CodeUsage codeUsage : codeUsageList){
                order = orderQueryService.findById(codeUsage.getOrderId()).getResult();
                CodeOrderDto codeOrderDto = new CodeOrderDto();
                codeOrderDto.setPrice(codeUsage.getPrice());
                codeOrderDto.setCode(codeUsage.getCode());
                if(order!=null){
                    codeOrderDto.setCreatedAt(order.getCreatedAt());
                    codeOrderDto.setOrderId(order.getId());
                    codeOrderDto.setStatus(order.getStatus());
                }
                codeOrderDto.setDiscount(codeUsage.getDiscount());
                codeOrderDto.setOriginPrice(codeUsage.getOriginPrice());
                codeOrderDtoList.add(codeOrderDto);
            }

            result.setResult(codeOrderDtoList);
            return result;
        } catch (Exception e) {
            log.error("failed to find CodeUsage(activityId = {}), cause:{}", activityId, Throwables.getStackTraceAsString(e));
            result.setError("codeUsage.query.failed");
            return result;
        }

    }


    /**
     * 根据订单id查找codeUsage
     * @param orderId 订单id
     * @return codeUsge集合
     */
    @Override
    public Response<CodeUsage> getCodeUsageByOrderId(Long orderId){
        Response<CodeUsage> result = new Response<CodeUsage>();
        if (orderId == null) {
            log.error("params can not be null");
            result.setError("illegal.param");
            return result;
        }

        try {
            CodeUsage codeUsage  = codeUsageDao.getCodeUsageByOrderId(orderId);
            if (codeUsage == null) {
                log.error("no CodeUsage found by order_id={} ", orderId);
                result.setError("codeUsage.not.found");
                return result;
            }
            result.setResult(codeUsage);
            return result;
        } catch (Exception e) {
            log.error("failed to find CodeUsage(orderId = {}), cause:{}", orderId, Throwables.getStackTraceAsString(e));
            result.setError("codeUsage.query.failed");
            return result;
        }
    }

    @Override
    public Response<Boolean> updateOrderId(Long oldId, Long newId) {
        Response<Boolean> result = new Response<Boolean>();
        try {
            checkArgument(!isNull(oldId),"illegal.param");
            checkArgument(!isNull(newId),"illegal.param");
            codeUsageDao.updateOrderId(oldId,newId);
            result.setResult(Boolean.TRUE);
        }catch (IllegalArgumentException e){
            log.error("update codeUsages set newOrderId={},oldOrderId={} fail,error={}",newId,oldId,e.getMessage());
            result.setError(e.getMessage());
        }catch (Exception e){
            log.error("update codeUsages set newOrderId={},oldOrderId={} fail,cause:{}",newId,oldId,Throwables.getStackTraceAsString(e));
            result.setError("code.useages.update.fail");
        }
        return result;
    }

}
