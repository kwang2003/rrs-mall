package com.aixforce.rrs.settle.dao;

import com.aixforce.common.model.Paging;
import com.aixforce.rrs.settle.model.SellerSettlement;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import org.joda.time.DateTime;
import org.mybatis.spring.support.SqlSessionDaoSupport;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static com.aixforce.common.utils.Arguments.notNull;
import static com.google.common.base.Preconditions.*;

/**
 * Mail: remindxiao@gmail.com <br>
 * Date: 2014-01-22 10:33 AM  <br>
 * Author: xiao
 */
@Repository
public class SellerSettlementDao extends SqlSessionDaoSupport {


    /**
     * 创建日结算汇总记录
     * 创建前需要先检查 指定确认日期下该商户记录是否存在
     *
     * @param sellerSettlement   日结算汇总记录
     * @return  新纪录id
     */
    public Long create(SellerSettlement sellerSettlement) {
        checkNotNull(sellerSettlement.getConfirmedAt());
        checkNotNull(sellerSettlement.getSellerId());

        SellerSettlement criteria = new SellerSettlement();
        criteria.setSellerId(sellerSettlement.getSellerId());


        Map<String, Object> params = Maps.newHashMapWithExpectedSize(3);
        params.put("confirmedStartAt", startOfDay(sellerSettlement.getConfirmedAt()));
        params.put("confirmedEndAt", endOfDay(sellerSettlement.getConfirmedAt()));
        params.put("criteria", criteria);
        Long count = getSqlSession().selectOne("SellerSettlement.countOf", params);


        boolean exist = count > 0L;
        checkState(!exist, "seller.settlement.record.duplicate");
        getSqlSession().insert("SellerSettlement.create", sellerSettlement);
        return sellerSettlement.getId();
    }


    /**
     * 根据起止日期分页查询日结算汇总信息
     *
     * @param sellerId              商户id
     * @param settleStatus          结算状态
     * @param confirmedStartAt      确认起始日期
     * @param confirmedEndAt        确认截止日期
     * @param offset                偏移值
     * @param limit                 记录数
     * @return  分页查询结果
     */
    public Paging<SellerSettlement> findBy(Long sellerId, Integer settleStatus,
                                           Date confirmedStartAt, Date confirmedEndAt, Integer offset, Integer limit){
        Map<String,Object> params = Maps.newHashMapWithExpectedSize(6);
        SellerSettlement criteria = new SellerSettlement();
        criteria.setSellerId(sellerId);
        criteria.setSettleStatus(settleStatus);
        params.put("criteria", criteria);
        params.put("confirmedStartAt", confirmedStartAt);
        params.put("confirmedEndAt", confirmedEndAt);
        params.put("offset", offset);
        params.put("limit", limit);
        return findBy(params);
    }


    /**
     * 分页查询符合条件的记录
     *
     * @param criteria  标准查询单元
     * @param offset    起始偏移，不能为空
     * @param limit     返回条数，不能为空
     * @return  查询结果
     */
    public Paging<SellerSettlement> findBy(SellerSettlement criteria, Integer offset, Integer limit) {
        Map<String, Object> params = Maps.newHashMapWithExpectedSize(20);
        params.put("criteria", criteria);
        params.put("offset", offset);
        params.put("limit", limit);

        if (criteria.getConfirmedAt() != null) {
            Date confirmedAt = criteria.getConfirmedAt();
            params.put("confirmedStartAt", startOfDay(confirmedAt));
            params.put("confirmedEndAt", endOfDay(confirmedAt));
        }

        if (criteria.getSyncedAt() != null) {
            Date syncedAt = criteria.getSyncedAt();
            params.put("syncedStartAt", startOfDay(syncedAt));
            params.put("syncedEndAt", endOfDay(syncedAt));
        }

        if (criteria.getVouchedAt() != null) {
            Date vouchedAt = criteria.getVouchedAt();
            params.put("vouchedStartAt", startOfDay(vouchedAt));
            params.put("vouchedEndAt", endOfDay(vouchedAt));
        }

        if (criteria.getCreatedAt() != null) {
            Date createdAt = criteria.getCreatedAt();
            params.put("createdStartAt", startOfDay(createdAt));
            params.put("createdEndAt", endOfDay(createdAt));
        }

        return findBy(params);
    }



    /**
     * 分页查询符合条件的记录
     *
     * @param params  参数
     * @return  查询结果
     */
    public Paging<SellerSettlement> findBy(Map<String, Object> params) {
        Long total = getSqlSession().selectOne("SellerSettlement.countOf", params);
        if (total == 0L) {
            return new Paging<SellerSettlement>(0L, Collections.<SellerSettlement>emptyList());
        }
        List<SellerSettlement> sellerSettlements = getSqlSession().selectList("SellerSettlement.findBy", params);
        return new Paging<SellerSettlement>(total, sellerSettlements);
    }


