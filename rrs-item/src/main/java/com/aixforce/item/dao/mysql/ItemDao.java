package com.aixforce.item.dao.mysql;

import com.aixforce.common.model.Paging;
import com.aixforce.item.model.Item;
import com.google.common.base.Objects;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import org.mybatis.spring.support.SqlSessionDaoSupport;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Author:  <a href="mailto:jlchen.cn@gmail.com">jlchen</a>
 * Date: 2013-01-31
 */
@Repository
public class ItemDao extends SqlSessionDaoSupport {

    public Item findById(Long id) {
        return getSqlSession().selectOne("Item.findById", id);
    }

    public List<Item> findByIds(List<Long> ids) {
        return getSqlSession().selectList("Item.findByIds", ids);
    }

    public Item findBySellerIdAndSpuId(Long sellerId, Long spuId) {
        return getSqlSession().selectOne("Item.findBySellerIdAndSpuId", ImmutableMap.of("sellerId", sellerId, "spuId", spuId));
    }

    public List<Item> findItemsBySellerIdAndSpuId(Long sellerId, Long spuId) {
        return getSqlSession().selectList("Item.findBySellerIdAndSpuId",
                ImmutableMap.of("sellerId", sellerId, "spuId", spuId));
    }

    public void create(Item item) {
        getSqlSession().insert("Item.create", item);
    }

    public void delete(Long id) {
        getSqlSession().delete("Item.delete", id);
    }

    public void update(Item item) {
        getSqlSession().update("Item.update", item);
    }

    public void bulkUpdateStatus(Long userId, int status, List<Long> ids) {
        if (ids.isEmpty()) {
            return;
        }
        Map<String, Object> params = Maps.newHashMapWithExpectedSize(3);
        params.put("status", status);
        if (userId != null) {
            params.put("userId", userId);
        }
        params.put("ids", ids);
        switch (Item.Status.fromNumber(status)) {
            case ON_SHELF:
                params.put("onShelf", true);
                break;
            case OFF_SHELF:
                params.put("offShelf", true);
                break;
            case FROZEN:
                params.put("frozen", true);
                break;
            case DELETED:
                params.put("deleted", true);
                break;
            default:
                throw new IllegalArgumentException("unknown item status value:" + status);
        }
        getSqlSession().update("Item.bulkUpdateStatus", params);
    }

    /**
     * 卖家后台管理商品列表
     *
     * @param userId 卖家id
     * @param offset 偏移
     * @param size   条数
     * @param params 查询参数
     * @return 商品列表
     */
    public Paging<Item> sellerItems(Long userId, int offset, int size, Map<String, Object> params) {
        params.put("userId", checkNotNull(userId));
        Integer count = getSqlSession().selectOne("Item.sellerItemCount", params);
        count = Objects.firstNonNull(count, 0);
        if (count == 0) {
            return new Paging<Item>(0L, Collections.<Item>emptyList());
        }

        params.put("offset", offset);
        params.put("limit", size);

        List<Item> items = getSqlSession().selectList("Item.sellerItems", params);
        return new Paging<Item>(count.longValue(), items);
    }

    public List<Item> forDump(Long lastId, Integer limit) {
        return getSqlSession().selectList("Item.forDump", ImmutableMap.of("lastId", lastId, "limit", limit));
    }

    public List<Item> forDeltaDump(Long lastId, String compared, Integer limit) {
        return getSqlSession().selectList("Item.forDeltaDump", ImmutableMap.of("lastId", lastId, "limit", limit, "compared", compared));
    }

    public List<Item> findPagingByShopId(Long lastId, Long shopId, Integer limit) {
        return getSqlSession().selectList("Item.findPagingByShopId", ImmutableMap.of(
                "lastId", lastId, "shopId", shopId, "limit", limit
        ));
    }

    public Long maxIdByShopId(Long shopId) {
        return Objects.firstNonNull((Long)getSqlSession().selectOne("Item.maxIdByShopId", shopId), 0L);
    }

