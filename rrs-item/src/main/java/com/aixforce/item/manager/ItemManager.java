package com.aixforce.item.manager;

import com.aixforce.common.model.Paging;
import com.aixforce.exception.ServiceException;
import com.aixforce.item.dao.mysql.ItemDao;
import com.aixforce.item.dao.mysql.ItemDetailDao;
import com.aixforce.item.dao.mysql.ItemServiceDao;
import com.aixforce.item.dao.mysql.SkuDao;
import com.aixforce.item.model.Item;
import com.aixforce.item.model.ItemDetail;
import com.aixforce.item.model.Sku;
import com.google.common.base.Objects;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Author:  <a href="mailto:jlchen.cn@gmail.com">jlchen</a>
 * Date: 2013-11-14
 */
@Component
public class ItemManager {
    private final static Logger log = LoggerFactory.getLogger(ItemManager.class);

    @Autowired
    private ItemDao itemDao;

    @Autowired
    private ItemDetailDao itemDetailDao;

    @Autowired
    private SkuDao skuDao;

    @Autowired
    private ItemServiceDao serviceTemplateDao;

    /**
     * 创建商品,商品detail,skus
     *
     * @param item       商品
     * @param itemDetail 商品详情
     * @param skus       skus
     * @return 新创建的商品id
     */
    @Transactional
    public Long create(Item item, ItemDetail itemDetail, List<Sku> skus) {
        log.info("in item manager create item:{}", item);
        item.setName(item.getName().trim());
        item.setSoldQuantity(0);
        itemDao.create(item);
        log.debug("succeed to create  {}", item);
        if (itemDetail != null) {
            itemDetail.setItemId(item.getId());
            itemDetailDao.create(itemDetail);
            log.debug("succeed to create {} and {}", item, itemDetail);
        }
        for (Sku sku : skus) {
            sku.setItemId(item.getId());
        }

        skuDao.create(skus);

        if (!Strings.isNullOrEmpty(item.getTemplateId())) {
            if(!serviceTemplateDao.isItemTempBinded(item.getId(), item.getTemplateId())) {
                serviceTemplateDao.deleteItemTempRelation(item.getId());
                serviceTemplateDao.saveItemTempRelation(item.getId(), item.getTemplateId(), item.getTemplateName());
                log.info("succeed to bind item:{} and template:{}", item, item.getTemplateId());
            }
        } else {
            serviceTemplateDao.deleteItemTempRelation(item.getId());
        }
        return item.getId();
    }

    @Transactional
    public void updateItemAndItemDetailAndCreateSku(Item item, ItemDetail itemDetail, Sku sku) {
        itemDao.update(item);
        itemDetail.setItemId(item.getId());
        itemDetailDao.update(itemDetail);
        skuDao.create(Lists.newArrayList(sku));
    }

    @Transactional
    public void update(Item item, ItemDetail itemDetail, List<Sku> skus) {
        log.info("in item manager update item:{}", item);
        itemDao.update(item);
        log.debug("succeed to update item to {}", item);

        itemDetail.setItemId(item.getId());
        itemDetailDao.update(itemDetail);

        //sku can only update price and stock
        List<Sku> skusToUpdate = Lists.newArrayListWithCapacity(skus.size());
        for (Sku sku : skus) {
            Sku toUpdate = new Sku();
            toUpdate.setId(sku.getId());
            toUpdate.setPrice(sku.getPrice());
            toUpdate.setStock(sku.getStock());
            skusToUpdate.add(toUpdate);
        }

        skuDao.update(skusToUpdate);

        if (!Strings.isNullOrEmpty(item.getTemplateId())) {
            if(!serviceTemplateDao.isItemTempBinded(item.getId(), item.getTemplateId())) {
                serviceTemplateDao.deleteItemTempRelation(item.getId());
                serviceTemplateDao.saveItemTempRelation(item.getId(), item.getTemplateId(), item.getTemplateName());
                log.info("succeed to bind item:{} and template:{}", item, item.getTemplateId());
            }
        } else {
            serviceTemplateDao.deleteItemTempRelation(item.getId());
        }
    }

    /**
     * 根据商品id列表批量获取商品
     *
     * @param ids id列表
     * @return 商品列表
     */
    public List<Item> findByIds(List<Long> ids) {
        if(ids == null||ids.isEmpty()){
            return Collections.emptyList();
        }
        return itemDao.findByIds(ids);
    }

