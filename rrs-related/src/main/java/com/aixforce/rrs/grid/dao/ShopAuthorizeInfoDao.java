package com.aixforce.rrs.grid.dao;

import com.aixforce.rrs.grid.model.ShopAuthorizeInfo;
import org.mybatis.spring.support.SqlSessionDaoSupport;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Created by yangzefeng on 14-1-16
 */
@Repository
public class ShopAuthorizeInfoDao extends SqlSessionDaoSupport{

    public void create(ShopAuthorizeInfo shopAuthorizeInfo) {
        getSqlSession().insert("ShopAuthorizeInfo.create", shopAuthorizeInfo);
    }

    public void update(ShopAuthorizeInfo shopAuthorizeInfo) {
        getSqlSession().update("ShopAuthorizeInfo.update", shopAuthorizeInfo);
    }

    public void delete(Long id) {
        getSqlSession().delete("ShopAuthorizeInfo.delete", id);
    }

    public ShopAuthorizeInfo findById(Long id) {
        return getSqlSession().selectOne("ShopAuthorizeInfo.findById", id);
    }

    public List<ShopAuthorizeInfo> findByShopId(Long shopId) {
        return getSqlSession().selectList("ShopAuthorizeInfo.findByShopId", shopId);
    }

    public List<ShopAuthorizeInfo> findByShopIds(List<Long> shopIds) {
        return getSqlSession().selectList("ShopAuthorizeInfo.findByShopIds", shopIds);
    }
}