    public Long maxId() {
        return Objects.firstNonNull((Long) getSqlSession().selectOne("Item.maxId"), 0L);
    }

    public void changeStock(Long id, int delta) {
        getSqlSession().update("Item.changeStock", ImmutableMap.of("id", id, "delta", delta));
    }

    public Paging<Item> findAllItems(Integer offset, Integer limit, List<Integer> status) {
        Integer count = getSqlSession().selectOne("Item.ItemCountByStatus", ImmutableMap.of("status", status));
        count = Objects.firstNonNull(count, 0);
        if (count == 0) {
            return new Paging<Item>(0L, Collections.<Item>emptyList());
        }

        List<Item> items = getSqlSession().selectList("Item.findAllItems",
                ImmutableMap.of("offset", offset, "limit", limit, "status", status));
        return new Paging<Item>(count.longValue(), items);
    }

    public Boolean updateStatus(Long itemId, Integer status) {
        return getSqlSession().update("Item.updateStatus",
                ImmutableMap.of("id", itemId, "status", status)) == 1;
    }

    public void updateStatusBySellerId(Long sellerId, Integer status) {
        getSqlSession().update("Item.updateStatusBySellerId",
                ImmutableMap.of("sellerId", sellerId, "status", status));
    }

    public Paging<Item> findBySellerId(Integer offset, Integer limit, Long sellerId, List<Integer> status) {
        Integer count = getSqlSession().selectOne("Item.sellerItemCount",
                ImmutableMap.of("userId", sellerId, "status", status));
        count = Objects.firstNonNull(count, 0);
        if (count == 0) {
            return new Paging<Item>(0L, Collections.<Item>emptyList());
        }
        List<Item> items = getSqlSession().selectList("Item.findBySellerId",
                ImmutableMap.of("sellerId", sellerId, "offset", offset, "limit", limit, "status", status));
        return new Paging<Item>(count.longValue(), items);
    }

    /**
     * 因为交易引起的库存和销量的变化
     *
     * @param id    商品id
     * @param delta 变化量,对于卖出商品为负值,对于退货则为正值
     */
    public void changeStockAndSoldQuantity(Long id, Integer delta) {
        getSqlSession().update("Item.changeStockAndSoldQuantity", ImmutableMap.of("id", id, "stockDelta", delta, "soldQuantityDelta", -delta));
    }

    public List<Long> findIdsBySellerId(Long sellerId) {
        return getSqlSession().selectList("Item.findIdsBySellerId", sellerId);
    }

    public List<Item> findBySellerIdNoPaging(Long sellerId) {
        return getSqlSession().selectList("Item.findBySellerIdNoPaging", sellerId);
    }

    public Integer countBySpuId(Long spuId) {
        return getSqlSession().selectOne("Item.ItemCountBySpuId", spuId);
    }

    public List<Item> findBySpuId(Long spuId) {
        return getSqlSession().selectList("Item.findBySpuId", spuId);
    }

    public List<Item> findOnShelfBySpuId(Long spuId) {
        return getSqlSession().selectList("Item.findOnShelfBySpuId", spuId);
    }

    public List<Item> findByShopId(Long shopId) {
        return getSqlSession().selectList("Item.findByShopId", shopId);
    }

    /**
     * 通过模板编号查询商品信息
     * @param modelId   模板编号
     * @return  List
     * 返回商品信息
     */
    public List<Item> findByModelId(Long modelId){
        return getSqlSession().selectList("Item.findByModelId" , modelId);
    }

    public void batchUpdateItemRegion(List<Long> itemIds, String region) {
        getSqlSession().update("Item.batchUpdateItemRegion",
                ImmutableMap.of("ids", itemIds, "region", region));
    }

    public Long countOnShelfByShopId(Long shopId) {
        return getSqlSession().selectOne("Item.countOnShelfByShopId", shopId);
    }

    public Integer sellerItemCount(Map<String, Object> params) {
        Integer count = getSqlSession().selectOne("Item.sellerItemCount", params);
        count = Objects.firstNonNull(count, 0);
        return count;
    }
}