    /**
     * 根据商品id查询商品
     *
     * @param itemId 商品id
     * @return 商品
     */
    public Item findById(Long itemId) {
        return itemDao.findById(itemId);
    }

    public Item findBySellerIdAndSpuId(Long sellerId, Long spuId) {
        return itemDao.findBySellerIdAndSpuId(sellerId, spuId);
    }

    /**
     * 根据itemId 查询商品的辅图
     *
     * @param itemId 商品id
     * @return 商品详情(辅图)
     */
    public ItemDetail findDetailByItemId(Long itemId) {
        return itemDetailDao.findByItemId(itemId);
    }

    /**
     * 根据商品id查询sku列表
     *
     * @param itemId 商品id
     * @return sku列表
     */
    public List<Sku> findSkusByItemId(Long itemId) {
        return skuDao.findByItemId(itemId);
    }

    /**
     * 卖家后台商品列表,用于管理店铺内商品
     *
     * @param userId 用户id
     * @param offset 起始便宜位置
     * @param size   返回条数
     * @param params 搜索参数
     * @return 商品列表
     */
    public Paging<Item> sellerItems(Long userId, int offset, Integer size, Map<String, Object> params) {
        return itemDao.sellerItems(userId, offset, size, params);
    }

    /**
     * 减商品和sku的库存
     *
     * @param skuId    sku id
     * @param itemId   商品 id
     * @param quantity 数量
     */
    @Transactional
    public void decrementStock(Long skuId, Long itemId, Integer quantity) {
        skuDao.changeStock(skuId, -quantity);
        itemDao.changeStock(itemId, -quantity);
    }

    /**
     * 增加商品和sku的库存
     *
     * @param skuId    sku id
     * @param itemId   商品 id
     * @param quantity 数量
     */
    @Transactional
    public void incrementStock(Long skuId, Long itemId, Integer quantity) {
        skuDao.changeStock(skuId, quantity);
        itemDao.changeStock(itemId, quantity);
    }

    @Transactional
    public void bulkUpdateStatus(Long userId, Integer status, List<Long> ids) {
        itemDao.bulkUpdateStatus(userId, status, ids);
    }

    @Transactional
    public void delete(Long userId, Long itemId) {
        Item item = itemDao.findById(itemId);
        if (item == null) {
            log.warn("can not find item(id={}),return directly", itemId);
            return;
        }

        if (!Objects.equal(userId, item.getUserId())) {
            throw new ServiceException("item.not.owner");
        }
        if (Objects.equal(item.getSoldQuantity(), 0)) {//如果商品未售出,则物理删除商品及相关信息
            itemDao.delete(itemId);
            itemDetailDao.delete(itemId);
            skuDao.deleteByItemId(itemId);
        } else {   //如果商品已经有了交易,则只逻辑删除商品
            itemDao.updateStatus(item.getId(), Item.Status.DELETED.toNumber());
        }

    }

    public Sku findSkuById(Long skuId) {
        return skuDao.findById(skuId);
    }

    /**
     * 因为交易或者退货引起的库存和销量的变化
     *
     * @param skuId    sku id
     * @param itemId   商品id
     * @param quantity 变化量,对于卖出商品为负值,对于退货则为正值
     */
    @Transactional
    public void changeSoldQuantityAndStock(Long skuId, Long itemId, Integer quantity) {
        skuDao.changeStock(skuId, quantity);
        itemDao.changeStockAndSoldQuantity(itemId, quantity);
    }

    public void updateStatusBySellerId(Long sellerId, Integer status) {
        itemDao.updateStatusBySellerId(sellerId,status);
    }

    public Paging<Item> findBySellerId(int offset, Integer size, Long sellerId, List<Integer> concernedStatus) {
        return itemDao.findBySellerId(offset, size, sellerId, concernedStatus);
    }

    public Paging<Item> findAllItems(int offset, Integer size, List<Integer> concernedStatus) {
        return itemDao.findAllItems(offset,size,concernedStatus);
    }

    public List<Long> findIdsBySellerId(Long sellerId) {
        return itemDao.findIdsBySellerId(sellerId);
    }
}
