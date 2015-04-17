package com.aixforce.rrs.code.service;

import com.aixforce.annotations.ParamInfo;
import com.aixforce.common.model.Paging;
import com.aixforce.common.model.Response;
import com.aixforce.rrs.code.dto.CodeOrderDto;
import com.aixforce.rrs.code.model.CodeUsage;
import com.aixforce.trade.model.Order;

import javax.annotation.Nullable;
import java.util.List;

/**
 * 优惠码绑定service
 *
 * Mail: 964393552@qq.com <br>
 * Date: 2014-07-03 PM  <br>
 * Author: songrenfei
 */
public interface CodeUsageService {


    /**
     * 创建优惠码使用情况
     * @param codeUsage
     * @return
     */
    Response<CodeUsage> create(CodeUsage codeUsage);


    /**
     * 更新 优惠码使用情况
     * @param codeUsage
     * @return
     */
    Response<Boolean> update(CodeUsage codeUsage);

    /**
     * 删除优惠码使用情况
     * @param id 优惠码使用情况id
     * @return
     */
    Response<Boolean> delete(Long id);

    /**
     * 根据优惠码使用情况id查找该条记录
     * @param id 优惠码使用情况id
     * @return
     */
    public Response<CodeUsage> findById(Long id);

    /**
     * 根据优惠码名称查找该条记录
     * @param code 优惠码使用情况id
     * @return
     */
    public Response<CodeUsage> findByName(String code);

    /**
     * 根据优惠活动id 查询关联的订单列表(CodeOrderDto) 有分页功能
     * @param activityId 优惠码名称
     * @return CodeOrderDto列表
     */
    public Response<Paging<CodeOrderDto>> getCodeOrderDtoByActivityId(@ParamInfo("activityId") @Nullable String activityId,
                                                               @ParamInfo("pageNo") @Nullable Integer pageNo,
                                                               @ParamInfo("count") @Nullable Integer count);

    /**
     * 批量更新优惠码使用情况
     * @param codeUsageList 优惠码是有情况集合
     * @return true更新成功false更新失败
     */
    public Response<Boolean> batchUpdateCodeUsage(List<CodeUsage> codeUsageList);

    /**
     * 批量创建优惠码使用情况
     * @param codeUsageList 优惠码是有情况集合
     * @return true更新成功false更新失败
     */
    public Response<Boolean> batchCreateCodeUsage(List<CodeUsage> codeUsageList);

    /**
     * 查询优惠活动id为当前优惠活动id的所有codeUsage记录 没有分页功能
     * @param activityId 优惠活动id
     * @return codeUsge集合
     */
    public Response<List<CodeUsage>> getAllCodeUsageByActivityId(Long activityId);

    /**
     * 根据优惠活动id 查询所有关联的订单列表(CodeOrderDto) 无分页功能
     * @param activityId 优惠码名称
     * @return CodeOrderDto列表
     */
    public Response<List<CodeOrderDto>> getAllCodeOrderDtoByActivityId(@ParamInfo("activityId") @Nullable String activityId);

    /**
     * 根据订单id查找codeUsage
     * @param orderId 订单id
     * @return codeUsge集合
     */
    public Response<CodeUsage> getCodeUsageByOrderId(Long orderId);

    /**
     * 更新子订单中总订单的code
     * @param oldId
     * @param newId
     */
    public Response<Boolean> updateOrderId(Long oldId, Long newId);
}
