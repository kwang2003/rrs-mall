package com.aixforce.item.service;

import com.aixforce.common.model.Response;
import com.aixforce.item.dao.mysql.ItemBundleDao;
import com.aixforce.item.dto.ItemBundle.BundleItemDetails;
import com.aixforce.item.dto.ItemBundle.BundleItems;
import com.aixforce.item.model.Item;
import com.aixforce.item.model.ItemBundle;
import com.aixforce.item.model.ItemDetail;
import com.aixforce.item.model.Sku;
import com.aixforce.user.base.BaseUser;
import com.google.common.base.Objects;
import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Created by yangzefeng on 14-4-21
 */
@Service
@Slf4j
public class ItemBundleServiceImpl implements ItemBundleService {

    @Autowired
    private ItemBundleDao itemBundleDao;

    @Autowired
    private ItemService itemService;

    @Override
    public Response<ItemBundle> findById(Long id) {
        Response<ItemBundle> result = new Response<ItemBundle>();
        if(id == null) {
            log.error("param can not be null");
            result.setError("illegal.param");
            return result;
        }
        try {
            ItemBundle itemBundle = itemBundleDao.findById(id);
            result.setResult(itemBundle);
            return result;
        }catch (Exception e) {
            log.error("fail to find item bundle by id{},cause:{}",id, Throwables.getStackTraceAsString(e));
            result.setError("item.bundle.query.fail");
            return result;
        }
    }

    @Override
    public Response<Long> create(ItemBundle itemBundle) {
        Response<Long> result = new Response<Long>();
        if(itemBundle == null) {
            log.error("param can not be null");
            result.setError("illegal.param");
            return result;
        }
        try {
            itemBundleDao.create(itemBundle);
            result.setResult(itemBundle.getId());
            return result;
        }catch (Exception e) {
            log.error("fail to create item bundle{},cause:{}", itemBundle, Throwables.getStackTraceAsString(e));
            result.setError("item.bundle.create.fail");
            return result;
        }
    }

    @Override
    public Response<Boolean> update(ItemBundle itemBundle, Long userId) {
        Response<Boolean> result = new Response<Boolean>();
        if(itemBundle == null || itemBundle.getId() == null) {
            log.error("param can not be null");
            result.setError("illegal.param");
            return result;
        }
        ItemBundle exist = itemBundleDao.findById(itemBundle.getId());
        if(exist == null) {
            log.error("id{} illegal for item bundle", itemBundle.getId());
            result.setError("illegal.param");
            return result;
        }
        if(!Objects.equal(userId, exist.getSellerId())) {
            log.error("authorize fail currentUser{}, itemBundle belong to{}", userId, exist.getSellerId());
            result.setError("authorize.fail");
            return result;
        }
        try {
            itemBundleDao.update(itemBundle);
            result.setResult(Boolean.TRUE);
            return result;
        }catch (Exception e) {
            log.error("fail to update item bundle{},cause:{}", itemBundle, Throwables.getStackTraceAsString(e));
            result.setError("item.bundle.update.fail");
            return result;
        }
    }

    @Override
    public Response<List<BundleItems>> findBySellerId(BaseUser user) {
        Response<List<BundleItems>> result = new Response<List<BundleItems>>();
        Long sellerId = user.getId();
        if(sellerId == null) {
            log.error("param can not be null");
            result.setError("illegal.param");
            return result;
        }
        try {
            List<ItemBundle> itemBundles = itemBundleDao.findBySellerId(sellerId);
            List<BundleItems> resultList = Lists.newArrayListWithCapacity(itemBundles.size());
            for(ItemBundle ib : itemBundles) {
                List<Long> itemIds = getItemIds(ib);
                Response<List<Item>> itemsR = itemService.findByIds(itemIds);
                if(!itemsR.isSuccess()) {
                    log.error("fail to find items by ids={}, error code:{}",
                            itemIds, itemsR.getError());
                    continue;
                }
                List<Item> items = itemsR.getResult();
                BundleItems bis = new BundleItems();
                bis.setItemBundle(ib);
                bis.setItems(items);
                resultList.add(bis);
            }
            result.setResult(resultList);
            return result;
        }catch (Exception e) {
            log.error("fail to find item bundle by sellerId{}, cause:{}", sellerId, Throwables.getStackTraceAsString(e));
            result.setError("item.bundle.query.fail");
            return result;
        }
    }

    @Override
    public Response<BundleItemDetails> findBundleItemDetails(Long id) {
        Response<BundleItemDetails> result = new Response<BundleItemDetails>();
        if(id == null) {
            log.error("param can not be null");
            result.setError("illegal.param");
            return result;
        }
        try {
            BundleItemDetails bids = new BundleItemDetails();
            ItemBundle itemBundle = itemBundleDao.findById(id);
            bids.setItemBundle(itemBundle);

            List<Long> itemIds = getItemIds(itemBundle);
            Response<List<Item>> itemsR = itemService.findByIds(itemIds);
            if(!itemsR.isSuccess()) {
                log.error("fail to find item by ids={}, error code:{}", itemIds);
                result.setError(itemsR.getError());
                return result;
            }
            List<Item> items = itemsR.getResult();

            List<BundleItemDetails.ItemDetailAndSku> detailAndSkus = Lists.newArrayListWithCapacity(items.size());
            for(Item i : items) {
                BundleItemDetails.ItemDetailAndSku detailAndSku = new BundleItemDetails.ItemDetailAndSku();
                detailAndSku.setItem(i);
                Response<ItemDetail> itemDetailR = itemService.findDetailBy(i.getId());
                if(!itemDetailR.isSuccess()) {
                    log.error("fail to find itemDetail by itemId={}, error code:{}", i.getId(), itemDetailR.getError());
                }else {
                    ItemDetail itemDetail = itemDetailR.getResult();
                    detailAndSku.setItemDetail(itemDetail);
                }
                Response<List<Sku>> skuR = itemService.findSkusByItemId(i.getId());
                if(!skuR.isSuccess()) {
                    log.error("fail to find skus by itemId={}, error code:{}",i.getId(), skuR.getError());
                }else {
                    List<Sku> skus = skuR.getResult();
                    detailAndSku.setSkuList(skus);
                }
                detailAndSkus.add(detailAndSku);
            }
            bids.setItemDetails(detailAndSkus);
            result.setResult(bids);
            return result;
        }catch (Exception e) {
            log.error("fail to find bundle item detail by bundleItemId={}, cause:{}");
            result.setError("item.bundle.detail.query.fail");
            return result;
        }
    }

    private List<Long> getItemIds(ItemBundle itemBundle) {
        List<Long> itemIds = Lists.newArrayListWithCapacity(4);
        if(itemBundle.getItemId1() != null) itemIds.add(itemBundle.getItemId1());
        if(itemBundle.getItemId2() != null) itemIds.add(itemBundle.getItemId2());
        if(itemBundle.getItemId3() != null) itemIds.add(itemBundle.getItemId3());
        if(itemBundle.getItemId4() != null) itemIds.add(itemBundle.getItemId4());
        return itemIds;
    }
}