    private Date startOfDay(Date date) {
        if (date == null) { return null; }
        return new DateTime(date).withTimeAtStartOfDay().toDate();
    }

    private Date endOfDay(Date date) {
        if (date == null) { return null; }
        return new DateTime(date).withTimeAtStartOfDay().plusDays(1).toDate();
    }

    /**
     * 根据标识获取日结信息
     *
     * @param id    标识
     * @return      日结算信息
     */
    public SellerSettlement get(Long id) {
        return getSqlSession().selectOne("SellerSettlement.get", id);

    }


    /**
     * 获取尚未同步的商户日汇总信息
     *
     * @param offset    偏移值
     * @param limit     记录数
     * @return  分页查询结果
     */
    public Paging<SellerSettlement> findUnVouched(int offset, int limit) {
        SellerSettlement criteria = new SellerSettlement();
        criteria.setVouched(SellerSettlement.Vouched.NOT.value());
        Map<String, Object> params = Maps.newHashMapWithExpectedSize(3);
        params.put("criteria", criteria);
        params.put("offset", offset);
        params.put("limit", limit);
        return findBy(params);

    }

    /**
     * 更新
     * @param settlement 商户日结算记录
     * @return 是否更新成功
     */
    public boolean update(SellerSettlement settlement) {
        return getSqlSession().update("SellerSettlement.update", settlement) == 1;
    }

    /**
     * 标记JDE同步完成
     *
     * @param id    商户日汇总标识
     * @return 操作是否成功
     */
    public boolean synced(Long id) {
        return getSqlSession().update("SellerSettlement.synced", ImmutableMap.of("id", id)) > 0;
    }

    /**
     * 标记JDE 同步失败
     *
     * @param id   商户日汇总标识
     * @return 操作是否成功
     */
    public boolean syncedFail(Long id) {
        return getSqlSession().update("SellerSettlement.syncedFail", ImmutableMap.of("id", id)) > 0;
    }


    /**
     * 查询尚未打印凭证的数据
     */
    public Paging<SellerSettlement> findSynced(int offset, int limit) {
        SellerSettlement criteria = new SellerSettlement();
        criteria.setSynced(SellerSettlement.Synced.DONE.value());
        Map<String, Object>  params = Maps.newHashMapWithExpectedSize(5);
        params.put("criteria", criteria);
        params.put("offset", offset);
        params.put("limit", limit);

        return findBy(params);
    }

    /**
     * 更新商户汇总的凭证及发票号
     *
     * @param sellerSettlement  已开票的商户汇总信息
     */
    public boolean vouching(SellerSettlement sellerSettlement) {
        SellerSettlement updating = new SellerSettlement();
        updating.setId(sellerSettlement.getId());
        updating.setVoucher(sellerSettlement.getVoucher());
        updating.setVouchedAt(sellerSettlement.getVouchedAt());
        return getSqlSession().update("SellerSettlement.update", sellerSettlement) == 1;
    }

    /**
     *
     * 更新商户发票号
     *
     * @param sellerSettlement  已开票的商户汇总信息
     */
    public boolean receipting(SellerSettlement sellerSettlement) {
        SellerSettlement updating = new SellerSettlement();
        updating.setId(sellerSettlement.getId());
        updating.setThirdPartyReceipt(sellerSettlement.getThirdPartyReceipt());
        updating.setThirdPartyReceiptAt(sellerSettlement.getThirdPartyReceiptAt());
        return getSqlSession().update("SellerSettlement.update", updating) == 1;
    }

    /**
     * 更新记录打印状态为"已打印"
     *
     * @param sellerSettlement  商户汇总记录
     * @return  是否操作成功
     */
    public boolean printing(SellerSettlement sellerSettlement) {
        checkArgument(notNull(sellerSettlement.getId()), "id.can.not.be.empty");

        SellerSettlement updating = new SellerSettlement();
        updating.setId(sellerSettlement.getId());
        updating.setPrinted(SellerSettlement.Printed.DONE.value());
        updating.setPrintedAt(DateTime.now().toDate());
        return getSqlSession().update("SellerSettlement.update", updating) == 1;

    }


    /**
     * 批量根据用户更新商户88码
     *
     * @param outerCode 商户88码
     * @param sellerId  商家id
     * @return  更新数量
     */
    public Integer batchUpdateOuterCode(String outerCode, Long sellerId) {
        return getSqlSession().update("SellerSettlement.batchUpdateOuterCode",
                ImmutableMap.of("outerCode", outerCode, "sellerId", sellerId));
    }


}
