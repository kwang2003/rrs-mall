package com.aixforce.rrs.buying.service;

import com.aixforce.annotations.ParamInfo;
import com.aixforce.common.model.PageInfo;
import com.aixforce.common.model.Paging;
import com.aixforce.common.model.Response;
import com.aixforce.exception.JsonResponseException;
import com.aixforce.exception.ServiceException;
import com.aixforce.item.model.Item;
import com.aixforce.item.model.Sku;
import com.aixforce.item.service.ItemService;
import com.aixforce.rrs.buying.dao.BuyingActivityDefinitionDao;
import com.aixforce.rrs.buying.dao.BuyingItemDao;
import com.aixforce.rrs.buying.dao.BuyingTempInvalidCodeDao;
import com.aixforce.rrs.buying.dao.BuyingTempOrderDao;
import com.aixforce.rrs.buying.dto.BuyingTempOrderDto;
import com.aixforce.rrs.buying.manager.BuyingTempOrderManger;
import com.aixforce.rrs.buying.model.BuyingActivityDefinition;
import com.aixforce.rrs.buying.model.BuyingItem;
import com.aixforce.rrs.buying.model.BuyingTempOrder;
import com.aixforce.rrs.grid.service.GridService;
import com.aixforce.rrs.presale.service.PreSaleService;
import com.aixforce.trade.model.UserTradeInfo;
import com.aixforce.trade.service.UserTradeInfoService;
import com.aixforce.user.base.BaseUser;
import com.aixforce.user.model.User;
import com.aixforce.user.service.AccountService;
import com.google.common.base.Objects;
import com.google.common.base.Strings;
import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Nullable;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static com.aixforce.common.utils.Arguments.*;
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;

/**
 * 抢购活动模拟订单service
 *
 * Mail: 964393552@qq.com <br>
 * Date: 2014-09-23 PM  <br>
 * Author: songrenfei
 */
@Slf4j
@Service
public class BuyingTempOrderServiceImpl implements BuyingTempOrderService{

    @Autowired
    private BuyingTempOrderDao buyingTempOrderDao;

    @Autowired
    private BuyingItemDao buyingItemDao;

    @Autowired
    private BuyingActivityDefinitionDao buyingActivityDefinitionDao;

    @Autowired
    private BuyingTempOrderManger buyingTempOrderManger;

    @Autowired
    private PreSaleService preSaleService;

    @Autowired
    private UserTradeInfoService userTradeInfoService;

    @Autowired
    private ItemService itemService;

    @Autowired
    private GridService gridService;

    @Autowired
    private AccountService<User> accountService;

    private static final DateTimeFormatter DATE_TIME_FORMAT = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");

    @Autowired
    private BuyingTempInvalidCodeDao buyingTempInvalidCodeDao;


    @Override
    public Response<BuyingTempOrder> create(BuyingTempOrder buyingTempOrder) {

        Response<BuyingTempOrder> result = new Response<BuyingTempOrder>();

        try {
            checkArgument(!isNull(buyingTempOrder),"illegal.param");
            Long id = buyingTempOrderDao.create(buyingTempOrder);
            buyingTempOrder.setId(id);
            result.setResult(buyingTempOrder);
            return result;
        }catch (IllegalArgumentException e){
            log.error("params can not be null");
            result.setError(e.getMessage());
            return result;
        }catch (Exception e) {
            log.error("failed to create buyingTempOrder {}, cause:{}", buyingTempOrder, Throwables.getStackTraceAsString(e));
            result.setError("buying.temp.order.create.failed");
            return result;
        }
    }



    @Override
    public Response<Boolean> update(BuyingTempOrder buyingTempOrder) {
        Response<Boolean> result = new Response<Boolean>();
        try {
            checkArgument(!isNull(buyingTempOrder),"illegal.param");
            checkArgument(!isNull(buyingTempOrder.getId()),"illegal.param");
            result.setResult(buyingTempOrderDao.update(buyingTempOrder));
            return result;
        }catch (IllegalArgumentException e){
            log.error("params can not be null");
            result.setError(e.getMessage());
            return result;
        }catch (Exception e) {
            log.error("failed to update buyingTempOrder {}, cause:{}", buyingTempOrder, Throwables.getStackTraceAsString(e));
            result.setError("buying.temp.order.update.failed");
        }
        return result;
    }



