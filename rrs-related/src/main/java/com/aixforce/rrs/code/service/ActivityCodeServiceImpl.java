package com.aixforce.rrs.code.service;

import com.aixforce.common.model.PageInfo;
import com.aixforce.common.model.Paging;
import com.aixforce.common.model.Response;
import com.aixforce.rrs.code.dao.ActivityCodeDao;
import com.aixforce.rrs.code.model.ActivityCode;
import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableMap;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * @author wanggen on 14-7-6.
 */
@Service
@Slf4j
public class ActivityCodeServiceImpl implements ActivityCodeService {

    @Autowired
    private ActivityCodeDao activityCodeDao;

    @Override
    public Response<List<ActivityCode>> findAllBy(Map<String, Object> param) {
        Response<List<ActivityCode>> resp = new Response<List<ActivityCode>>();
        try{
            resp.setResult(activityCodeDao.findAllBy(param));
            return resp;
        }catch (Exception e){
            resp.setError("coupon-code.read.failed");
            log.error("Failed to find from `activity_codes` with param:{}", param, e);
            return resp;
        }
    }


    /**
     * 分页查询
     * @param param     查询参数
     * @param pageNo    查询页码
     * @param pageSize  每页记录数量
     * @return          分页查询结果
     */
    @Override
    public Response<Paging<ActivityCode>> findByPaging(Map<String, Object> param, Integer pageNo, Integer pageSize){
        Response<Paging<ActivityCode>> resp = new Response<Paging<ActivityCode>>();
        try{
            PageInfo pageInfo = new PageInfo(pageNo, pageSize);
            param.put("offset", pageInfo.offset);
            param.put("limit", pageInfo.limit);
            resp.setResult(activityCodeDao.findByPaging(param));
            return resp;
        }catch (Exception e){
            resp.setError("coupon-code.read.failed");
            log.error("Failed to findByPaging from `activity_codes` with param:{}",param , e);
            return resp;
        }
    }

    @Override
    public Response<ActivityCode> findOneByActivityIdAndCode(Long actId, String code) {
        Response<ActivityCode> resp = new Response<ActivityCode>();
        try{
            ActivityCode activityCode = activityCodeDao.findOneByActivityIdAndCode(actId, code);
            if(activityCode == null) {
                log.error("activity code not found by actId={}, code={}", actId, code);
                resp.setError("coupon-code.not.found");
                return resp;
            }
            resp.setResult(activityCode);
            return resp;
        }catch (Exception e){
            resp.setError("coupon-code.read.failed");
            log.error("Faild to findOneByActivityIdAndCode from `activity_codes` with param actId:{},code:{}", actId, code, e);
            return resp;
        }
    }

    @Override
    public Response<Boolean> batchUpdateByIds(Map<Long, Integer> param) {
        Response<Boolean> response = new Response<Boolean>();
        if(param == null || param.isEmpty()) {
            log.error("param can not be null or empty when batch update usage");
            response.setError("illegal.param");
            return response;
        }
        try{
            for(Map.Entry entry: param.entrySet()){
                activityCodeDao.updateUsageById(
                        ImmutableMap.of("id", entry.getKey(), "usage", entry.getValue()));
            }
            response.setResult(Boolean.TRUE);
            return response;
        }catch(Exception e){
            response.setError("coupon-code.update.failed");
            log.error("Failed to update from `activity_codes` with param:{}", param, e);
            return response;
        }

    }

    @Override
    public Response<Integer> countUsageByActivityId(Long id) {
        Response<Integer> resp = new Response<Integer>();
        try{
            resp.setResult(activityCodeDao.countUsageByActivityId(id));
            return resp;
        }catch (Exception e){
            resp.setError("coupon-code.read.failed");
            log.error("Failed to count usage from `activity_codes` with param:{}", id, e);
            return resp;
        }
    }

    /**
     * 根据优惠码code查找关联的activityIds
     * @param code
     * @return
     */
    @Override
    public Response<List<Long>> findActivityIdsByCode(String code){
        Response<List<Long>> result = new Response<List<Long>>();
        if (code == null) {
            log.error("param can not be null");
            result.setError("illegal.param");
            return result;
        }
        try {
            List<Long>  activityIdList =  activityCodeDao.findActivityIdsByCode(code);
            if(activityIdList==null){
                log.error("no activityIds(code = {}) found", code);
                result.setError("activityIds.not.found");
            }
            result.setResult(activityIdList);
            return result;

        } catch (Exception e) {
            log.error("failed no activityIs(code = {}) found, cause:{}", code, Throwables.getStackTraceAsString(e));
            result.setError("find activityIds by code");
            result.setError("activityId.query.failed");
            return result;
        }
    }

    /**
     * 根据活动id查询活动关联的所有code
     * @param activityId 活动id
     * @return 优惠码集合
     */
    @Override
    public Response<Paging<ActivityCode>> findCodesByActivityId(Long activityId,Integer pageNo,Integer count){
        PageInfo pageInfo = new PageInfo(pageNo, count);
        Response<Paging<ActivityCode>> result = new Response<Paging<ActivityCode>>();
        Paging<ActivityCode> codePaging;


        try {

            if(activityId!=null){
                log.error("param can not be null");
                result.setError("illegal.param");
                return result;
            }else{
                codePaging = activityCodeDao.findCodesByActivityId(activityId, pageInfo.getOffset(), pageInfo.getLimit());
            }
            result.setResult(codePaging);
            return result;
        } catch (Exception e) {
            log.error("find codes by activity id(activityId = {}) found, cause:{}", activityId, Throwables.getStackTraceAsString(e));
            result.setError("coupon-code.read.failed");
            return result;
        }
    }
}
