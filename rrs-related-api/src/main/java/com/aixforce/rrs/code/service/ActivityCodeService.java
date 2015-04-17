package com.aixforce.rrs.code.service;

import com.aixforce.common.model.Paging;
import com.aixforce.common.model.Response;
import com.aixforce.rrs.code.model.ActivityCode;

import java.util.List;
import java.util.Map;

/**
 * @author wanggen on 14-7-6.
 * @Desc:
 */
public interface ActivityCodeService {

    /**
     * 根据查询条件查询
     *
     * @param param 可选查询参数 [id | code | activityId | activityName | activityType(1or2)]
     * @return 分页查询结果
     */
    Response<List<ActivityCode>> findAllBy(Map<String, Object> param);



    /**
     * 分页查询
     * @param param     可选查询参数 [id | code | activityId | activityName | activityType(1or2)]
     * @param pageNo    页码
     * @param pageSize  每页数量
     * @return          分页查询结果
     */
    Response<Paging<ActivityCode>> findByPaging(Map<String, Object> param, Integer pageNo, Integer pageSize);


    /**
     * 根据活动id及优惠码查询 活动-码 关系信息
     *
     * @return 优惠活动发放的优惠码信息
     */
    Response<ActivityCode>  findOneByActivityIdAndCode(Long actId, String code);


    /**
     * 根据 活动-码 表的 ids 批量更新码的使用数量
     *
     * @param param `activity_code` 表的 id-usage 键值对
     */
    Response<Boolean> batchUpdateByIds(Map<Long, Integer> param);


    /**
     * 根据活动 id 统计该活动发放所有码的使用数量和
     *
     * @param id 活动id
     * @return 该活动发放的所有码的使用情况
     */
    Response<Integer> countUsageByActivityId(Long id);

    /**
     * 根据优惠码code查找关联的activityIds
     * @param code
     * @return 活动id集合
     */

   Response<List<Long>> findActivityIdsByCode(String code);

    /**
     * 根据活动id查询活动关联的所有code
     * @param activityId 活动id
     * @return 优惠码集合
     */
   Response<Paging<ActivityCode>> findCodesByActivityId(Long activityId,Integer pageNo,Integer count);




}