    @Override
    public Response<Boolean> delete(Long id) {
        Response<Boolean> result = new Response<Boolean>();
        try {
            checkArgument(!isNull(id),"illegal.param");
            result.setResult(buyingTempOrderDao.delete(id));
            return result;
        }catch (IllegalArgumentException e){
            log.error("params can not be null");
            result.setError(e.getMessage());
            return result;
        }catch (Exception e) {
            log.error("failed to delete buyingTempOrder (id={}), cause:{}", id, Throwables.getStackTraceAsString(e));
            result.setError("buying.temp.order.delete.failed");
        }
        return result;
    }

    @Override
    public Response<BuyingTempOrder> findById(Long id) {
        Response<BuyingTempOrder> result = new Response<BuyingTempOrder>();
        try {
            checkArgument(!isNull(id),"illegal.param");
            BuyingTempOrder bd = buyingTempOrderDao.findById(id);
            checkState(!isNull(bd), "buying.temp.order.not.found");
            result.setResult(bd);
            return result;
        }catch (IllegalArgumentException e){
            log.error("params can not be null");
            result.setError(e.getMessage());
            return result;
        }catch (IllegalStateException e){
            log.error("failed to find buyingTempOrder(id = {}),error:{}",id,e.getMessage());
            result.setError(e.getMessage());
            return result;
        }catch (Exception e) {
            log.error("failed to find buyingTempOrder(id = {}), cause:{}", id, Throwables.getStackTraceAsString(e));
            result.setError("buying.temp.order.query.failed");
            return result;
        }
    }



    @Override
    public Response<Paging<BuyingTempOrder>> paging(@ParamInfo("itemName") @Nullable String itemName,
                                                    @ParamInfo("id") @Nullable Long id,
                                                    @ParamInfo("buyerId") @Nullable Long buyerId,
                                                    @ParamInfo("shopId") @Nullable Long shopId,
                                                    @ParamInfo("startDate") @Nullable String startDate,
                                                    @ParamInfo("endDate") @Nullable String endDate,
                                                    @ParamInfo("pageNo") @Nullable Integer pageNo,
                                                    @ParamInfo("size") @Nullable Integer size) {

        PageInfo page = new PageInfo(pageNo, size);

        Response<Paging<BuyingTempOrder>> result = new Response<Paging<BuyingTempOrder>>();
        Map<String, Object> params = Maps.newHashMap();
        params.put("offset", page.offset);
        params.put("limit", page.limit);
        if (!Strings.isNullOrEmpty(itemName)) {
            params.put("itemName", itemName);
        }
        if(!isNull(id)){
            params.put("id",id);
        }
        if(notNull(buyerId)){
            params.put("buyerId",buyerId);
        }
        if(notNull(shopId)){
            params.put("shopId",shopId);
        }
        if (!Strings.isNullOrEmpty(startDate)) {
            params.put("startAt", startDate);
        }
        if (!Strings.isNullOrEmpty(endDate)) {
            params.put("endAt", endDate);
        }
        try{

            Paging<BuyingTempOrder> paging = buyingTempOrderDao.paging(params);
            for(BuyingTempOrder buyingTempOrder :paging.getData()){
                buyingTempOrder.setBuyerName(accountService.findUserById(buyingTempOrder.getBuyerId()).getResult().getName());
            }
            result.setResult(paging);
        }catch (Exception e){
            log.error("fail paging BuyingTempOrder by params={},cause:{}",params,Throwables.getStackTraceAsString(e));
            result.setError("fail.paging.buying.temp.order");
        }
        return result;
    }


