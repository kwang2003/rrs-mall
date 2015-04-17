package com.aixforce.rrs.settle.dao;

import com.aixforce.common.model.Paging;
import com.aixforce.rrs.settle.model.DepositFeeCash;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import org.joda.time.DateTime;
import org.mybatis.spring.support.SqlSessionDaoSupport;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static com.aixforce.common.utils.Arguments.notNull;
import static com.google.common.base.Preconditions.checkState;

/**
 * Mail: xiao@terminus.io <br>
 * Date: 2014-05-20 12:01 PM  <br>
 * Author: xiao
 */
@Repository
public class DepositFeeCashDao extends SqlSessionDaoSupport {

    public Long create(DepositFeeCash depositFeeCash) {
        getSqlSession().insert("DepositFeeCash.create", depositFeeCash);
        return depositFeeCash.getId();
    }

    /**
     * 标记基础金提现明细为“已提现”
     *
     * @param depositFeeCash    基础金提现
     */
    public Boolean cashing(DepositFeeCash depositFeeCash) {
        checkState(notNull(depositFeeCash.getId()), "deposit.fee.id.empty");
        DepositFeeCash updating = new DepositFeeCash();
        updating.setId(depositFeeCash.getId());
        updating.setStatus(DepositFeeCash.Status.DONE.value());
        return update(updating);
    }

    /**
     * 获取基础金提现对象
     */
    public DepositFeeCash get(Long id) {
        return getSqlSession().selectOne("DepositFeeCash.get", id);
    }

    /**
     * 分页查询基础金提现
     */
    public Paging<DepositFeeCash> findBy(Map<String, Object> params) {
        Long count = getSqlSession().selectOne("DepositFeeCash.countOf", params);
        if(count == 0L){
            return new Paging<DepositFeeCash>(0L, Collections.<DepositFeeCash>emptyList());
        }
        List<DepositFeeCash> r = getSqlSession().selectList("DepositFeeCash.findBy",params);
        return new Paging<DepositFeeCash>(count, r);
    }

    public Paging<DepositFeeCash> findUnVouched(int offset, Integer limit) {
        DepositFeeCash criteria = new DepositFeeCash();
        criteria.setVouched(DepositFeeCash.Vouched.NOT.value());
        Map<String, Object> params = Maps.newHashMapWithExpectedSize(3);
        params.put("criteria", criteria);
        params.put("offset", offset);
        params.put("limit", limit);
        return findBy(params);
    }

    public Boolean synced(Long id) {
        DepositFeeCash updating = new DepositFeeCash();
        updating.setId(id);
        updating.setSynced(DepositFeeCash.Synced.DONE.value());
        updating.setSyncedAt(DateTime.now().toDate());
        return getSqlSession().update("DepositFeeCash.update", updating) == 1;
    }

    public Paging<DepositFeeCash> findSynced(int offset, Integer size) {
        DepositFeeCash criteria = new DepositFeeCash();
        criteria.setSynced(DepositFeeCash.Synced.DONE.value());
        Map<String, Object>  params = Maps.newHashMapWithExpectedSize(5);
        params.put("criteria", criteria);
        params.put("offset", offset);
        params.put("limit", size);
        return findBy(params);
    }

    public boolean vouching(DepositFeeCash depositFeeCash) {
        DepositFeeCash updating = new DepositFeeCash();
        updating.setId(depositFeeCash.getId());
        updating.setVouched(DepositFeeCash.Vouched.DONE.value());
        updating.setVoucher(depositFeeCash.getVoucher());
        updating.setVouchedAt(depositFeeCash.getVouchedAt());

        return getSqlSession().update("DepositFeeCash.update", updating) == 1;
    }


    public boolean update(DepositFeeCash updating) {
        return getSqlSession().update("DepositFeeCash.update", updating) == 1;
    }

    public DepositFeeCash getByDepositId(Long id) {
        return getSqlSession().selectOne("DepositFeeCash.getByDepositId", id);
    }

    /**
     * 批量根据用户更新商户88码
     *
     * @param outerCode 商户88码
     * @param sellerId  商家id
     * @return  更新数量
     */
    public Integer batchUpdateOuterCode(String outerCode, Long sellerId) {
        return getSqlSession().update("DepositFeeCash.batchUpdateOuterCode",
                ImmutableMap.of("outerCode", outerCode, "sellerId", sellerId));
    }
}
