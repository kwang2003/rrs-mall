package com.aixforce.shop.dao;

import com.aixforce.common.model.Paging;
import com.aixforce.shop.model.Shop;
import com.google.common.base.Objects;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Maps;
import org.mybatis.spring.support.SqlSessionDaoSupport;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Author:  <a href="mailto:jlchen.cn@gmail.com">jlchen</a>
 * Date: 2013-10-28
 */
@Repository
public class ShopDao extends SqlSessionDaoSupport {
    public Shop findByUserId(Long userId) {
        return getSqlSession().selectOne("Shop.findByUserId", userId);
    }

    public Long create(Shop shop) {
        getSqlSession().insert("Shop.create", shop);
        return shop.getId();
    }

    public boolean update(Shop shop) {
        return getSqlSession().update("Shop.update", shop) == 1;
    }

    public boolean updateStatus(Long shopId, Shop.Status status) {
        return getSqlSession().update("Shop.updateStatusById",
                ImmutableMap.of("id", shopId, "status", status.value())) == 1;
    }

    public void batchUpdateStatus(List<Long> ids, Integer status) {
        getSqlSession().update("Shop.batchUpdateStatus", ImmutableMap.of("ids", ids, "status", status));
    }

    public boolean delete(Long id) {
        return getSqlSession().delete("Shop.delete", id) == 1;
    }

    public Shop findById(Long id) {
        return getSqlSession().selectOne("Shop.findById", id);
    }

    public List<Shop> findByIds(List<Long> ids) {
        return getSqlSession().selectList("Shop.findByIds", ids);
    }

    public Shop findByName(String name) {
        return getSqlSession().selectOne("Shop.findByName", name);
    }

    public List<Shop> forDump(Long lastId, int limit) {
        return getSqlSession().selectList("Shop.forDump", ImmutableMap.of("lastId", lastId, "limit", limit));
    }

    public List<Shop> forDeltaDump(Long lastId, String compared, int limit) {
        return getSqlSession().selectList("Shop.forDeltaDump", ImmutableMap.of("lastId", lastId, "limit", limit, "compared", compared));
    }

    public Long maxId() {
        return Objects.firstNonNull((Long) getSqlSession().selectOne("Shop.maxId"), 0L);
    }

    public Paging<Shop> shops(Integer offset, Integer size, String name, String businessId, String userName) {
        Map<String,Object> params = Maps.newHashMap();
        if(!Strings.isNullOrEmpty(name)) {
            params.put("name", name);
        }
        if(!Strings.isNullOrEmpty(userName)) {
            params.put("userName", userName);
        }
        if(!Strings.isNullOrEmpty(businessId)) {
            params.put("businessId", businessId);
        }
        Long total = getSqlSession().selectOne("Shop.countOf", params);
        if (total > 0L) {
            params.put("offset", offset);
            params.put("limit", size);
            List<Shop> shops = getSqlSession().selectList("Shop.pagination", params);
            return new Paging<Shop>(total, shops);
        }
        return new Paging<Shop>(0L, Collections.<Shop>emptyList());
    }

    public Paging<Shop> brandShops(Integer offset, Integer size,List<Integer> ids) {
        Map<String,Object> params = Maps.newHashMap();
        if(ids!=null&&ids.size()>0) {
            params.put("ids", ids);
        }
        Long total = getSqlSession().selectOne("Shop.countOf", params);
        if (total > 0L) {
            params.put("offset", offset);
            params.put("limit", size);
            List<Shop> shops = getSqlSession().selectList("Shop.pagination", params);
            return new Paging<Shop>(total, shops);
        }
        return new Paging<Shop>(0L, Collections.<Shop>emptyList());
    }

    public Paging<Shop> findByStatus(Integer offset, Integer size, Integer status) {
        Long total = getSqlSession().selectOne("Shop.countOf", ImmutableMap.of("status", status));
        if (total > 0L) {
            List<Shop> shops = getSqlSession().selectList("Shop.pagination",
                    ImmutableMap.of("offset", offset, "limit", size, "status", status));
            return new Paging<Shop>(total, shops);
        }
        return new Paging<Shop>(0L, Collections.<Shop>emptyList());
    }

    public Paging<Shop> findBy(Map<String, Object> params) {
        Long total = getSqlSession().selectOne("Shop.countBy", params);
        if (total == 0L) {
            return new Paging<Shop>(0L, Collections.<Shop>emptyList());
        }
        List<Shop> shops = getSqlSession().selectList("Shop.findBy", params);
        return new Paging<Shop>(total, shops);
    }

    public List<Shop> findByTaxRegisterNo(String taxNo) {
        return getSqlSession().selectList("Shop.findByTaxRegisterNo", taxNo);
    }

    /**
     * 查询存在税务登记号的店铺
     *
     * @return  店铺分页信息
     */
    public Paging<Shop> findWithTaxNo(Integer offset, Integer limit) {
        Long total = getSqlSession().selectOne("Shop.countOfWithTaxNo");
        if (total == 0L) {
            return new Paging<Shop>(0L, Collections.<Shop>emptyList());
        }
        List<Shop> shops = getSqlSession().selectList("Shop.findWithTaxNo",
                ImmutableMultimap.of("offset", offset, "limit", limit));
        return new Paging<Shop>(total, shops);

    }
}