    @Override
    public Response<BuyingTempOrderDto> pagings(@ParamInfo("itemName") @Nullable String itemName,
                                                @ParamInfo("status") @Nullable Integer status,
                                                @ParamInfo("startDate") @Nullable String startDate,
                                                @ParamInfo("endDate") @Nullable String endDate,
                                                @ParamInfo("pageNo") @Nullable Integer pageNo,
                                                @ParamInfo("size") @Nullable Integer size,
                                                @ParamInfo("baseUser") BaseUser baseUser) {

        PageInfo page = new PageInfo(pageNo, size);

        Response<BuyingTempOrderDto> result = new Response<BuyingTempOrderDto>();
        BuyingTempOrderDto buyingTempOrderDto = new BuyingTempOrderDto();
        Map<String, Object> params = Maps.newHashMap();
        params.put("offset", page.offset);
        params.put("limit", page.limit);
        if (!Strings.isNullOrEmpty(itemName)) {
            params.put("itemName", itemName);
        }
        if(!isNull(status)){
            params.put("status",status);
        }

        params.put("buyerId", baseUser.getId()); //取当前用户的虚拟订单

        if (!Strings.isNullOrEmpty(startDate)) {
            params.put("startAt", startDate);
        }
        if (!Strings.isNullOrEmpty(endDate)) {
            params.put("endAt", endDate);
        }
        try{

            Paging<BuyingTempOrder> paging = buyingTempOrderDao.paging(params);
            buyingTempOrderDto.setPaging(paging);
            buyingTempOrderDto.setSystemAt(new Date());//系统当前时间
            result.setResult(buyingTempOrderDto);
        }catch (Exception e){
            log.error("fail paging BuyingTempOrder by params={},cause:{}",params,Throwables.getStackTraceAsString(e));
            result.setError("fail.paging.buying.temp.order");
        }
        return result;
    }



