package com.aixforce.rrs.buying.manager;

import com.aixforce.common.model.Response;
import com.aixforce.exception.ServiceException;
import com.aixforce.item.model.Item;
import com.aixforce.item.service.ItemService;
import com.aixforce.rrs.buying.dao.BuyingActivityDefinitionDao;
import com.aixforce.rrs.buying.dao.BuyingItemDao;
import com.aixforce.rrs.buying.dao.BuyingOrderRecordDao;
import com.aixforce.rrs.buying.dao.BuyingTempOrderDao;
import com.aixforce.rrs.buying.dto.BuyingActivityDto;
import com.aixforce.rrs.buying.model.BuyingActivityDefinition;
import com.aixforce.rrs.buying.model.BuyingItem;
import com.aixforce.rrs.buying.model.BuyingOrderRecord;
import com.aixforce.rrs.buying.model.BuyingTempOrder;
import com.aixforce.rrs.code.dao.CodeUsageDao;
import com.aixforce.rrs.code.dto.DiscountAndUsage;
import com.aixforce.rrs.code.model.CodeUsage;
import com.aixforce.rrs.settle.model.ItemSettlement;
import com.aixforce.shop.model.Shop;
import com.aixforce.shop.service.ShopService;
import com.aixforce.trade.model.Order;
import com.aixforce.trade.model.OrderExtra;
import com.aixforce.trade.model.OrderItem;
import com.aixforce.trade.service.OrderWriteService;
import com.google.common.base.Function;
import com.google.common.base.Objects;
import com.google.common.collect.Lists;
import com.sun.org.apache.xpath.internal.operations.Bool;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

import static com.aixforce.common.utils.Arguments.isNull;

/**
 * Created by songrenfei on 14-9-23
 */
@Component
@Slf4j
public class BuyingActivityManger {

    @Autowired
    private BuyingActivityDefinitionDao buyingActivityDefinitionDao;

    @Autowired
    private BuyingItemDao buyingItemDao;

    @Autowired
    private ItemService itemService;

    @Autowired
    private ShopService shopService;

    @Autowired
    private OrderWriteService orderWriteService;

    @Autowired
    private BuyingOrderRecordDao buyingOrderRecordDao;

    @Autowired
    private BuyingTempOrderDao buyingTempOrderDao;

    @Autowired
    private CodeUsageDao codeUsageDao;

    @Value("#{app.eHaierSellerId}")
    private String eHaierSellerId;

    @Transactional
    public Long create(BuyingActivityDto buyingActivityDto,Long userId){


        BuyingActivityDefinition buyingActivityDefinition =buyingActivityDto.getBuyingActivityDefinition();

        Response<Shop> shopRes = shopService.findByUserId(userId);
        if(!shopRes.isSuccess()){
            throw new ServiceException(shopRes.getError());
        }
        Shop shop = shopRes.getResult();
        buyingActivityDefinition.setShopId(shop.getId());
        buyingActivityDefinition.setShopName(shop.getName());
        buyingActivityDefinition.setSellerId(userId);
        buyingActivityDefinition.setSellerName(shop.getUserName());
        buyingActivityDefinition.setBusinessId(shop.getBusinessId());

        List<BuyingItem> buyingItemList = buyingActivityDto.getItemList();

        if(isNull(buyingItemList)||buyingItemList.size()>15){
            log.error("buying item list length={} out of", buyingItemList.size());
            throw new ServiceException("buying.item.list.length.out.of");
        }


        if(buyingItemList.size()>1){

            final List<Long> itemIds= Lists.transform(buyingItemList, new Function<BuyingItem, Long>() {
                @Override
                public Long apply(BuyingItem buyingItem) {
                    return buyingItem.getItemId();
                }
            });

            for(BuyingItem buyingItem : buyingItemList){
                    if(itemIds.indexOf(buyingItem.getItemId())!=itemIds.lastIndexOf(buyingItem.getItemId())){
                        log.error("create buying item for item(id={}) multiple", buyingItem.getItemId());
                        throw new ServiceException("buying.item.fot.item.id.multiple");
                    }
            }
        }

        Long id = buyingActivityDefinitionDao.create(buyingActivityDefinition);

        for (BuyingItem buyingItem : buyingItemList){


            Response<Item> itemRes = itemService.findById(buyingItem.getItemId());
            if (itemRes.getResult() == null) {
                log.error("item(id={}) mot found", id);
                throw new ServiceException("item.not.found");
            }
            //这里没有判断是否上架
            //商品是否属于卖家
            if (!Objects.equal(userId, itemRes.getResult().getUserId())) {
                log.error("item(id{}) not owner");
                throw new ServiceException("item.not.owner");
            }
            if(buyingItem.getIsStorage()!=null&&buyingItem.getIsStorage()){
                if(!Objects.equal(eHaierSellerId,userId.toString())){
                    log.error("current  user not ehaier");
                    throw new ServiceException("current.user.not.ehaier");
                }
            }
            //当不支持分仓时要验证商品库存
            if(buyingItem.getIsStorage()==null||!buyingItem.getIsStorage()){
                buyingItem.setIsStorage(Boolean.FALSE);
                if(itemRes.getResult().getQuantity()<=0){
                    log.error("item(id={}) stock not enough",id);
                    throw new ServiceException("item.stock.not.enough");
                }
            }

            buyingItem.setBuyingActivityId(id);
            buyingItemDao.create(buyingItem);
        }

        return id;
    }


