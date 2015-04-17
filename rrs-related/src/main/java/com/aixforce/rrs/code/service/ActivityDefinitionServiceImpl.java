package com.aixforce.rrs.code.service;

import com.aixforce.common.model.PageInfo;
import com.aixforce.common.model.Paging;
import com.aixforce.common.model.Response;
import com.aixforce.common.utils.BeanMapper;
import com.aixforce.rrs.code.dao.ActivityBindDao;
import com.aixforce.rrs.code.dao.ActivityCodeDao;
import com.aixforce.rrs.code.dao.ActivityDefinitionDao;
import com.aixforce.rrs.code.dto.RichActivityDefinition;
import com.aixforce.rrs.code.manager.ActivityDefinitionManager;
import com.aixforce.rrs.code.model.ActivityBind;
import com.aixforce.rrs.code.model.ActivityCode;
import com.aixforce.rrs.code.model.ActivityDefinition;
import com.google.common.base.Objects;
import com.google.common.base.Strings;
import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * @author wanggen
 */
@Service
@Slf4j
public class ActivityDefinitionServiceImpl implements ActivityDefinitionService {

    private final static DateTimeFormatter DFT = DateTimeFormat.forPattern("yyyy-MM-dd");

    @Autowired
    private ActivityDefinitionDao activityDefinitionDao;      //codeDefinitionDao-管理优惠活动信息

    @Autowired
    private ActivityCodeDao activityCodeDao;

    @Autowired
    private ActivityBindDao activityBindDao;

    @Autowired
    private ActivityDefinitionManager codeDefinitionManager;

    @Override
    public Response<Long> create(ActivityDefinition activityDefinition, List<String> codes, List<Long> items, Integer itemType) {
        Response<Long> resp = new Response<Long>();
        try {
            Long createdId = codeDefinitionManager.create(activityDefinition, codes, items, Objects.firstNonNull(itemType, 1));
            resp.setResult(createdId);
        } catch (IllegalArgumentException e) {
            resp.setError(e.getMessage());
            log.error("Failed to insert record with param:[{}] into `code_definitions`, error:{}", activityDefinition, e.getMessage());
        } catch (IllegalStateException e) {
            resp.setError(e.getMessage());
            log.error("Failed to insert record with param:[{}] into `code_definitions`, error:{}", activityDefinition, e.getMessage());
        } catch (Exception e) {
            resp.setError("activityDefinition.insert.failed");
            log.error("Failed to insert record with param:[{}] into `code_definitions`, cause:{}", activityDefinition, Throwables.getStackTraceAsString(e));
        }

        return resp;
    }

    @Override
    public Response<ActivityDefinition> findActivityDefinitionById(Long id) {
        Response<ActivityDefinition> response = new Response<ActivityDefinition>();
        try {
            ActivityDefinition actDef = activityDefinitionDao.findById(id);
            response.setResult(actDef);
            return response;
        } catch (Exception e) {
            response.setError("activityDefinition.select.failed");
            log.error("Failed to find ActivityDefinition by id={}", e);
            return response;
        }
    }

    @Override
    public Response<RichActivityDefinition> findRichActivityDefinitionById(Long id) {
        Response<RichActivityDefinition> response = new Response<RichActivityDefinition>();
        try {
            ActivityDefinition activityDefinition = activityDefinitionDao.findById(id);
            if (activityDefinition == null) {
                response.setResult(null);
                return response;
            }
            RichActivityDefinition richActivityDefinition = new RichActivityDefinition();
            BeanMapper.copy(activityDefinition, richActivityDefinition);
            if (Objects.equal(activityDefinition.getActivityType(), ActivityDefinition.ActivityType.PUBLIC_CODE.value())) {
                List<ActivityCode> codes = activityCodeDao.findByActivityId(id);
                richActivityDefinition.setCode(codes.get(0).getCode());
            }
            List<Long> bindIds = activityBindDao.findBindIdsByActivityId(id, ActivityBind.TargetType.ITEM.toNumber());
            //richActivityDefinition.setItemIds(JOINER.join(bindIds));
            richActivityDefinition.setItemIds(bindIds);
            response.setResult(richActivityDefinition);
            return response;
        } catch (Exception e) {
            response.setError("activityDefinition.select.failed");
            log.error("Failed to find full ActivityDefinition by id={}", e);
            return response;
        }
    }

