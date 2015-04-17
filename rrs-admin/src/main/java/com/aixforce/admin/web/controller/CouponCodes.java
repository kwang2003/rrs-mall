package com.aixforce.admin.web.controller;

import com.aixforce.common.model.Paging;
import com.aixforce.common.model.Response;
import com.aixforce.exception.JsonResponseException;
import com.aixforce.rrs.code.dto.ActivityDefinitionDto;
import com.aixforce.rrs.code.dto.RichActivityDefinition;
import com.aixforce.rrs.code.model.ActivityCode;
import com.aixforce.rrs.code.model.ActivityDefinition;
import com.aixforce.rrs.code.service.ActivityCodeService;
import com.aixforce.rrs.code.service.ActivityDefinitionService;
import com.aixforce.web.misc.MessageSources;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotNull;
import java.util.Date;
import java.util.HashSet;
import java.util.List;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * Created by IntelliJ IDEA.
 * User: AnsonChan
 * Date: 14-7-5
 */
@Controller
@Slf4j
@RequestMapping(value = "/api/admin/coupon-code")
public class CouponCodes {

    @Autowired
    private ActivityDefinitionService activityDefinitionService;

    @Autowired
    private ActivityCodeService activityCodeService;

    @Autowired
    private MessageSources messageSources;

    @RequestMapping(value = "/{id}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public RichActivityDefinition get(@PathVariable Long id) {
        Response<RichActivityDefinition> resp = activityDefinitionService.findRichActivityDefinitionById(id);
        if (!resp.isSuccess()) {
            throw new JsonResponseException(500, messageSources.get(resp.getError()));
        }
        return resp.getResult();
    }

