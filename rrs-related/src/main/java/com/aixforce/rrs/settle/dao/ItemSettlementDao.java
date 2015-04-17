package com.aixforce.rrs.settle.dao;

import com.aixforce.common.model.PageInfo;
import com.aixforce.common.model.Paging;
import com.aixforce.rrs.settle.model.ItemSettlement;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import org.mybatis.spring.support.SqlSessionDaoSupport;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static com.aixforce.common.utils.Dates.endOfDay;
import static com.aixforce.common.utils.Dates.startOfDay;

/**
 * Mail: remindxiao@gmail.com <br>
 * Date: 2014-01-22 10:33 AM  <br>
 * Author: xiao
 */
@Repository
public class ItemSettlementDao extends SqlSessionDaoSupport {

    /**
     * 创建子订单结算记录
     *
     * @param itemSettlement 子订单结算记录
     * @return 新记录的id
     */
    public Long create(ItemSettlement itemSettlement) {
        getSqlSession().insert("ItemSettlement.create", itemSettlement);
        return itemSettlement.getId();
    }

    /**
     * 根据记录id查找
     *
     * @param id 记录id
     * @return 查询结果
     */
    public ItemSettlement get(Long id) {
        return getSqlSession().selectOne("ItemSettlement.get", id);
    }

    public ItemSettlement getBy(ItemSettlement criteria) {
        return getSqlSession().selectOne("ItemSettlement.getBy",
                ImmutableMap.of("criteria", criteria));
    }


    public Paging<ItemSettlement> findBy(ItemSettlement.Type type, Date paidAt, Integer pageNo, Integer size) {
        ItemSettlement criteria = new ItemSettlement();
        criteria.setPaidAt(paidAt);
        criteria.setType(type.value());
        PageInfo pageInfo = new PageInfo(pageNo, size);
        return findBy(criteria, pageInfo.offset, pageInfo.limit);
    }


    public Paging<ItemSettlement> findBy(ItemSettlement criteria, Integer offset, Integer limit) {
        Map<String, Object> params = Maps.newHashMapWithExpectedSize(20);
        params.put("criteria", criteria);
        params.put("offset", offset);
        params.put("limit", limit);

        if (criteria.getConfirmedAt() != null) {
            Date confirmedAt = criteria.getConfirmedAt();
            params.put("confirmedStartAt", startOfDay(confirmedAt));
            params.put("confirmedEndAt", endOfDay(confirmedAt));
        }
        if (criteria.getPaidAt() != null) {
            Date paidAt = criteria.getPaidAt();
            params.put("paidStartAt", startOfDay(paidAt));
            params.put("paidEndAt", endOfDay(paidAt));
        }

        return findBy(params);
    }

    public Paging<ItemSettlement> findBy(Map<String, Object> params) {
        Long total = getSqlSession().selectOne("ItemSettlement.countOf", params);
        if (total == 0L) {
            return new Paging<ItemSettlement>(0L, Collections.<ItemSettlement>emptyList());
        }
        List<ItemSettlement> settlements = getSqlSession().selectList("ItemSettlement.findBy", params);
        return new Paging<ItemSettlement>(total, settlements);
    }

    /**
     * 更新
     * @param settlement 结算记录
     * @return 是否更新成功
     */
    public boolean update(ItemSettlement settlement) {
        return getSqlSession().update("ItemSettlement.update", settlement) == 1;
    }

    /**
     * 更新结算凭证号
     *
     * @param id      子订单结算记录id
     * @param voucher 结算凭证号
     * @return 是否更新成功
     */
    public boolean updateVoucher(Long id, String voucher) {
        return getSqlSession().update("ItemSettlement.updateVoucher",
                ImmutableMap.of("id", id, "voucher", voucher)) > 0;
    }

    /**
     * 根据订单号获取子订单结算信息
     *
     * @param orderId   订单号
     * @return          子订单结算列表
     */
    public List<ItemSettlement> list(Long orderId) {
        Map<String, Object> params = Maps.newHashMapWithExpectedSize(1);
        params.put("orderId", orderId);
        return getSqlSession().selectList("ItemSettlement.list", params);
    }

    /**
     * 根据订单号查询子订单结算分页信息
     *
     * @param orderId   订单号
     * @return 符合条件的分页信息
     */
    public List<ItemSettlement> findByOrderId(Long orderId) {
        Map<String, Object> params = Maps.newHashMapWithExpectedSize(3);
        ItemSettlement criteria = new ItemSettlement();
        criteria.setOrderId(orderId);
        params.put("criteria", criteria);
        params.put("offset", 0);
        params.put("limit", 10000);
        return getSqlSession().selectList("ItemSettlement.findBy", params);
    }

    /**
     * 根据子订单号查询子订单结算分页信息
     *
     * @param orderItemId   子订单号
     * @return 符合条件的分页信息
     */
    public ItemSettlement findByOrderItemId(Long orderItemId) {
        Map<String, Object> params = Maps.newHashMapWithExpectedSize(1);
        params.put("orderItemId", orderItemId);
        return getSqlSession().selectOne("ItemSettlement.findByOrderItemId", params);
    }

    /**
     * 子订单结算信息标记关闭，同时需要更新交易状态
     *
     * @param itemSettlement     子订单
     */
    public boolean finished(ItemSettlement itemSettlement) {
        return getSqlSession().update("ItemSettlement.finished",
                ImmutableMap.of("settlement", itemSettlement)) > 0;

    }

    /**
     * 批量更新符合条件的子订单信息
     *
     * @param sellerId                  商户号
     * @param voucher                   凭证号
     * @param thirdPartyReceipt         第三方（支付宝手续费）发票号
     * @param confirmedStartAt          查询起始时间（基于确认时间）
     * @param confirmedEndAt            查询截止时间（基于截止时间）
     * @return   是否更新成功
     */
    public boolean batchVouched(Long sellerId, String voucher, String thirdPartyReceipt, Date confirmedStartAt, Date confirmedEndAt) {
        Map<String, Object> params = Maps.newHashMapWithExpectedSize(2);
        params.put("sellerId", sellerId);
        params.put("voucher", voucher);
        params.put("thirdPartyReceipt", thirdPartyReceipt);
        params.put("confirmedStartAt", confirmedStartAt);
        params.put("confirmedEndAt", confirmedEndAt);
        getSqlSession().update("ItemSettlement.batchVouched", params);
        return true;
    }

    /**
     * 批量更新符合条件的子订单信息  <br/>
     * 结算状态 -> "已结算"
     *
     * @param confirmedStartAt      查询起始时间（基于确认时间）
     * @param confirmedEndAt        查询截止时间（基于截止时间）
     */
    public boolean batchSynced(Date confirmedStartAt, Date confirmedEndAt) {
        Map<String, Object> params = Maps.newHashMapWithExpectedSize(2);
        params.put("confirmedStartAt", confirmedStartAt);
        params.put("confirmedEndAt", confirmedEndAt);
        getSqlSession().update("ItemSettlement.batchSynced", params);
        return true;
    }

    /**
     * 删除
     * @param id    标识
     */
    public void delete(Long id) {
        getSqlSession().delete("ItemSettlement.delete", id);
    }


    /**
     * 标记为补帐记录
     */
    public Boolean fixed(Long id) {
        ItemSettlement updating = new ItemSettlement();
        updating.setId(id);
        updating.setFixed(Boolean.TRUE);
        return update(updating);
    }

   public ItemSettlement findByOrderIdAndType(Long orderId,Integer type){
       return getSqlSession().selectOne("ItemSettlement.findByOrderIdAndType", ImmutableMap.of("orderId",orderId,"type",type));
   }
}
