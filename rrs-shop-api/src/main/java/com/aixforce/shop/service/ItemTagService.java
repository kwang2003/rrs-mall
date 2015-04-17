package com.aixforce.shop.service;

import com.aixforce.annotations.ParamInfo;
import com.aixforce.common.model.Response;

import javax.annotation.Nonnull;
import java.util.List;

/**
 * Author:  <a href="mailto:jlchen.cn@gmail.com">jlchen</a>
 * Date: 2013-10-29
 */
public interface ItemTagService {

    /**
     * 创建店铺内的商品类目树
     *
     * @param userId          用户id
     * @param shopId          店铺id
     * @param tree            类目树
     * @param tagsToBeRemoved 待删除的tags
     * @return mysql中的主键id
     */
    Response<Long> saveTree(@Nonnull Long userId, @Nonnull Long shopId, @Nonnull String tree, List<String> tagsToBeRemoved);


    /**
     * 根据当前登录用户查找商品类目树，该接口商家后台调用
     *
     * @param userId 用户id
     * @return 类目树的json字符串
     */
    Response<String> findTree(Long userId);

    /**
     * 返回店铺内类目树，该接口买家能调用,配组件形式
     *
     * @param sellerId 店主id
     * @return 店铺内类目树
     */
    Response<String> findTreeByBuyer(@ParamInfo("sellerId") Long sellerId);


    /**
     * 将商品和店铺内类目接触关联
     *
     * @param userId 用户Id
     * @param itemId 商品id
     * @param tag    tag名称
     * @return 是否解除关联成功
     */
    Response<Boolean> removeTagOfItem(@Nonnull final Long userId, @Nonnull final Long itemId, @Nonnull final String tag);


    /**
     * 批量为商品添加tags
     *
     * @param userId  用户id
     * @param itemIds 商品id列表
     * @param tags    tag名称列表
     * @return 是否成功
     */
    Response<Boolean> addTagsForItems(Long userId, List<Long> itemIds, List<String> tags);


    /**
     * 批量为商品删除tags
     *
     * @param userId  用户id
     * @param itemIds 商品id列表
     * @param tags    tag列表
     * @return 是否删除成功
     */
    Response<Boolean> removeTagsOfItems(Long userId, List<Long> itemIds, List<String> tags);

    /**
     * 为未分类商品添加标签
     *
     * @param userId 用户id
     * @param itemId 商品id
     * @return 是否添加成功
     */
    Response<Boolean> addUnclassifiedItem(Long userId, Long itemId);

    /**
     * 删除商品后删除商品tag，如果商品还未分类删除unclassified－items中该itemId
     *
     * @param userId  用户id
     * @param itemIds 商品ids
     * @return 是否删除成功
     */
    Response<Boolean> removeTagsOfItems(Long userId, List<Long> itemIds);
}
