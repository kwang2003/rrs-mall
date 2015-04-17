package com.aixforce.item.dao.mysql;

import com.aixforce.common.model.Paging;
import com.aixforce.item.model.Brand;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import org.mybatis.spring.support.SqlSessionDaoSupport;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Created by yangzefeng on 14-1-15
 */
@Repository
public class BrandDao extends SqlSessionDaoSupport {

    public void create(Brand brand) {
        getSqlSession().insert("Brand.create", brand);
    }

    public void update(Brand brand) {
        getSqlSession().update("Brand.update", brand);
    }

    public void delete(Long id) {
        getSqlSession().delete("Brand.delete", id);
    }

    public List<Brand> findAll() {
        return getSqlSession().selectList("Brand.findAll");
    }
    
    public List<Map<String, Object>> findItems(Map<String, Object> map) {
        return getSqlSession().selectList("Brand.findItems",map);
    }
    
    public Integer findCount(Map<String, Object> map) {
        return getSqlSession().selectOne("Brand.findCount",map);
    }

    public Paging<Brand> paging(Integer offset, Integer limit) {
        Long total = getSqlSession().selectOne("Brand.count");
        if(total == 0) {
            return new Paging<Brand>(0L, Collections.<Brand>emptyList());
        }
        List<Brand> data = getSqlSession().selectList("Brand.pagination",
                ImmutableMap.of("offset", offset, "limit", limit));
        return new Paging<Brand>(total, data);
    }

    public List<Brand> findByIds(List<Long> ids) {
        return getSqlSession().selectList("Brand.findByIds", ids);
    }

    public Brand findById(Long id) {
        return getSqlSession().selectOne("Brand.findById", id);
    }

    public Brand findByName(String name) {
        return getSqlSession().selectOne("Brand.findByName", name);
    }

    public Paging<Brand> pagingByName(String name, Integer offset, Integer limit) {
        Map<String, Object> n = Maps.newHashMap();
        n.put("name", name);
        Long total = getSqlSession().selectOne("Brand.countLikeName", n);
        if (total==0) {
            return new Paging<Brand>(0L, Collections.<Brand>emptyList());
        }
        Map<String, Object> params = Maps.newHashMap();
        params.put("name", name);
        params.put("offset", offset);
        params.put("limit", limit);
        List<Brand> data = getSqlSession().selectList("Brand.pagingByName", params);
        return new Paging<Brand>(total, data);
    }
}
