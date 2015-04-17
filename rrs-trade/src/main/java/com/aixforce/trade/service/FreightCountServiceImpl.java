package com.aixforce.trade.service;

import com.aixforce.common.model.Response;
import com.aixforce.item.model.Item;
import com.aixforce.item.model.ItemDetail;
import com.aixforce.item.service.ItemService;
import com.aixforce.trade.dao.FreightModelDao;
import com.aixforce.trade.manager.FreightModelManager;
import com.aixforce.trade.model.FreightModel;
import com.aixforce.trade.model.OrderItem;
import com.aixforce.trade.model.UserTradeInfo;
import com.aixforce.user.base.BaseUser;
import com.aixforce.user.base.UserUtil;
import com.google.common.base.Objects;
import com.google.common.base.Throwables;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Desc:运费计算信息处理
 * Mail:v@terminus.io
 * Created by Michael Zhao
 * Date:2014-04-22.
 */
@Slf4j
@Service
public class FreightCountServiceImpl implements FreightCountService {
    @Autowired
    private FreightModelManager freightModelManager;

    @Autowired
    private FreightModelDao freightModelDao;

    @Autowired
    private ItemService itemService;

    @Autowired
    private OrderQueryService orderQueryService;

    @Autowired
    private UserTradeInfoService userTradeInfoService;

    @Autowired
    private FreightModelService freightModelService;

    //为了解决计算运费时需要多次调用运费模版计算运费所以设置一个cache（设定60秒）
    private final LoadingCache<Long , FreightModel> modelCache = CacheBuilder.newBuilder().expireAfterAccess(60 , TimeUnit.SECONDS).build(
        new CacheLoader<Long, FreightModel>() {
            @Override
            public FreightModel load(Long modelId) throws Exception {
                //查询优惠券模版
                return freightModelDao.findById(modelId);
            }
        }
    );

    @Override
    public Response<Integer> countDefaultFee(Integer addressId, Long itemId, Integer itemNum) {
        Response<Integer> result = new Response<Integer>();

        if(addressId == null){
            log.error("count freight fee need addressId, itemId={}", itemId);
            result.setError("freight.model.addressId.null");
            return result;
        }

        if(itemId == null){
            log.error("count freight fee need itemId");
            result.setError("freight.model.itemId.null");
            return result;
        }

        if(itemNum == null || itemNum <= 0){
            log.error("count freight fee need itemNum");
            result.setError("freight.model.itemNum.error");
            return result;
        }

        try{
            Response<Item> itemRes = itemService.findById(itemId);
            if(!itemRes.isSuccess()){
                log.error("find item failed, itemId={}, error code={}", itemId, itemRes.getError());
                result.setError(itemRes.getError());
                return result;
            }

            //商品对象
            Item item = itemRes.getResult();

            //获取运费模板
            FreightModel freightModel;
            //为旧的没有绑定运费模板的数据默认一个系统包邮模板
            if(Objects.equal(ItemService.defaultModelId , item.getFreightModelId()) || item.getFreightModelId() == null){
                //获取默认的运费模板
                freightModel = freightModelService.findDefaultModel().getResult();
            }else{
                freightModel = modelCache.get(item.getFreightModelId());
            }

            if(freightModel == null){
                log.error("can't find freight model, freightModelId={}", item.getFreightModelId());
                result.setError("freight.model.find.failed");
                return result;
            }

            if(Objects.equal(FreightModel.CostWay.from(freightModel.getCostWay()) , FreightModel.CostWay.BEAR_SELLER)){
                //卖家承担运费
                result.setResult(0);
            }else{
                //商家承担运费
                Response<ItemDetail> itemDetailR = itemService.findDetailBy(item.getId());
                if(!itemDetailR.isSuccess()) {
                    log.error("fail to find itemDetail by itemId={}, error code={}", item.getId(), itemDetailR.getError());
                }
                ItemDetail itemDetail = itemDetailR.getResult();
                result.setResult(freightModelManager.countFee(addressId , freightModel, itemDetail, itemNum));
            }
        }catch(Exception e){
            log.error("count freight fee failed, addressId={}, itemId={}, cause: {}" , addressId, itemId, Throwables.getStackTraceAsString(e));
            result.setError("freight.model.count.failed");
        }

        return result;
    }

    @Override
    public Response<Integer> countDefaultFee(Long userTradeInfoId, Long itemId, Integer itemNum) {
        Response<Integer> result = new Response<Integer>();

        //运费地址
        Integer addressId = null;
        if(userTradeInfoId == null){
            //获取默认的用户物流地址
            BaseUser user = UserUtil.getCurrentUser();
            if(user != null){
                //获取用户的默认收获地址
                Response<List<UserTradeInfo>> addressList = userTradeInfoService.findTradeInfosByUserId(user.getId());
                if(!addressList.isSuccess()){
                    log.error("find user trade address information failed, userId={}, error code={}", user.getId(), addressList.getError());
                    result.setError(addressList.getError());
                    return result;
                }
                if(addressList.getResult().isEmpty()){
                    log.warn("user default trade address is empty.");
                    result.setError("user don't have a trade address.");
                    return result;
                }else{
                    //现在是默认地址到省份
                    for(UserTradeInfo userTradeInfo : addressList.getResult()){
                        if(userTradeInfo.getIsDefault()  == 1){
                            //默认收货地址
                            addressId = userTradeInfo.getProvinceCode();
                            break;
                        }
                    }
                }
            }
        }else{
            Response<UserTradeInfo> tradeRes = userTradeInfoService.findById(userTradeInfoId);
            if(!tradeRes.isSuccess()){
                log.error("find user trade address information failed, userTradeInfoId={}, error code={}", userTradeInfoId, tradeRes.getError());
                result.setError(tradeRes.getError());
                return result;

            }
            addressId = tradeRes.getResult().getProvinceCode();
        }

        return countDefaultFee(addressId , itemId, itemNum);
    }

