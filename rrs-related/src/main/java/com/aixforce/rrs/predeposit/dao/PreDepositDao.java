package com.aixforce.rrs.predeposit.dao;


import com.aixforce.common.model.Paging;
import com.aixforce.rrs.predeposit.model.PreDeposit;
import com.aixforce.rrs.presale.model.PreSale;
import com.google.common.base.Objects;
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
 * Created by yangzefeng on 14-2-12
 */
@Repository
public class PreDepositDao extends SqlSessionDaoSupport{

    // mapper 的命名空间
    private final static String namespace = "PreDeposit.";



    public PreDeposit get(Long id) {
        return getSqlSession().selectOne(namespace + "get", id);
    }

    public PreDeposit getBySpuId(Long spuId) {
        return getSqlSession().selectOne(namespace + "getBySpuId", spuId);
    }

    public List<PreDeposit> findByIds(List<Long> ids) {
        return getSqlSession().selectList(namespace + "findByIds", ids);
    }

    public List<PreDeposit> findByCriterion(PreDeposit criterion, int offset, int limit) {
        return getSqlSession().selectList(namespace + "findByCriterion", ImmutableMap.of("criterion", criterion,
                "offset", offset, "limit", limit));
    }


    public Paging<PreDeposit> findBy(PreDeposit criteria, int offset, int limit) {
        Map<String, Object> params = Maps.newHashMap();
        params.put("criteria", criteria);
        params.put("offset", offset);
        params.put("limit", limit);

        if (criteria.getCreatedAt() != null) {
            Date createdAt = criteria.getCreatedAt();
            params.put("createdStartAt", startOfDay(createdAt));
            params.put("createdEndAt", endOfDay(createdAt));
        }

        return findBy(params);
    }

    public Paging<PreDeposit> findBy(Map<String, Object> params) {
        Long total = getSqlSession().selectOne(namespace + "countOf", params);
        if (total == 0L) {
            return new Paging<PreDeposit>(0L, Collections.<PreDeposit>emptyList());
        }
        List<PreDeposit> preSales = getSqlSession().selectList(namespace + "findBy", params);
        return new Paging<PreDeposit>(total, preSales);
    }


    public Long countBy(PreDeposit criterion) {
        return getSqlSession().selectOne(namespace + "countBy", criterion);
    }

    public void create(PreDeposit preDeposit) {
        preDeposit.setFakeSoldQuantity(Objects.firstNonNull(preDeposit.getFakeSoldQuantity(), 0));
        getSqlSession().insert(namespace + "create", preDeposit);
    }

    public boolean update(PreDeposit preDeposit) {
        return getSqlSession().update(namespace + "update", preDeposit) == 1;
    }

    public PreDeposit findByItemId(Long itemId){
        return getSqlSession().selectOne(namespace + "findByItemId", itemId);
    }

    /**
     *  终止预售
     */
    public boolean stop(Long id) {
        PreDeposit updating = new PreDeposit();
        updating.setId(id);
        updating.setStatus(PreSale.Status.STOPPED.value());
        return update(updating);
    }
}
