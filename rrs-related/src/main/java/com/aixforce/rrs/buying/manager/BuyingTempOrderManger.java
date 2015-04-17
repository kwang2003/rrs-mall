package com.aixforce.rrs.buying.manager;

import com.aixforce.common.model.Response;
import com.aixforce.exception.ServiceException;
import com.aixforce.item.service.ItemService;
import com.aixforce.rrs.buying.dao.BuyingTempOrderDao;
import com.aixforce.rrs.buying.model.BuyingItem;
import com.aixforce.rrs.buying.model.BuyingTempOrder;
import com.aixforce.rrs.presale.dao.AddressStorageDao;
import com.aixforce.rrs.presale.dao.StorageStockDao;
import com.aixforce.rrs.presale.model.AddressStorage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import static com.aixforce.common.utils.Arguments.notNull;

/**
 * Created by songrenfei on 14-9-23
 */
@Component
@Slf4j
public class BuyingTempOrderManger {

    @Autowired
    private BuyingTempOrderDao buyingTempOrderDao;

    @Autowired
    private ItemService itemService;

    @Autowired
    private AddressStorageDao addressStorageDao;

    @Autowired
    private StorageStockDao storageStockDao;


    /**
     * 创建虚拟订单
     * @param buyingTempOrder 虚拟订单
     * @param isStorage 是否分仓
     * @param itemId 商品id
     * @param skuId skuid
     * @param skuQuantity 购买数量
     * @return
     */
    @Transactional
    public Long create(BuyingTempOrder buyingTempOrder,Boolean isStorage,Long itemId,Long skuId,Integer skuQuantity,Integer regionId){

        //减少库存
        if(isStorage){

            //首先根据商品id和地区id查找仓库码
            AddressStorage addressStorage = addressStorageDao.findByItemIdAndAddressId(itemId, regionId);
            if (addressStorage == null) {
                log.warn("no addressStorage found by itemId={} and addressId={}", itemId, regionId);
                throw new ServiceException("storage.not.found");
            }
            //修改分仓的销量
            Long storageId = addressStorage.getStorageId();
            storageStockDao.changeSoldCount(skuQuantity, itemId, storageId);

            //将分仓信息和order信息关联起来, 取消订单恢复库存要用到这个信息
            //preSaleRedisDao.addStorageId2PreSaleOrder(orderId, storageId, item.getId(),quantity);

        }else{
            Response<Boolean> dr = itemService.changeSoldQuantityAndStock(skuId, itemId, -skuQuantity);
            if (!dr.isSuccess()) {
                log.error("failed to decrement stock of sku(id={}) and item(id={}),error code:{}",
                        skuId, itemId, dr.getError());
            }
        }
        buyingTempOrder.setRegionId(regionId);
        return buyingTempOrderDao.create(buyingTempOrder);
    }

    /*
     取消订单
     */
    public Boolean cancelOrder(BuyingTempOrder buyingTempOrder,BuyingItem buyingItem){

        //是否支持分仓
        if(notNull(buyingItem.getIsStorage())&&buyingItem.getIsStorage()){
            //首先根据商品id和地区id查找仓库码
            AddressStorage addressStorage = addressStorageDao.findByItemIdAndAddressId(buyingTempOrder.getItemId(), buyingTempOrder.getRegionId());
            if (addressStorage == null) {
                log.error("no addressStorage found by itemId={} and addressId={}", buyingTempOrder.getItemId(), buyingTempOrder.getRegionId());
                throw new ServiceException("storage.not.found");
            }
            //修改分仓的销量
            Long storageId = addressStorage.getStorageId();

            storageStockDao.changeSoldCount(-buyingTempOrder.getSkuQuantity(), buyingTempOrder.getItemId(), storageId);

        }else{
            Response<Boolean> dr = itemService.changeSoldQuantityAndStock(buyingTempOrder.getSkuId(), buyingTempOrder.getItemId(),
                                                                            buyingTempOrder.getSkuQuantity());
            if (!dr.isSuccess()) {
                log.error("failed to decrement stock of sku(id={}) and item(id={}),error code:{}",
                        buyingTempOrder.getSkuId(), buyingTempOrder.getItemId(), dr.getError());
            }
        }

        BuyingTempOrder tempOrder = new BuyingTempOrder();
        tempOrder.setId(buyingTempOrder.getId());
        tempOrder.setStatus(BuyingTempOrder.Status.CANCEL.value());
        return buyingTempOrderDao.update(tempOrder);

    }


}
