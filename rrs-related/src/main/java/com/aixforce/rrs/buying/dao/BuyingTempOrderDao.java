package com.aixforce.rrs.buying.dao;

import com.aixforce.common.model.Paging;
import com.aixforce.rrs.buying.model.BuyingTempOrder;
import com.google.common.collect.ImmutableMap;
import org.mybatis.spring.support.SqlSessionDaoSupport;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Created by songrenfei on 14-9-22.
 */

@Repository
public class BuyingTempOrderDao extends SqlSessionDaoSupport {

    public Long create(BuyingTempOrder buyingTempOrder) {
        getSqlSession().insert("BuyingTempOrder.create", buyingTempOrder);
        return buyingTempOrder.getId();
    }

    public boolean delete(Long id) {
        return getSqlSession().delete("BuyingTempOrder.delete", id) == 1;
    }

    public boolean update(BuyingTempOrder buyingTempOrder) {
        return getSqlSession().update("BuyingTempOrder.update", buyingTempOrder) == 1;
    }

    public BuyingTempOrder findById(Long id) {
        return getSqlSession().selectOne("BuyingTempOrder.findById", id);
    }


    public Paging<BuyingTempOrder> paging(Map<String, Object> param){

        Long total = getSqlSession().selectOne("BuyingTempOrder.count", param);
        if(total==null||total==0){
            return Paging.empty(BuyingTempOrder.class);
        }
        List<BuyingTempOrder> buyingTempOrderList =getSqlSession().selectList("BuyingTempOrder.paging",param);

        return new Paging<BuyingTempOrder>(total,buyingTempOrderList);
    }


    /**
     * 根据活动id和商品id获取商品的销售数量
     * @param activityId 活动id
     * @param itemId 商品id
     * @return 销售数量
     */
    public Integer getSaleQuantity(Long activityId, Long itemId){

        return getSqlSession().selectOne("BuyingTempOrder.getSaleQuantity",ImmutableMap.of("activityId", activityId,
                "itemId", itemId));
    }

    /**
     * 得到具体用户已抢购的数量
     * @param activityId 活动id
     * @param itemId 商品id
     * @param userId 用户id
     * @return 数量
     */
    public Integer getHasBuyQuantity(Long activityId, Long itemId,Long userId){

        return getSqlSession().selectOne("BuyingTempOrder.getHasBuyQuantity",ImmutableMap.of("activityId", activityId,
                "itemId", itemId,"userId",userId));
    }


    public BuyingTempOrder findByOrderId(Long orderId) {
        return getSqlSession().selectOne("BuyingTempOrder.findByOrderId", orderId);
    }


    /**
     * 根据订单列表查询虚拟订单
     * @param orderIds  订单id列表
     * @return  虚拟订单列表
     */
    public List<BuyingTempOrder> findInOrderIds(List<Long> orderIds) {
        return getSqlSession().selectList("BuyingTempOrder.findInOrderIds", orderIds);
    }

    public Boolean updateToOutDate(Date now){
        getSqlSession().update("BuyingTempOrder.updateToOutDate",now);
        return true;
    }

    /**
     * 更新总订单的code
     * @param oldId
     * @param newId
     */
    public void updateOrderId(Long oldId, Long newId) {
        if(oldId==null||newId==null){
            return;
        }
        getSqlSession().update("BuyingTempOrder.updateOrderId", ImmutableMap.of("oldId", oldId, "newId", newId));
    }
}
