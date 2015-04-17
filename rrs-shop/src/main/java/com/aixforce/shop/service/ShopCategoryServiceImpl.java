package com.aixforce.shop.service;

import com.aixforce.common.model.Response;
import com.aixforce.shop.dao.ShopCategoryDao;
import com.aixforce.shop.model.ShopCategory;
import com.google.common.base.Objects;
import com.google.common.base.Throwables;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;
import java.util.List;

/**
 * Author:  <a href="mailto:jlchen.cn@gmail.com">jlchen</a>
 * Date: 2013-10-29
 */
@Service
@Slf4j
public class ShopCategoryServiceImpl implements ShopCategoryService {

    @Autowired
    private ShopCategoryDao shopCategoryDao;

    /**
     * 创建类目
     *
     * @param shopCategory shop category
     * @return 设置了id的类目
     */
    @Override
    public Response<ShopCategory> create(@Nonnull ShopCategory shopCategory) {
        Response<ShopCategory> result = new Response<ShopCategory>();
        try {
            shopCategory.setParentId(Objects.firstNonNull(shopCategory.getParentId(), 0L));
            shopCategoryDao.create(shopCategory);
            result.setResult(shopCategory);
            return result;
        } catch (Exception e) {
            log.error("failed to create {},cause:{}", shopCategory, Throwables.getStackTraceAsString(e));
            result.setError("shopCategory.create.fail");
            return result;
        }
    }

    /**
     * 获取某个父类目下面所有的子类目
     *
     * @param parentId parent id, 一级类目的parentId=0
     * @return 子类目列表
     */
    @Override
    public Response<List<ShopCategory>> findByParentId(@Nonnull Long parentId) {
        Response<List<ShopCategory>> result = new Response<List<ShopCategory>>();
        try {
            List<ShopCategory> categories = shopCategoryDao.findByParentId(parentId);
            result.setResult(categories);
            return result;
        } catch (Exception e) {
            log.error("failed to query shop categories where parentId={},cause:{}", parentId, Throwables.getStackTraceAsString(e));
            result.setError("shopCategory.query.fail");
            return result;
        }
    }

    /**
     * 修改店铺类目名称
     *
     * @param id   店铺类目id
     * @param name 店铺类目名称
     * @return 修改是否成功
     */
    @Override
    public Response<Boolean> updateName(@Nonnull Long id, @Nonnull String name) {
        Response<Boolean> result = new Response<Boolean>();
        try {
            boolean success = shopCategoryDao.updateName(id, name);
            result.setResult(success);
            return result;
        } catch (Exception e) {
            log.error("failed to update name to {} where shopCategoryId={},cause:{}", name, id, Throwables.getStackTraceAsString(e));
            result.setError("shopCategory.update.fail");
            return result;
        }
    }

    /**
     * 删除店铺类目名称
     *
     * @param id 店铺类目id
     * @return 修改是否成功
     */
    @Override
    public Response<Boolean> delete(@Nonnull Long id) {
        Response<Boolean> result = new Response<Boolean>();
        try {
            boolean success = shopCategoryDao.delete(id);
            result.setResult(success);
            return result;
        } catch (Exception e) {
            log.error("failed to delete shopCategory where id={},cause:{}");
            result.setError("shopCategory.delete.fail");
            return result;
        }
    }
}
