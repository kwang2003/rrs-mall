package com.aixforce.shop.dao;

import com.aixforce.common.model.Paging;
import com.aixforce.shop.model.ShopExtra;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMultimap;
import org.mybatis.spring.support.SqlSessionDaoSupport;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static com.aixforce.common.utils.Arguments.notNull;
import static com.google.common.base.Preconditions.checkArgument;

/**
 * Created by IntelliJ IDEA.
 * User: AnsonChan
 * Date: 14-1-23
 */
@Repository
public class ShopExtraDao extends SqlSessionDaoSupport {
    public ShopExtra findById(Long id) {
        return getSqlSession().selectOne("ShopExtra.findById", id);
    }

    public ShopExtra findByShopId(Long shopId) {
        return getSqlSession().selectOne("ShopExtra.findByShopId", shopId);
    }

    public List<ShopExtra> findByOuterCode(String outerCode) {
        return getSqlSession().selectList("ShopExtra.findByOuterCode", outerCode);
    }

    public Long create(ShopExtra extra) {
        checkArgument(notNull(extra.getShopId()), "shop.id.can.not.be.empty");
        ShopExtra exist = findByShopId(extra.getShopId());

        if (notNull(exist)) {    // 若已经存在则不创建
            return exist.getId();
        }

        getSqlSession().insert("ShopExtra.create", extra);
        return extra.getId();
    }

    public boolean update(ShopExtra extra) {
        return getSqlSession().update("ShopExtra.update", extra) == 1;
    }

    public boolean updateByShopId(ShopExtra extra) {
        return getSqlSession().update("ShopExtra.updateByShopId", extra) == 1;
    }

    public Paging<ShopExtra> findBy(Map<String, Object> params, Integer offset, Integer limit) {
        params.put("offset", offset);
        params.put("limit", limit);
        return findBy(params);
    }

    public Paging<ShopExtra> findBy(Map<String, Object> params) {
        Long total = getSqlSession().selectOne("ShopExtra.countOf", params);
        if (total == 0L) {
            return new Paging<ShopExtra>(0L, Collections.<ShopExtra>emptyList());
        }
        List<ShopExtra> shopExtras = getSqlSession().selectList("ShopExtra.findBy", params);
        return new Paging<ShopExtra>(total, shopExtras);
    }

    /**
     * 更新到店支付
     * @param extra
     * @return
     */
    public Boolean updateIsStorePayByShopId(ShopExtra extra) {
        return getSqlSession().update("ShopExtra.updateIsStorePayByShopid", extra) == 1;
    }
}