    @Transactional
    public Boolean update(BuyingActivityDto buyingActivityDto,Long userId){

        BuyingActivityDefinition buyingActivityDefinition =buyingActivityDto.getBuyingActivityDefinition();


        Response<Shop> shopRes = shopService.findByUserId(userId);
        if(!shopRes.isSuccess()){
            throw new ServiceException(shopRes.getError());
        }
        Shop shop = shopRes.getResult();
        buyingActivityDefinition.setShopId(shop.getId());
        buyingActivityDefinition.setShopName(shop.getName());
        buyingActivityDefinition.setSellerId(userId);
        buyingActivityDefinition.setSellerName(shop.getUserName());

        List<BuyingItem> buyingItemList = buyingActivityDto.getItemList();

        Boolean isUpdate  = buyingActivityDefinitionDao.update(buyingActivityDefinition);

        Long id =buyingActivityDto.getBuyingActivityDefinition().getId();

        buyingItemDao.deleteByActivityId(id);   //删除之前的关联

        for (BuyingItem buyingItem : buyingItemList){

            Response<Item> itemRes = itemService.findById(buyingItem.getItemId());
            if (itemRes.getResult() == null) {
                log.error("item(id={}) mot found", id);
                throw new ServiceException("item.not.found");
            }
            if (!Objects.equal(userId, itemRes.getResult().getUserId())) {
                log.error("item(id{}) not owner");
                throw new ServiceException("item.not.owner");
            }

            if(buyingItem.getIsStorage()!=null&&buyingItem.getIsStorage()){
                if(!Objects.equal(eHaierSellerId,userId.toString())){
                    log.error("current  user not ehaier");
                    throw new ServiceException("current.user.not.ehaier");
                }
            }

            //当不支持分仓时要验证商品库存
            if(buyingItem.getIsStorage()==null||(!buyingItem.getIsStorage()&&!Objects.equal(eHaierSellerId,userId))){
                if(itemRes.getResult().getQuantity()<=0){
                    log.error("item(id={}) stock not enough",id);
                    throw new ServiceException("item.stock.not.enough");
                }
            }

                buyingItem.setBuyingActivityId(id);
                buyingItemDao.create(buyingItem);
        }

        return isUpdate;
    }


    @Transactional
    public Boolean updateFakeSoldQuantity(List<BuyingItem> buyingItemList){

        for(BuyingItem buyingItem : buyingItemList){
            buyingItemDao.update(buyingItem);
        }

        return Boolean.TRUE;
    }

    @Transactional
    public Long createBuyingOrder(Order order, OrderItem orderItem,
                                  OrderExtra orderExtra, BuyingOrderRecord buyingOrderRecord,
                                  BuyingTempOrder buyingTempOrderToUpdate, CodeUsage codeUsage) {

        Response<Long> orderIdR = orderWriteService.buyingOrderCreate(order, orderItem, orderExtra);
        if(!orderIdR.isSuccess() || orderIdR.getResult() == null) {
            log.error("fail to create buying order, error code:{}", orderIdR.getError());
            throw new ServiceException(orderIdR.getError());
        }

        Long orderId = orderIdR.getResult();

        buyingOrderRecord.setOrderId(orderId);
        buyingOrderRecordDao.create(buyingOrderRecord);

        buyingTempOrderToUpdate.setOrderId(orderId);
        buyingTempOrderDao.update(buyingTempOrderToUpdate);

        if(codeUsage.getDiscount() != null) {
            codeUsage.setOrderId(orderId);
            codeUsageDao.create(codeUsage);
        }

        return orderId;
    }
}
