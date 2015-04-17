package com.aixforce.shop.service;

import com.aixforce.common.model.Response;
import com.aixforce.shop.dao.ItemTagDao;
import com.aixforce.shop.dao.ShopDao;
import com.aixforce.shop.dao.ShopInteriorCategoryDao;
import com.aixforce.shop.model.Shop;
import com.aixforce.shop.model.ShopInteriorCategory;
import com.google.common.base.Throwables;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Nonnull;
import java.util.List;

/**
 * Author:  <a href="mailto:jlchen.cn@gmail.com">jlchen</a>
 * Date: 2013-11-01
 */
@Service
public class ItemTagServiceImpl implements ItemTagService {

    private final static Logger log = LoggerFactory.getLogger(ItemTagServiceImpl.class);

    @Autowired
    private ShopInteriorCategoryDao shopInteriorCategoryDao;

    @Autowired
    private ItemTagDao itemTagDao;

    @Autowired
    private ShopDao shopDao;

    /**
     * 创建店铺内的商品类目树
     *
     * @param userId          用户id
     * @param shopId          店铺id
     * @param tree            类目树
     * @param tagsToBeRemoved 待删除的tags
     * @return mysql中的主键id
     */
    @Override
    public Response<Long> saveTree(@Nonnull Long userId, @Nonnull Long shopId, @Nonnull String tree, List<String> tagsToBeRemoved) {
        Response<Long> result = new Response<Long>();
        try {

            ShopInteriorCategory shopInteriorCategory = shopInteriorCategoryDao.findByShopId(shopId);
            if (shopInteriorCategory == null) {
                shopInteriorCategory = new ShopInteriorCategory();
                shopInteriorCategory.setShopId(shopId);
                shopInteriorCategory.setCategories(tree.trim());
                Long id = shopInteriorCategoryDao.create(shopInteriorCategory);
                result.setResult(id);
            } else {
                shopInteriorCategory.setCategories(tree.trim());
                shopInteriorCategoryDao.update(shopInteriorCategory);
                result.setResult(shopInteriorCategory.getId());
            }
            //处理待删除的tags
            for (String tag : tagsToBeRemoved) {
                itemTagDao.removeTag(userId, tag);
            }
            return result;
        } catch (Exception e) {
            log.error("failed to save tag tree ({}) for shop(id={}),cause:{}",
                    tree, shopId, Throwables.getStackTraceAsString(e));
            result.setError("tagTree.save.fail");
            return result;
        }
    }

    /**
     * 根据店铺id查找商品类目树
     *
     * @param userId 用户id
     * @return 类目树的json字符串
     */
    @Override
    public Response<String> findTree(Long userId) {
        Response<String> result = new Response<String>();
        Shop shop = shopDao.findByUserId(userId);
        if (shop == null) {
            log.error("no shop found for user(id={})", userId);
            result.setError("shop.not.found");
            return result;
        }
        Long shopId = shop.getId();
        try {
            ShopInteriorCategory shopInteriorCategory = shopInteriorCategoryDao.findByShopId(shopId);
            if (shopInteriorCategory == null) {
                log.warn("no shop interior category found for shop(id={})", shopId);
                result.setResult("");
                return result;
            } else {
                result.setResult(shopInteriorCategory.getCategories());
                return result;
            }
        } catch (Exception e) {
            log.error("failed to query shop interior category where shopId={},cause:{}",
                    shopId, Throwables.getStackTraceAsString(e));
            result.setError("itemTags.query.fail");
            return result;
        }
    }

    @Override
    public Response<String> findTreeByBuyer(Long sellerId) {
        return findTree(sellerId);
    }


    /**
     * 将商品和店铺内类目接触关联
     *
     * @param userId 用户id
     * @param itemId 商品id
     * @param tag    tag名称
     * @return 是否解除关联成功
     */
    @Override
    public Response<Boolean> removeTagOfItem(@Nonnull Long userId, @Nonnull Long itemId, @Nonnull String tag) {
        Response<Boolean> result = new Response<Boolean>();
        try {
            itemTagDao.removeTagOfItem(userId, itemId, tag.trim());
            if (!itemTagDao.hasTags(userId, itemId)) {
                itemTagDao.addUnclassifiedItem(userId, itemId);
            }
            result.setSuccess(true);
            return result;
        } catch (Exception e) {
            log.error("failed to delete tag ({}) for item(id={}) where user(id={}),cause:{}",
                    tag, itemId, userId, Throwables.getStackTraceAsString(e));
            result.setError("tag.delete.item.fail");
            return result;
        }
    }


    /**
     * 批量为商品添加tags
     *
     * @param userId  用户id
     * @param itemIds 商品id列表
     * @param tags    tag名称列表
     * @return 是否成功
     */
    @Override
    public Response<Boolean> addTagsForItems(Long userId, List<Long> itemIds, List<String> tags) {
        Response<Boolean> result = new Response<Boolean>();
        try {
            itemTagDao.addTagsForItems(userId, itemIds, tags);
            result.setSuccess(true);
            return result;
        } catch (Exception e) {
            log.error("failed to add tag ({}) for items({}) where user(id={}),cause:{}",
                    tags, itemIds, userId, Throwables.getStackTraceAsString(e));
            result.setError("tag.add.item.fail");
            return result;
        }
    }


    /**
     * 批量为商品删除tags
     *
     * @param userId  用户id
     * @param itemIds 商品id列表
     * @param tags    tag列表
     * @return 是否删除成功
     */
    @Override
    public Response<Boolean> removeTagsOfItems(Long userId, List<Long> itemIds, List<String> tags) {
        Response<Boolean> result = new Response<Boolean>();
        try {
            itemTagDao.removeTagsOfItems(userId, tags, itemIds);
            for (Long itemId : itemIds) {
                if (!itemTagDao.hasTags(userId, itemId)) {
                    itemTagDao.addUnclassifiedItem(userId, itemId);
                }
            }
            result.setSuccess(true);
            return result;
        } catch (Exception e) {
            log.error("failed to delete tags ({}) for items({}) where user(id={}),cause:{}",
                    tags, itemIds, userId, Throwables.getStackTraceAsString(e));
            result.setError("tag.delete.item.fail");
            return result;
        }
    }

    @Override
    public Response<Boolean> addUnclassifiedItem(Long userId, Long itemId) {
        Response<Boolean> result = new Response<Boolean>();
        if (userId == null) {
            log.error("userId cannot be null");
            result.setError("userId.null.fail");
            return result;
        }
        if (itemId == null) {
            log.error("item cannot be null");
            result.setError("itemId.null.fail");
            return result;
        }
        Boolean success = itemTagDao.addUnclassifiedItem(userId, itemId);
        if (success) {
            result.setResult(true);
            return result;
        } else {
            result.setError("unclassified.tag.create.fail");
            return result;
        }
    }

    @Override
    public Response<Boolean> removeTagsOfItems(Long userId, List<Long> itemIds) {
        Response<Boolean> result = new Response<Boolean>();
        try {
            for (Long itemId : itemIds) {
                Boolean hasTags = itemTagDao.hasTags(userId, itemId);
                if (hasTags) {
                    itemTagDao.removeTagsOfItem(userId, itemId);
                } else {
                    itemTagDao.removeUnclassified(userId, itemId);
                }
            }
            result.setResult(Boolean.TRUE);
            return result;
        } catch (Exception e) {
            log.error("remove tags of item failed, itemIds={}, cause={}", itemIds, Throwables.getStackTraceAsString(e));
            result.setError("items.tags.remove.fail");
            return result;
        }
    }


}