    @Override
    public Response<Long> createTempOrder(BuyingTempOrder buyingTempOrder) {
        Response<Long> result = new Response<Long>();

        try {
            checkArgument(!isNull(buyingTempOrder),"illegal.param");
            checkArgument(!isNull(buyingTempOrder.getBuyerId()),"illegal.param");
            checkArgument(!isNull(buyingTempOrder.getItemId()),"illegal.param");
            checkArgument(!isNull(buyingTempOrder.getBuyingActivityId()),"illegal.param");
            checkArgument(!isNull(buyingTempOrder.getSkuQuantity()),"illegal.param");
            checkArgument(!isNull(buyingTempOrder.getSkuId()),"illegal.param");
            checkArgument(!isNull(buyingTempOrder.getTradeInfoId()),"illegal.param");

            final Integer quantity = buyingTempOrder.getSkuQuantity();
            final Long itemId = buyingTempOrder.getItemId();
            final Long activityId =buyingTempOrder.getBuyingActivityId();
            final Long userId =buyingTempOrder.getBuyerId();
            final Integer skuQuantity =buyingTempOrder.getSkuQuantity();
            final Long skuId = buyingTempOrder.getSkuId();
            final Long tradeInfoId =buyingTempOrder.getTradeInfoId();

            //判断抢购活动状态
            BuyingActivityDefinition buyingActivityDefinition = buyingActivityDefinitionDao.findById(activityId);
            if(isNull(buyingActivityDefinition)){
                log.error("buying activity definition not found by id={}",activityId);
                result.setError("buying.activity.definition.not.found");
                return result;
            }
            if(buyingActivityDefinition.getStatus().equals(4)||buyingActivityDefinition.getStatus().equals(5)){
                log.error("buying activity definition id={}, not.valid", activityId);
                result.setError("buying.activity.definition.not.valid");
                return result;
            }
            if(buyingActivityDefinition.getStatus().equals(1)||buyingActivityDefinition.getStatus().equals(2)){
                log.error("buying activity definition id={}, not.start", activityId);
                result.setError("buying.activity.definition.not.start");
                return result;
            }

            Response<UserTradeInfo> resultTrade = userTradeInfoService.findById(tradeInfoId);
            if (!resultTrade.isSuccess()) {
                log.error("failed to find userTradeInfo by id, id={},cause:{}", tradeInfoId, resultTrade.getError());
                result.setError(resultTrade.getError());
                return result;
            }
            Response<Boolean> filterFatOrders = gridService.verifyRegionWhenCreateTempOrder(buyingTempOrder.getItemId(), resultTrade.getResult().getDistrictCode());
            if (!filterFatOrders.isSuccess()) {
                log.error("fail to filter fatOrders, error code:{}", filterFatOrders.getError());
                result.setError(filterFatOrders.getError());
                return result;
            }

            if(!Objects.equal(userId,resultTrade.getResult().getUserId())){
                log.error("address not is current user(id={}),tradeInfo(id={})",userId,tradeInfoId);
                result.setError("address.not.current.user");
                return result;
            }

            buyingTempOrder.setPayLimit(buyingActivityDefinition.getPayLimit());
            buyingTempOrder.setOrderStartAt(buyingActivityDefinition.getOrderStartAt());
            buyingTempOrder.setOrderEndAt(buyingActivityDefinition.getOrderEndAt());


            BuyingItem buyingItem = buyingItemDao.findByActivityIdAnditemId(activityId,itemId);
            checkState(!isNull(buyingItem),"buying.item.not.found");

            buyingTempOrder.setBuyingPrice(buyingItem.getItemBuyingPrice());
            if(!isNull(buyingItem.getBuyLimit())){
                //判断限购数量 当用户分多次购买 当前购买数量+已购买数量<=限购数量
                Integer hasBuyQuantity = buyingTempOrderDao.getHasBuyQuantity(activityId,itemId,userId);
                if(isNull(hasBuyQuantity)){
                    hasBuyQuantity=0;
                }
                //判断限制数量是否合法
                checkState((skuQuantity + hasBuyQuantity)<= buyingItem.getBuyLimit(), "out.of.buy.limit");
            }

            //判断商品是否存在
            Response<Item> itemR = itemService.findById(itemId);
            if (!itemR.isSuccess()) {
                log.error("fail to find item by item id={} when create tempOrder order", itemId);
                result.setError("item.not.found");
                return result;
            }
            Item item = itemR.getResult();

            //如果卖家和买家一样则不创建订单
            if(Objects.equal(item.getUserId(), buyingTempOrder.getBuyerId())) {
                log.error("buyerId can not same as sellerId={}, this order will be ignored", buyingTempOrder.getBuyerId());
                result.setError("can.not.buy.self.item");
                return result;
            }

            Response<Sku> skuRes = itemService.findSkuById(skuId);
            if(!skuRes.isSuccess()){
                log.error("fail find sku by id={} error={}",skuId,skuRes.getError());
                result.setError(skuRes.getError());
                return result;
            }
            Sku sku = skuRes.getResult();



            buyingTempOrder.setSellerId(item.getUserId());
            buyingTempOrder.setItemName(item.getName());
            buyingTempOrder.setItemImage(item.getMainImage());
            buyingTempOrder.setShopId(item.getShopId());
            StringBuilder sb = new StringBuilder();
            if(!Strings.isNullOrEmpty(sku.getAttributeKey1())){
                sb.append(sku.getAttributeKey1() + sku.getAttributeName1() + " ");
            }
            if(!Strings.isNullOrEmpty(sku.getAttributeKey2())){
                sb.append(sku.getAttributeKey2() + sku.getAttributeName2());
            }
            buyingTempOrder.setSkuAttributeJson(sb.toString());

            Boolean isStorage = false;

            //判断库存  支持分仓
            if(notNull(buyingItem.getIsStorage())&&buyingItem.getIsStorage()){
                isStorage = true;
                boolean hasEnoughStock = preSaleService.enoughStock(itemId, resultTrade.getResult().getDistrictCode(), quantity); //引用了预售中得判断逻辑
                if (!hasEnoughStock) { //没有足够的库存
                    log.warn("no enough stock for itemId={}, regionId={}, count={}", itemId, resultTrade.getResult().getDistrictCode(), quantity);
                    result.setError("storage.stock.not.enough");
                    return result;
                }

            }else {
                //商品库存是否>购买数量
                if(item.getQuantity()<skuQuantity){
                    log.error("item(id={}) stock not enough",itemId);
                    result.setError("item.stock.not.enough");
                    return result;
                }

                //sku库存是否>购买数量
                if(sku.getStock()<skuQuantity){
                    log.error("item(id={}) stock not enough",itemId);
                    result.setError("item.stock.not.enough");
                    return result;
                }

            }

            Long id =buyingTempOrderManger.create(buyingTempOrder,isStorage,itemId,skuId,skuQuantity,resultTrade.getResult().getDistrictCode());
            result.setResult(id);
            return result;
        }catch (IllegalArgumentException e){
            log.error("params can not be null");
            result.setError(e.getMessage());
            return result;
        }catch (IllegalStateException e){
            log.error("failed to create buyingTempOrder error={}",e.getMessage());
            result.setError(e.getMessage());
            return result;
        } catch (ServiceException e){
            log.error("storage not found");
            result.setError(e.getMessage());
            return  result;

        }catch(Exception e) {
            log.error("failed to create buyingTempOrder {}, cause:{}", buyingTempOrder, Throwables.getStackTraceAsString(e));
            result.setError("buying.temp.order.create.failed");
            return result;
        }
    }


