package com.aixforce.rrs.settle.dao;

import com.aixforce.common.model.Paging;
import com.aixforce.rrs.settle.model.DepositFee;
import com.aixforce.rrs.settle.model.DepositFeeCash;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.mybatis.spring.support.SqlSessionDaoSupport;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static com.aixforce.common.utils.Arguments.notNull;
import static com.aixforce.rrs.settle.model.DepositFee.ofDeposit;
import static com.aixforce.rrs.settle.model.DepositFee.ofTech;
import static com.google.common.base.Preconditions.checkArgument;

/**
 * Author:  <a href="mailto:jlchen.cn@gmail.com">jlchen</a>
 * Date: 2014-01-18
 */
@Repository
public class DepositFeeDao extends SqlSessionDaoSupport {

    /**
     * 创建缴费记录
     * @param depositFee   缴费记录
     * @return  新纪录id
     */
    public Long create(DepositFee depositFee) {
        getSqlSession().insert("DepositFee.create", depositFee);
        return depositFee.getId();
    }

    /**
     * 分页查询保证金明细
     * @param criteria  标准查询单元
     * @param types     类型列表
     * @param offset    起始位置
     * @param size      每页显示条数
     * @return  分页查询结果
     */
    public Paging<DepositFee> findBy(DepositFee criteria, List<Integer> types, Integer offset, Integer size){
        Map<String,Object> params = Maps.newHashMapWithExpectedSize(3);
        params.put("criteria",criteria);
        params.put("types", types);
        params.put("offset", offset);
        params.put("limit", size);
        return findBy(params);
    }

    /**
     * 分页查询保证金明细
     * @param params  查询参数
     * @return 分页查询结果
     */
    public Paging<DepositFee> findBy(Map<String, Object> params) {
        Long count = getSqlSession().selectOne("DepositFee.countOf", params);
        if(count == 0L){
            return new Paging<DepositFee>(0L, Collections.<DepositFee>emptyList());
        }

        List<DepositFee> depositFees = getSqlSession().selectList("DepositFee.findBy", params);
        return new Paging<DepositFee>(count, depositFees);
    }


    /**
     * 分页查询保证金明细
     *
     * @param criteria  查询单元
     * @param offset    起始位置
     * @param limit     每页显示条数
     * @return  分页查询结果
     */
    public Paging<DepositFee> findDepositFeeBy(DepositFee criteria, Integer offset, Integer limit) {
         checkArgument(ofDeposit(criteria), "deposit.type.incorrect");
        return findBy(criteria, DepositFee.Type.deposits, offset, limit);
    }


    /**
     * 分页查询技术服务费明细
     *
     * @param criteria  查询单元
     * @param offset    起始位置
     * @param limit     每页显示条数
     * @return  分页查询结果
     */
    public Paging<DepositFee> findTechFeeBy(DepositFee criteria, Integer offset, Integer limit) {
        checkArgument(ofTech(criteria), "deposit.type.incorrect");
        return findBy(criteria, DepositFee.Type.techs, offset, limit);
    }

    public DepositFee get(Long id) {
        Map<String, Object> params = Maps.newHashMapWithExpectedSize(1);
        params.put("id", id);
        return getSqlSession().selectOne("DepositFee.get", params);
    }

    /**
     *
     * @param fee  费用信息
     * @return    费用
     */
    public Boolean update(DepositFee fee) {
        return (long)getSqlSession().update("DepositFee.update", fee) == 1;
    }


    /**
     * 标记同步完成
     *
     * @param id    标识
     * @return  操作是否成功
     */
    public boolean synced(Long id) {
        return getSqlSession().update("DepositFee.synced", ImmutableMap.of("id", id)) == 1;
    }

    /**
     * 标记同步失败
     * @param id    标识
     * @return  操作是否成功
     */
    public boolean syncedFail(Long id) {
        return getSqlSession().update("DepositFee.syncedFail", ImmutableMap.of("id", id)) == 1;
    }