    /**
     * 创建优惠活动，并导入优惠码
     *
     * @return 新增的优惠活动的 ID 号
     */
    @RequestMapping(method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Long create(@RequestBody ActivityDefinitionDto defDto) {

        if (defDto == null)
            throw new JsonResponseException(400, "输入参数不能为空");

        try {
            validation(defDto.getActivityDefinition(), defDto.getCodes(), defDto.getItemIds());
        } catch (RuntimeException e) {
            if (e instanceof IllegalStateException || e instanceof IllegalArgumentException)
                throw new JsonResponseException(400, messageSources.get(e.getMessage()));
            else{
                log.error("Error occur when save activity:{}", defDto, e);
                throw new JsonResponseException(500, "输入的参数有误");
            }
        }

        Response<Long> resp = activityDefinitionService.create(defDto.getActivityDefinition(), defDto.getCodes(), defDto.getItemIds(), defDto.getItemType());
        if (!resp.isSuccess())
            throw new JsonResponseException(500, messageSources.get(resp.getError()));
        return resp.getResult();

    }

    private void validation(ActivityDefinition activityDefinition, List<String> codes, List<Long> itemIds) {

        Date now = new DateTime().withTimeAtStartOfDay().toDate();

        //NO.1 非法数据验证
        checkArgument(activityDefinition != null, "优惠活动信息不可为空");
        checkArgument(activityDefinition.getActivityName() != null, "活动名称不能为空");
        checkArgument(activityDefinition.getDiscount() != null && activityDefinition.getDiscount() > 0, "输入的优惠价必须大于0");
        checkArgument(activityDefinition.getStock() == null || activityDefinition.getStock() > 0, "优惠码发放数量或为空，或不为空但不可为负数");
        checkArgument(activityDefinition.getStartAt() != null && activityDefinition.getEndAt() != null, "优惠活动的开始时间与截止时间都不可为空");

        //NO.2 将开始日期日期格式化为 YYYY-MM-dd 00:00:00； 将截止日期格式化为 YYYY-MM-dd 23:59:59
        activityDefinition.setStartAt(new DateTime(activityDefinition.getStartAt()).withTimeAtStartOfDay().toDate());
        Date endAt = new DateTime(activityDefinition.getEndAt()).plusDays(1).withTimeAtStartOfDay().minusSeconds(1).toDate();
        activityDefinition.setEndAt(endAt);
        checkArgument(now.compareTo(activityDefinition.getEndAt()) <= 0, "活动截止日期不可小于当前日期");

        //NO.3 根据活动开始日期与截止日期判断活动状态 (待生效 or 立即生效)
        if (now.compareTo(activityDefinition.getStartAt()) >= 0 && now.compareTo(activityDefinition.getEndAt()) <= 0)
            activityDefinition.setStatus(ActivityDefinition.Status.OK.toNumber());
        else
            activityDefinition.setStatus(ActivityDefinition.Status.INIT.toNumber());
        activityDefinition.setOrderCount(0);

        //NO.4 验证输入的优惠码是否存在重复码
        if (codes == null || codes.size() <= 0)
            throw new JsonResponseException(400, "创建优惠活动失败,优惠码不可为空");
        HashSet<String> distinctCodes = Sets.newHashSet();
        HashSet<String> repeatedCodes = Sets.newHashSet();
        for (int i = 0; i < codes.size(); i++) {
            String code = codes.get(i);
            if (code == null || (code = code.trim()).equals("")) {
                codes.remove(i);
                continue;
            }
            codes.set(i, code);
            if (!distinctCodes.contains(code))
                distinctCodes.add(code);
            else
                repeatedCodes.add(code);

        }
        if (!repeatedCodes.isEmpty())
            throw new JsonResponseException(400, "创建优惠活动失败,下列优惠码出现重复: " + repeatedCodes);

        //NO.5 验证输入商品是否存在重复
        if (itemIds == null || itemIds.size() <= 0)
            throw new JsonResponseException(400, "创建优惠活动失败,必须指定该活动中可优惠商品");
        HashSet<Long> distinctItems = Sets.newHashSet();
        HashSet<Long> repeatedItems = Sets.newHashSet();
        for (Long itemId : itemIds) {
            if (!distinctItems.contains(itemId))
                distinctItems.add(itemId);
            else
                repeatedItems.add(itemId);
        }
        if (!repeatedItems.isEmpty())
            throw new JsonResponseException(400, "创建优惠活动失败,下列商品ID出现重复:" + repeatedItems);
    }


    /**
     * 根据优惠活动查询该活动发放的优惠码
     *
     * @param activityId 优惠活动ID
     * @param pageNo     页码
     * @param pageSize   每页数量
     * @return 活动-码 列表
     */
    @RequestMapping(value = "/{id}/codes", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Paging<ActivityCode> queryActCodesByActDefId(@PathVariable("id") Long activityId, Integer pageNo, Integer pageSize) {

        Response<Paging<ActivityCode>> codesResp = activityCodeService.findCodesByActivityId(activityId, pageNo, pageSize);
        if (!codesResp.isSuccess())
            throw new JsonResponseException(500, messageSources.get(codesResp.getError()));
        return codesResp.getResult();

    }


    /**
     * 更新优活动
     *
     * @return 更新影响的行数，若返回结果 != 1 则未更新任何结果
     */
    @RequestMapping(method = RequestMethod.PUT, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Integer update(@RequestBody ActivityDefinitionDto actDefDto) {

        Response<Integer> resp = activityDefinitionService.update(actDefDto.getActivityDefinition(), actDefDto.getItemIds(), actDefDto.getItemType());
        if (!resp.isSuccess())
            throw new JsonResponseException(500, messageSources.get(resp.getError()));
        return resp.getResult();

    }


    /**
     * 根据 ID 删除优惠活动
     * 1.删除活动信息
     * 2.删除该活动发出的优惠码
     * 3.删除该活动可优惠的商品
     *
     * @param id 自增序列ID
     * @return 删除的行数
     */
    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Integer deleteById(@PathVariable @NotNull Long id) {

        Response<Integer> deletedResp = activityDefinitionService.deleteActivityDefinitionByIds(Lists.newArrayList(id));
        if (!deletedResp.isSuccess())
            throw new JsonResponseException(500, messageSources.get(deletedResp.getError()));
        return deletedResp.getResult();

    }


    /**
     * 根据活动 ID 停用该活动, 将优惠活动状态从已生效状态改为失效状态
     *
     * @param id 待停用的优惠活动序列ID
     * @return 更新影响行数，标识是否停用成功(when >= 1)
     */
    @RequestMapping(value = "/{id}/cancel", method = RequestMethod.PUT, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Integer cancelById(@PathVariable(value = "id") @NotNull Long id) {

        ActivityDefinition actDef = new ActivityDefinition();

        if (id == null)
            throw new JsonResponseException(401, messageSources.get("param.can.not.empty"));

        actDef.setId(id);

        //1.根据活动 ID 查询活动信息;   若该活动不存在或该活动不是有效状态，则中断修改
        Response<ActivityDefinition> foundDefinition = activityDefinitionService.findActivityDefinitionById(id);
        if (foundDefinition.getResult() == null || !foundDefinition.getResult().getStatus().equals(ActivityDefinition.Status.OK.toNumber()))
            return 0;

        actDef.setStatus(ActivityDefinition.Status.STOP.toNumber());

        //2.更新优惠活动
        Response<Integer> updateResponse = activityDefinitionService.update(actDef, null, null);

        if (!updateResponse.isSuccess())
            throw new JsonResponseException(500, messageSources.get(updateResponse.getError()));

        return updateResponse.getResult();

    }

    //测试自动生效
    @RequestMapping(value = "/update/effect", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public String updateToEffect() {
        activityDefinitionService.updateToEffect();
        return "activityDefinition update effect success";
    }

    //测试自动生效
    @RequestMapping(value = "/update/expiry", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public String updateToExpiry() {
        activityDefinitionService.updateToExpiry();
        return "activityDefinition update expiry success";
    }
}