    @Override
    public Response<Boolean> cancelOrder(BuyingTempOrder buyingTempOrder) {
        Response<Boolean> result = new Response<Boolean>();
        try {
            checkArgument(!isNull(buyingTempOrder),"illegal.param");

            BuyingItem buyingItem = buyingItemDao.findByActivityIdAnditemId(buyingTempOrder.getBuyingActivityId(),buyingTempOrder.getItemId());
            checkState(!isNull(buyingItem), "buying.item.not.found");

            result.setResult(buyingTempOrderManger.cancelOrder(buyingTempOrder,buyingItem));
        }catch (IllegalArgumentException e){
            log.error("params can not be null");
            result.setError(e.getMessage());
            return result;
        }catch (IllegalStateException e){
            log.error("not to find buyingItem by activity(id = {}) and itemId(id={}),error:{}",buyingTempOrder.getBuyingActivityId(),buyingTempOrder.getItemId(),e.getMessage());
            result.setError(e.getMessage());
            return result;
        }catch (ServiceException e){

            result.setError(e.getMessage());

        }catch (Exception e){
            log.error("failed to cancel buyingTempOrder id={}, cause:{}", buyingTempOrder.getId(), Throwables.getStackTraceAsString(e));
            result.setError("buying.temp.order.cancel.failed");
        }
        return result;
    }

    @Override
    public Response<BuyingTempOrder> getByOrderId(Long orderId) {

        Response<BuyingTempOrder> result = new Response<BuyingTempOrder>();
        try {
            checkArgument(!isNull(orderId), "illegal.param");
            BuyingTempOrder buyingTempOrder = buyingTempOrderDao.findByOrderId(orderId);
            checkState(!isNull(buyingTempOrder),"not.find.buying.temp.order.by.order.id");
            result.setResult(buyingTempOrder);
            return result;
        }catch (IllegalArgumentException e){
            log.error("params can not be null");
            result.setError(e.getMessage());
            return result;
        }catch (IllegalStateException e){
            log.error("not.find.buying.temp.order.by.order.id");
            result.setError("not.find.buying.temp.order.by.order.id");
            return  result;

        } catch (Exception e) {
            log.error("failed to find buyingTempOrder by order(id={}), cause:{}", orderId, Throwables.getStackTraceAsString(e));
            result.setError("failed.find.buying.temp.order.by.order.id");
            return result;
        }
    }