    /**
     * 获取尚未同步JDE的保证金数据
     *
     * @param offset        偏移值
     * @param limit         每页显示条数
     * @return  分页信息
     */
    public Paging<DepositFee> findUnVouchedDeposits(int offset, int limit) {
        DepositFee criteria = new DepositFee();
        criteria.setVouched(DepositFee.Vouched.NOT.value());
        Map<String, Object> params = Maps.newHashMapWithExpectedSize(4);
        params.put("types", Lists.newArrayList(1, 2, 4));
        params.put("criteria", criteria);
        params.put("limit", limit);
        params.put("offset", offset);
        return findBy(params);
    }

    /**
     * 获取尚未同步至JDE的保证金数据
     *
     * @param offset    偏移值
     * @param limit     每页显示条数
     *
     * @return  分页信息
     */
    public Paging<DepositFee> findUnVouchedTechs(int offset, int limit) {
        DepositFee criteria = new DepositFee();
        criteria.setVouched(DepositFee.Vouched.NOT.value());
        Map<String, Object> params = Maps.newHashMapWithExpectedSize(4);
        params.put("types", Lists.newArrayList(3));
        params.put("criteria", criteria);
        params.put("limit", limit);
        params.put("offset", offset);
        return findBy(params);
    }


    /**
     * 查询未更新票据的记录
     */
    public Paging<DepositFee> findSynced(Integer offset, Integer limit) {
        DepositFee criteria = new DepositFee();
        criteria.setSynced(DepositFeeCash.Synced.DONE.value());
        Map<String, Object>  params = Maps.newHashMapWithExpectedSize(5);
        params.put("criteria", criteria);
        params.put("offset", offset);
        params.put("limit", limit);
        return findBy(params);
    }


    /**
     * 更新凭证号
     *
     * @param depositFee  基础金
     * @return  操作是否成功
     */
    public boolean vouching(DepositFee depositFee) {
        checkArgument(notNull(depositFee.getId()), "deposit.fee.id.empty");
        checkArgument(notNull(depositFee.getId()), "deposit.fee.voucher.empty");

        DepositFee updating = new DepositFee();
        updating.setId(depositFee.getId());
        updating.setVoucher(depositFee.getVoucher());
        updating.setVouchedAt(depositFee.getVouchedAt());
        updating.setVouched(DepositFee.Vouched.DONE.value());
        return getSqlSession().update("DepositFee.update", updating) == 1;

    }

    /**
     * 更新发票号
     *
     * @param depositFee  基础金
     * @return   操作是否成功
     */
    public boolean receipting(DepositFee depositFee) {
        checkArgument(notNull(depositFee.getId()), "deposit.fee.id.empty");
        checkArgument(notNull(depositFee.getReceipt()), "deposit.fee.receipt.null");

        DepositFee updating = new DepositFee();
        updating.setId(depositFee.getId());
        updating.setReceipt(depositFee.getReceipt());
        updating.setReceiptedAt(depositFee.getReceiptedAt());
        updating.setReceipted(DepositFee.Receipted.DONE.value());
        return getSqlSession().update("DepositFee.update", updating) == 1;
    }


    /**
     * 汇总指定卖家的技术服务费
     *
     * @param sellerId  卖家id
     * @return 汇总的技术服务费
     */
    public Long summaryTechFeeOfSeller(Long sellerId) {
        Long result = getSqlSession().selectOne("DepositFee.summaryTechFeeOf", sellerId);
        return result == null ? 0L : result;
    }

    /**
     * 批量根据用户更新商户88码
     *
     * @param outerCode 商户88码
     * @param sellerId  商家id
     * @return  更新数量
     */
    public Integer batchUpdateOuterCode(String outerCode, Long sellerId) {
        return getSqlSession().update("DepositFee.batchUpdateOuterCode",
                ImmutableMap.of("outerCode", outerCode, "sellerId", sellerId));
    }
}
