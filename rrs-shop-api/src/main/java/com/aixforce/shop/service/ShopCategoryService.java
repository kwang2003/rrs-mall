package com.aixforce.shop.service;

import com.aixforce.common.model.Response;
import com.aixforce.shop.model.ShopCategory;

import javax.annotation.Nonnull;
import java.util.List;

/**
 * 店铺所属类目服务
 * Author:  <a href="mailto:jlchen.cn@gmail.com">jlchen</a>
 * Date: 2013-10-29
 */
public interface ShopCategoryService {

    /**
     * 创建类目
     *
     * @param shopCategory shop category
     * @return 设置了id的类目
     */
    Response<ShopCategory> create(@Nonnull ShopCategory shopCategory);

    /**
     * 获取某个父类目下面所有的子类目
     *
     * @param parentId parent id, 一级类目的parentId=0
     * @return 子类目列表
     */
    Response<List<ShopCategory>> findByParentId(@Nonnull Long parentId);


    /**
     * 修改店铺类目名称
     *
     * @param id   店铺类目id
     * @param name 店铺类目名称
     * @return 修改是否成功
     */
    Response<Boolean> updateName(@Nonnull Long id, @Nonnull String name);

    /**
     * 删除店铺类目名称
     *
     * @param id 店铺类目id
     * @return 修改是否成功
     */
    Response<Boolean> delete(@Nonnull Long id);


}
