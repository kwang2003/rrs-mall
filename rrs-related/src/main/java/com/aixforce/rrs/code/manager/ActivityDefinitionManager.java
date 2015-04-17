package com.aixforce.rrs.code.manager;

import com.aixforce.common.model.Response;
import com.aixforce.item.model.Item;
import com.aixforce.item.service.ItemService;
import com.aixforce.rrs.code.dao.ActivityBindDao;
import com.aixforce.rrs.code.dao.ActivityCodeDao;
import com.aixforce.rrs.code.dao.ActivityDefinitionDao;
import com.aixforce.rrs.code.model.ActivityBind;
import com.aixforce.rrs.code.model.ActivityCode;
import com.aixforce.rrs.code.model.ActivityDefinition;

import com.google.common.base.Strings;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @author wanggen on 14-7-5.
 * @Desc: 优惠活动及其优惠码，商品的事务性写管理
 */
@Component
@Slf4j
public class ActivityDefinitionManager {

    @Autowired
    private ActivityDefinitionDao activityDefinitionDao;

    @Autowired
    private ActivityCodeDao activityCodeDao;

    @Autowired
    private ActivityBindDao activityBindDao;

    @Autowired
    private ItemService itemService;


    /**
     * 创建优活动及优惠码
     * 1.创建优惠活动
     * 2.插入该优惠活动发放的优惠码
     * 3.将该优惠活动中可优惠的商品初始化
     *
     * @param actDef 优惠活动信息
     * @param codes  相关优惠码
     * @param items  指定可以商品
     * @return 成功创建的活动ID
     */
    @Transactional(rollbackFor = {Exception.class, RuntimeException.class})
    public Long create(ActivityDefinition actDef, List<String> codes, List<Long> items, Integer itemType) {

        //NO.1 将优惠活动定义新增到  `activity_definition`
        long createdActivityId = activityDefinitionDao.create(actDef);

        //NO.2 将该活动发放的码插入到 `activity_codes` 表
        if (codes != null)
            for (String code : codes) {
                saveActivityCode(actDef, createdActivityId, code);
            }

        //NO.3 优惠活动 bind 多个 item
        if (items != null)
            for (Long itemId : items) {
                saveActivityItemBinds(actDef.getDiscount(), itemType, createdActivityId, itemId);
            }

        return createdActivityId;

    }


    /**
     * 保存优惠码
     * @param actDef            当前待保存优惠码所属优惠活动
     * @param createdActivityId 优惠活动ID
     * @param code              待保存优惠码
     */
    private void saveActivityCode(ActivityDefinition actDef, long createdActivityId, String code) {
        if(Strings.isNullOrEmpty(code) || code.trim().equals(""))
            return;
        ActivityCode activityCode = new ActivityCode();
        activityCode.setActivityType(actDef.getActivityType());
        activityCode.setActivityName(actDef.getActivityName());
        activityCode.setUsage(0);
        activityCode.setCode(code.toLowerCase());
        activityCode.setActivityId(createdActivityId);
        activityCodeDao.create(activityCode);
    }


    /**
     * 关联优惠活动与商品
     * @param discount  优惠价
     * @param itemType  商品类别
     * @param activityId 优惠活动ID
     * @param itemId    商品ID
     */
    private void saveActivityItemBinds(Integer discount, Integer itemType, long activityId, Long itemId) {
        Response<Item> itemResponse = itemService.findById(itemId);
        Item item = itemResponse.getResult();

        if(item==null){
            log.error("Illegal itemId:[{}] passed in for it does not exits in `items`", itemId);
            throw new IllegalStateException("activityDefinition.illegal.itemId");
        }
        if(item.getPrice()==null || discount>item.getPrice()){
            log.error("The Item's price:{} must be higher than discount:{} but not", item.getPrice(), discount);
            throw new IllegalStateException("activityDefinition.illegal.discount");
        }

        ActivityBind activityBind = new ActivityBind();
        activityBind.setActivityId(activityId);
        activityBind.setTargetId(itemId);
        activityBind.setTargetType(itemType);
        activityBindDao.create(activityBind);
    }



    /**
     * 更新优惠活动
     */
    @Transactional(rollbackFor = {Exception.class})
    public Integer update(ActivityDefinition actDef, List<Long> items, Integer itemType) {

        //1.更新优惠活动信息
        int updatedNum = activityDefinitionDao.update(actDef);

        //2.将该活动原相关商品删除
        if (items != null && items.size()>0) {

            activityBindDao.deleteActivityBindByActivityId(actDef.getId());

            //3.重新将传入的商品初始化到 `activity_bind` 表中
            for (Long itemId : items) {
                saveActivityItemBinds(actDef.getDiscount(), itemType, actDef.getId(), itemId);
            }

        }

        return updatedNum;
    }


    /**
     * 根据优惠活动 ids 删除优惠活动
     *
     * @param ids 优惠活动 id 列表
     */
    @Transactional
    public int deleteActivityDefinitionByIds(List<Long> ids) {

        if (ids == null || ids.isEmpty())
            return 0;

        //1.删除优惠活动定义
        int deleted = activityDefinitionDao.deleteByIds(ids);

        //2.删除优惠活动发放的所有优惠码
        activityCodeDao.deleteByActivityIds(ids);

        //3.删除优惠活动中可使用优惠码的 item
        for (Long id : ids)
            activityBindDao.deleteActivityBindByActivityId(id);

        return deleted;
    }
}