    /**
     * 根据订单列表获取虚拟订单信息
     *
     * @param orderIds 订单列表
     * @return 虚拟订单列表
     */
    @Override
    public Response<List<BuyingTempOrder>> findByOrderIds(List<Long> orderIds) {
        Response<List<BuyingTempOrder>> result = new Response<List<BuyingTempOrder>>();

        try {
            if (isNull(orderIds) || isEmpty(orderIds))  {
                List<BuyingTempOrder> empty = Lists.newArrayListWithCapacity(0);
                result.setResult(empty);
            } else {
                checkArgument(orderIds.size() <= 50, "ids.to.long");
                List<BuyingTempOrder> orders = buyingTempOrderDao.findInOrderIds(orderIds);
                result.setResult(orders);
            }

        } catch (IllegalArgumentException e) {
            log.info("fail to query by orderIds:{}, error:{}", orderIds, e.getMessage());
            result.setError(e.getMessage());
        } catch (Exception e) {
            log.info("fail to query by orderIds:{}, cause:{}", orderIds, Throwables.getStackTraceAsString(e));
            result.setError("buying.order.query.fail");
        }

        return result;
    }

    @Override
    public void batchUpdateStatus() {
        log.info("execute updateToOutDate start date={},",DATE_TIME_FORMAT.print(new DateTime(new Date())));
        buyingTempOrderDao.updateToOutDate(new Date());
        log.info("execute updateToOutDate end date={},", DATE_TIME_FORMAT.print(new DateTime(new Date())));
    }


    @Override
    public Response<Boolean> updateOrderId(Long oldId, Long newId) {
        Response<Boolean> result = new Response<Boolean>();
        try {
            checkArgument(!isNull(oldId),"illegal.param");
            checkArgument(!isNull(newId),"illegal.param");
            buyingTempOrderDao.updateOrderId(oldId,newId);
            result.setResult(Boolean.TRUE);
        }catch (IllegalArgumentException e){
            log.error("update buyingTempOrder set newOrderId={},oldOrderId={} fail,error={}",newId,oldId,e.getMessage());
            result.setError(e.getMessage());
        }catch (Exception e){
            log.error("update buyingTempOrder set newOrderId={},oldOrderId={} fail,cause:{}",newId,oldId,Throwables.getStackTraceAsString(e));
            result.setError("buying.temp.order.update.failed");
        }
        return result;
    }

    /**
     * 得到具体用户已抢购的数量
     * @param activityId 活动id
     * @param itemId 商品id
     * @param userId 用户id
     * @return 数量
     */
    @Override
    public Response<Integer> getHasBuyQuantity(Long activityId, Long itemId,Long userId){
        Response<Integer> result = new Response<Integer>();
        try{
            checkArgument(!isNull(activityId),"illegal.param");
            checkArgument(!isNull(itemId),"illegal.param");
            checkArgument(!isNull(userId),"illegal.param");
            Integer buyQuantity =buyingTempOrderDao.getHasBuyQuantity(activityId,itemId,userId);
            result.setResult(buyQuantity);
            return result;
        }catch (IllegalArgumentException e){
            log.error("fail to sum user(id={}) has buy activity(id={}) item(id={}) quantity error:{}",userId,activityId,itemId,e.getMessage());
            result.setError(e.getMessage());
        }catch (Exception e){
            log.error("fail to sum user(id={}) has buy activity(id={}) item(id={}) quantity error:{}",userId,activityId,itemId,e.getMessage());
            result.setError("fail.to.sum.user.has.buy.quantity");
        }
        return result;
    }

    @Override
    public Response<Boolean> checkBuyingOrderId(Long id) {
        Response<Boolean> result = new Response<Boolean>();
        Long buyingOrderId = buyingTempInvalidCodeDao.findByOrderId(id);
        if (null == buyingOrderId) {
            result.setResult(false);
        } else {
            result.setResult(true);
        }
        return result;
    }
}