    @Override
    public Response<List<ActivityDefinition>> findValidActivityDefinitionsByCode(String code) {
        Response<List<ActivityDefinition>> response = new Response<List<ActivityDefinition>>();
        try {
            List<Long> activityIds = activityCodeDao.findActivityIdsByCode(code.toLowerCase());
            if(activityIds == null || activityIds.isEmpty()) {
                response.setResult(Collections.<ActivityDefinition>emptyList());
                return response;
            }
            List<ActivityDefinition> actDef = activityDefinitionDao.findValidByIds(activityIds);
            response.setResult(actDef);
            return response;
        } catch (Exception e) {
            response.setError("activityDefinition.select.failed");
            log.error("Failed to find ActivityDefinitions by code:{}", code, e);
            return response;
        }
    }



    @Override
    public Response<Paging<RichActivityDefinition>> findActivityDefinitionByPaging(Map<String, Object> params,
                                                                               Integer pageNo,
                                                                               Integer count) {

        Response<Paging<RichActivityDefinition>> pubDtosResp = new Response<Paging<RichActivityDefinition>>();

        PageInfo pageInfo = new PageInfo(pageNo, count);
        Map<String, Object> qParams = Maps.newHashMap();
        qParams.put("offset", pageInfo.offset);
        qParams.put("limit", pageInfo.limit);

        String activityName = (String) params.get("activityName");
        if (!Strings.isNullOrEmpty(activityName)) {
            qParams.put("activityName", activityName);
        }
        String activityType = (String) params.get("activityType");
        if (!Strings.isNullOrEmpty(activityType)) {
            qParams.put("activityType", Integer.valueOf(activityType));
        }
        String businessIdStr = (String) params.get("businessId");
        if (!Strings.isNullOrEmpty(businessIdStr)) {
            qParams.put("businessId", Integer.valueOf(businessIdStr));
        }
        String channelTypeStr = (String) params.get("channelType");
        if (!Strings.isNullOrEmpty(channelTypeStr)) {
            qParams.put("channelType", Integer.valueOf(channelTypeStr));
        }
        String startAtStr = (String) params.get("startAt");
        if (!Strings.isNullOrEmpty(startAtStr)) {
            qParams.put("startAt", DFT.parseDateTime(startAtStr).toDate());
        }
        String endAtStr = (String) params.get("endAt");
        if (!Strings.isNullOrEmpty(endAtStr)) {
            qParams.put("endAt", DFT.parseDateTime(endAtStr).toDate());
        }
        String statusStr = (String) params.get("status");
        if (!Strings.isNullOrEmpty(statusStr)) {
            qParams.put("status", Integer.valueOf(statusStr));
        }

        try{

            //NO1. 根据查询条件查询出优惠活动列表分页
            Paging<ActivityDefinition> actDefsPaging = activityDefinitionDao.findByPaging(qParams);
            List<ActivityDefinition> actDefs = actDefsPaging.getData();

            //NO2. 将优惠活动信息 ActivityDefinition 封装到 PublicActivityCodeDto
            List<RichActivityDefinition> dtoList = Lists.newArrayList();

            if (actDefs != null) {
                for (ActivityDefinition actDef : actDefs) {
                    //NO2.1 如果该活动发放的公开码，将公开码信息 set 到dto
                    RichActivityDefinition richActivityDefinition = new RichActivityDefinition();
                    BeanMapper.copy(actDef, richActivityDefinition);
                    if (Objects.equal(activityType, String.valueOf(ActivityDefinition.ActivityType.PUBLIC_CODE.value()))) {
                        List<ActivityCode> codes = activityCodeDao.findByActivityId(actDef.getId());
                        richActivityDefinition.setCode(codes.get(0).getCode());
                    }
                    //得到活动绑定的targetId 目前只查的target_type=1 item
                   List<Long> binids = activityBindDao.findBindIdsByActivityId(actDef.getId(),1);
                   //String itemIds=Joiner.on(" ").skipNulls().join(binids);
                   richActivityDefinition.setItemIds(binids);
                   dtoList.add(richActivityDefinition);
                }
            }

            pubDtosResp.setResult(new Paging<RichActivityDefinition>(actDefsPaging.getTotal(), dtoList));
            return pubDtosResp;

        }catch (Exception e){
            pubDtosResp.setError("activityDefinition.select.failed");
            log.error("Failed to query from `activity_definitions` by param:{} ", params, e);
            return pubDtosResp;

        }

    }