    @Override
    public Response<Integer> countFeeByOrder(Integer addressId, Long orderId) {
        Response<Integer> result = new Response<Integer>();

        if(addressId == null){
            log.error("count order freight fee need addressId");
            result.setError("freight.model.addressId.null");
            return result;
        }

        if(orderId == null){
            log.error("count order freight fee need orderId");
            result.setError("freight.model.orderId.null");
            return result;
        }

        try{
            //获取子订单信息
            Response<List<OrderItem>> orderItemRes = orderQueryService.findOrderItemByOrderId(orderId);
            if(!orderItemRes.isSuccess()){
                log.error("find order item failed , orderId={}, error code={}", orderId , orderItemRes.getError());
                result.setError(orderItemRes.getError());
                return result;
            }

            Integer allFee = 0;
            for(OrderItem orderItem : orderItemRes.getResult()){
                Response<Integer> countRes = countDefaultFee(addressId , orderItem.getItemId(), orderItem.getQuantity());
                if(countRes.isSuccess()){
                    log.error("count order item fee failed, addressId={}, itemId={}, quantity={}, error code={}"
                            , addressId, orderItem.getItemId(), orderItem.getQuantity(), countRes.getError());
                    result.setError(countRes.getError());
                }

                allFee += countRes.getResult();
            }
            result.setResult(allFee);
        }catch(Exception e){
            log.error("count order freight fee failed, addressId={}, orderId={}, cause: {}", addressId, orderId, Throwables.getStackTraceAsString(e));
            result.setError("freight.model.count.failed");
        }

        return result;
    }

    @Override
    public Response<Integer> countOrderFee(Long userTradeInfoId, Long orderId) {
        Response<Integer> result = new Response<Integer>();

        if(userTradeInfoId == null){
            log.error("count order freight fee need user trade infoId");
            result.setError("freight.model.userTradeInfo.null");
            return result;
        }

        if(orderId == null){
            log.error("count order freight fee need orderId");
            result.setError("freight.model.orderId.null");
            return result;
        }

        //现在默认的是计算省份
        Response<UserTradeInfo> response = userTradeInfoService.findById(userTradeInfoId);
        if(!response.isSuccess()){
            log.error("find user trade info failed , userTradeInfoId={} , error code={}", userTradeInfoId, response.getError());
            result.setError(response.getError());
            return result;
        }
        Integer addressId = response.getResult().getProvinceCode();

        return countFeeByOrder(addressId , orderId);
    }

    @Override
    public Response<Integer> countFeeByOrderItem(Integer addressId, Long orderItemId) {
        Response<Integer> result = new Response<Integer>();

        if(addressId == null){
            log.error("count freight fee need addressId");
            result.setError("freight.model.addressId.null");
            return result;
        }

        if(orderItemId == null){
            log.error("count freight fee need order itemId");
            result.setError("freight.model.orderItemId.null");
            return result;
        }

        //获取子订单信息
        Response<OrderItem> itemRes = orderQueryService.findOrderItemById(orderItemId);
        if(!itemRes.isSuccess()){
            log.error("find order item failed, orderItemId={}, error code={}", orderItemId, itemRes.getError());
            result.setError(itemRes.getError());
            return result;
        }

        return countDefaultFee(addressId , itemRes.getResult().getItemId(), itemRes.getResult().getQuantity());
    }

    @Override
    public Response<Integer> countOrderItemFee(Long userTradeInfoId, Long orderItemId) {
        Response<Integer> result = new Response<Integer>();

        if(userTradeInfoId == null){
            log.error("count order freight fee need user trade infoId");
            result.setError("freight.model.userTradeInfo.null");
            return result;
        }

        if(orderItemId == null){
            log.error("count freight fee need order itemId");
            result.setError("freight.model.orderItemId.null");
            return result;
        }

        //现在默认的是计算省份
        Response<UserTradeInfo> response = userTradeInfoService.findById(userTradeInfoId);
        if(!response.isSuccess()){
            log.error("find user trade info failed , userTradeInfoId={} , error code={}", userTradeInfoId, response.getError());
            result.setError(response.getError());
            return result;
        }
        Integer addressId = response.getResult().getProvinceCode();

        return countFeeByOrderItem(addressId , orderItemId);
    }

    @Override
    public Integer countFeeByItem(Integer addressId, Item item, Integer itemNum) {
        Integer countFee = 0;

        try{
            //获取运费模板(没有绑定运费模版就默认包邮模版)
            FreightModel freightModel = item.getFreightModelId() != null ?
                    modelCache.get(item.getFreightModelId()) : freightModelService.findDefaultModel().getResult();

            if(Objects.equal(-1L, item.getFreightModelId())){  //对于包邮模板, 直接返回运费0;
                return 0;
            }

            if(Objects.equal(FreightModel.CostWay.from(freightModel.getCostWay()) , FreightModel.CostWay.BEAR_SELLER)){
                //卖家承担运费
                countFee = 0;
            }else{
                //商家承担运费
                Response<ItemDetail> itemDetailR = itemService.findDetailBy(item.getId());
                if(!itemDetailR.isSuccess()) {
                    log.error("fail to find itemDetail by itemId={}, error code={}", item.getId(), itemDetailR.getError());
                }
                ItemDetail itemDetail = itemDetailR.getResult();

                countFee = freightModelManager.countFee(addressId , freightModel, itemDetail, itemNum);
            }
        }catch(Exception e){
            log.error("count freight fee by item failed , addressId={}, item={}, itemNum={}, cause: {}",
                    addressId, item, itemNum, Throwables.getStackTraceAsString(e));
        }

        return countFee;
    }
}

