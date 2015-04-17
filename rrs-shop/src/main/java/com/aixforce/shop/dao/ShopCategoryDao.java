package com.aixforce.shop.dao;

import com.aixforce.shop.model.ShopCategory;
import com.google.common.collect.ImmutableMap;
import org.mybatis.spring.support.SqlSessionDaoSupport;
import org.springframework.stereotype.Repository;

import javax.annotation.Nonnull;
import java.util.List;

/**
 * Author:  <a href="mailto:jlchen.cn@gmail.com">jlchen</a>
 * Date: 2013-10-29
 */
@Repository
public class ShopCategoryDao extends SqlSessionDaoSupport {

    public List<ShopCategory> findByParentId(@Nonnull Long parentId) {
        return getSqlSession().selectList("ShopCategory.findByParentId", parentId);
    }

    public ShopCategory findById(@Nonnull Long id) {
        return getSqlSession().selectOne("ShopCategory.findById", id);
    }

    public Long create(ShopCategory category) {
        getSqlSession().insert("ShopCategory.create", category);
        return category.getId();
    }

    public boolean updateName(Long id, String name) {
        return getSqlSession().update("ShopCategory.updateName", ImmutableMap.of("id", id, "name", name)) == 1;
    }

    public boolean delete(Long id) {
        return getSqlSession().delete("ShopCategory.delete", id) == 1;
    }
}