    @Override
    public Response<Integer> update(ActivityDefinition actDef, List<Long> items, Integer itemType) {
        Response<Integer> resp = new Response<Integer>();
        Date now = new DateTime().withTimeAtStartOfDay().toDate();
        try {
            if(actDef.getStartAt()!=null) {
                actDef.setStartAt(new DateTime(actDef.getStartAt()).withTimeAtStartOfDay().toDate());
            }
            if(actDef.getEndAt()!=null){
                actDef.setEndAt(new DateTime(actDef.getEndAt()).plusDays(1).withTimeAtStartOfDay().minusSeconds(1).toDate());
                checkArgument(now.compareTo(actDef.getEndAt()) <= 0, "end.date.not.less.than.now.date");
                //NO.3 根据活动开始日期与截止日期判断活动状态 (待生效 or 立即生效)
                if (now.compareTo(actDef.getStartAt()) >= 0 && now.compareTo(actDef.getEndAt()) <= 0)
                    actDef.setStatus(ActivityDefinition.Status.OK.toNumber());
                else
                    actDef.setStatus(ActivityDefinition.Status.INIT.toNumber());
            }


            Integer count = codeDefinitionManager.update(actDef, items, Objects.firstNonNull(itemType, 1));
            resp.setResult(count);
            return resp;
        }  catch (IllegalArgumentException e) {
            resp.setError("activityDefinition.update.failed");
            log.error("Failed to update record with param:[{}] table `activity_definitions`, Caused by:{}", actDef, Throwables.getStackTraceAsString(e));
            return resp;
        }catch (Exception e) {
            resp.setError("activityDefinition.update.failed");
            log.error("Failed to update record with param:[{}] table `activity_definitions`, Caused by:{}", actDef, Throwables.getStackTraceAsString(e));
            return resp;
        }
    }


    @Override
    public Response<Integer> deleteActivityDefinitionByIds(List<Long> ids) {
        Response<Integer> resp = new Response<Integer>();
        try {
            resp.setResult(codeDefinitionManager.deleteActivityDefinitionByIds(ids));
            return resp;
        } catch (Exception e) {
            resp.setError("activityDefinition.delete.failed");
            log.error("Failed to delete record with param:[{}] table `activity_definition`, Caused by:{}", ids, Throwables.getStackTraceAsString(e));
            return resp;
        }
    }


    /**
     * 根据优惠码 ids 查询相关的有效优惠活动定义
     *
     * @param ids 优惠码id集合
     * @return 优惠码相关的优惠活动
     */
    @Override
    public Response<List<ActivityDefinition>> findValidActivityDefinitionsByIds(List<Long> ids) {
        Response<List<ActivityDefinition>> result = new Response<List<ActivityDefinition>>();
        if (ids == null) {
            log.error("param can not be null");
            result.setError("illegal.param");
            return result;
        }
        try {
            List<ActivityDefinition> activityDefinitionList = activityDefinitionDao.findValidByIds(ids);
            if (activityDefinitionList == null) {
                log.error("no activityDefinition(ids = {}) found", ids);
                result.setError("activityDefinition.not.found");
            }
            result.setResult(activityDefinitionList);
            return result;

        } catch (Exception e) {
            log.error("failed no activityDefinition(ids = {}) found, cause:{}", ids, Throwables.getStackTraceAsString(e));
            result.setError("activityBind.query.failed");
            return result;
        }
    }

    @Override
    public Response<Boolean> updateToEffect() {
        Response<Boolean> result = new Response<Boolean>();
        try {
            result.setResult(activityDefinitionDao.updateToEffect());
            return result;
        } catch (Exception e) {
            log.error("failed to update activityDefinition, cause:{}", Throwables.getStackTraceAsString(e));
            result.setError("activityDefinition.update.failed");
        }
        return result;
    }

    @Override
    public Response<Boolean> updateToExpiry() {
        Response<Boolean> result = new Response<Boolean>();
        try {
            result.setResult(activityDefinitionDao.updateToExpiry());
            return result;
        } catch (Exception e) {
            log.error("failed to update activityDefinition, cause:{}", Throwables.getStackTraceAsString(e));
            result.setError("activityDefinition.update.failed");
        }
        return result;
    }

    @Override
    public Response<Boolean> updateToExpiryByHand(Long id) {

        Response<Boolean> result = new Response<Boolean>();
        try {
            result.setResult(activityDefinitionDao.updateToExpiryByHand(id));
            return result;
        } catch (Exception e) {
            log.error("failed to update activityDefinition id={}, cause:{}", id,Throwables.getStackTraceAsString(e));
            result.setError("activityDefinition.update.failed");
        }
        return result;

    }

}
