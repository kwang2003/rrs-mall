package com.aixforce.rrs.code.service;

import com.aixforce.annotations.ParamInfo;
import com.aixforce.common.model.Paging;
import com.aixforce.common.model.Response;
import com.aixforce.rrs.code.dto.RichActivityDefinition;
import com.aixforce.rrs.code.model.ActivityDefinition;

import java.util.List;
import java.util.Map;

/**
 * @author wanggen on 14-7-3.
 */
public interface ActivityDefinitionService {


    /**
     * 新增优惠活动,同时新增该活动中可使用的优惠码
     * @param activityDefinition  活动定义信息
     * @param codes               发放的优惠码
     * @param items               相关商品
     * @param itemType            商品类别
     * @return                    创建成功的活动ID
     */
    Response<Long> create(ActivityDefinition activityDefinition, List<String> codes, List<Long> items, Integer itemType);


    /**
     * 根据优惠活动 ID 查询优惠活动信息
     * @param id  优惠活动 ID
     * @return    优惠活动定义
     */
    Response<ActivityDefinition> findActivityDefinitionById(Long id);

    Response<RichActivityDefinition> findRichActivityDefinitionById(Long id);


    /**
     * 根据优惠码 code 查询相关的优惠活动定义
     * @param code  优惠码
     * @return  优惠码相关的优惠活动
     */
    Response<List<ActivityDefinition>> findValidActivityDefinitionsByCode(String code);


    /**
     * 根据条件分页查询
     * @param params 查询参数
     *               activityName  活动名称精确查询
     *               activityType  活动类别 [1:公开码 | 2:渠道码]
     *               status        活动状态 [1:新建 | 2:已生效; 3:已失效 | 4:人工使失效]
     *               channelType   频道类别 [1:经销商 | 2:服务兵]
     *               pageNo        页码
     *               pageSize      每页数量
     * @return              分页结果
     */
    Response<Paging<RichActivityDefinition>> findActivityDefinitionByPaging(@ParamInfo("params") Map<String, Object> params,
                                                                        @ParamInfo("pageNo") Integer pageNo,
                                                                        @ParamInfo("count") Integer count);

    /**
     * 更新操作，更新优惠活动信息，同时更新优惠活动中关联的商品
     * @param activityDefinition 活动定义信息
     * @param items              相关商品
     * @return                   >=1 更新成功
     */
    Response<Integer> update(ActivityDefinition activityDefinition, List<Long> items, Integer itemType);


    /**
     * 根据序列号 ids 删除优惠活动，同时删除该优惠活动中发放的优惠码
     * @param ids 自增序列号 ids
     * @return 删除的行数
     */
    Response<Integer> deleteActivityDefinitionByIds(List<Long> ids);


    /**
     * 根据优惠活动 ids 查询相关的有效优惠活动定义
     * @param ids  优惠活动id集合
     * @return  有效的优惠活动
     */
    Response<List<ActivityDefinition>> findValidActivityDefinitionsByIds(List<Long>  ids);
    /**
     * 计划任务自动生效
     * @return
     */
    Response<Boolean> updateToEffect();
    /**
     * 计划任务自动失效
     * @return
     */
    Response<Boolean> updateToExpiry();
    /**
     * 手动失效
     * @param id 活动id
     * @return
     */
    Response<Boolean> updateToExpiryByHand(Long id);

}
