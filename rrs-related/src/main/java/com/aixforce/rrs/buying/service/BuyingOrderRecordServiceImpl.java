package com.aixforce.rrs.buying.service;

import com.aixforce.annotations.ParamInfo;
import com.aixforce.common.model.PageInfo;
import com.aixforce.common.model.Paging;
import com.aixforce.common.model.Response;
import com.aixforce.rrs.buying.dao.BuyingOrderRecordDao;
import com.aixforce.rrs.buying.dto.BuyingActivityOrderDto;
import com.aixforce.rrs.buying.model.BuyingOrderRecord;
import com.aixforce.trade.model.Order;
import com.aixforce.trade.service.OrderQueryService;
import com.aixforce.user.model.User;
import com.aixforce.user.service.AccountService;
import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Nullable;

import java.util.List;
import java.util.Map;

import static com.aixforce.common.utils.Arguments.isNull;
import static com.aixforce.common.utils.Arguments.isNullOrEmpty;
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;

/**
 * 抢购活动下单记录service
 *
 * Mail: 964393552@qq.com <br>
 * Date: 2014-09-23 PM  <br>
 * Author: songrenfei
 */
@Slf4j
@Service
public class BuyingOrderRecordServiceImpl implements BuyingOrderRecordService{

    @Autowired
    private BuyingOrderRecordDao buyingOrderRecordDao;

    @Autowired
    private OrderQueryService orderQueryService;

    @Autowired
    private AccountService accountService;

    @Override
    public Response<BuyingOrderRecord> create(BuyingOrderRecord buyingOrderRecord) {

        Response<BuyingOrderRecord> result = new Response<BuyingOrderRecord>();

        try {
            checkArgument(!isNull(buyingOrderRecord),"illegal.param");
            buyingOrderRecordDao.create(buyingOrderRecord);
            result.setResult(buyingOrderRecord);
            return result;
        }catch (IllegalArgumentException e){
            log.error("params can not be null");
            result.setError(e.getMessage());
            return result;
        }catch (Exception e) {
            log.error("failed to create buyingOrderRecord {}, cause:{}", buyingOrderRecord, Throwables.getStackTraceAsString(e));
            result.setError("buying.order.record.create.failed");
            return result;
        }
    }

    @Override
    public Response<Boolean> update(BuyingOrderRecord buyingOrderRecord) {
        Response<Boolean> result = new Response<Boolean>();
        try {
            checkArgument(!isNull(buyingOrderRecord),"illegal.param");
            checkArgument(!isNull(buyingOrderRecord.getId()),"illegal.param");
            result.setResult(buyingOrderRecordDao.update(buyingOrderRecord));
            return result;
        }catch (IllegalArgumentException e){
            log.error("params can not be null");
            result.setError(e.getMessage());
            return result;
        }catch (Exception e) {
            log.error("failed to update buyingOrderRecord {}, cause:{}", buyingOrderRecord, Throwables.getStackTraceAsString(e));
            result.setError("buying.order.record.update.failed");
        }
        return result;
    }

    @Override
    public Response<Boolean> delete(Long id) {
        Response<Boolean> result = new Response<Boolean>();
        try {
            checkArgument(!isNull(id),"illegal.param");
            result.setResult(buyingOrderRecordDao.delete(id));
            return result;
        }catch (IllegalArgumentException e){
            log.error("params can not be null");
            result.setError(e.getMessage());
            return result;
        }catch (Exception e) {
            log.error("failed to delete buyingOrderRecord (id={}), cause:{}", id, Throwables.getStackTraceAsString(e));
            result.setError("buying.order.record.delete.failed");
        }
        return result;
    }

    @Override
    public Response<BuyingOrderRecord> findById(Long id) {
        Response<BuyingOrderRecord> result = new Response<BuyingOrderRecord>();
        try {
            checkArgument(!isNull(id),"illegal.param");
            BuyingOrderRecord bd = buyingOrderRecordDao.findById(id);
            checkState(!isNull(bd), "buying.order.record.not.found");
            result.setResult(bd);
            return result;
        }catch (IllegalArgumentException e){
            log.error("params can not be null");
            result.setError(e.getMessage());
            return result;
        }catch (IllegalStateException e){
            log.error("failed to find buyingOrderRecord(id = {}),error:{}",id,e.getMessage());
            result.setError(e.getMessage());
            return result;
        }catch (Exception e) {
            log.error("failed to find buyingOrderRecord(id = {}), cause:{}", id, Throwables.getStackTraceAsString(e));
            result.setError("buying.order.record.query.failed");
            return result;
        }
    }

    @Override
    public Response<Paging<BuyingActivityOrderDto>> getBuyingActivityOrderDtoByActivityId(@ParamInfo("activityId") @Nullable String activityId,
                                                                                          @ParamInfo("orderId") @Nullable Long orderId,
                                                                                          @ParamInfo("itemId") @Nullable Long itemId,
                                                                                          @ParamInfo("pageNo") @Nullable Integer pageNo,
                                                                                          @ParamInfo("size") @Nullable Integer size) {
        PageInfo pageInfo = new PageInfo(pageNo, size);
        Response<Paging<BuyingActivityOrderDto>> result = new Response<Paging<BuyingActivityOrderDto>>();
        try {
            checkArgument(!isNull(activityId),"illegal.param");
            Map<String, Object> params = Maps.newHashMap();
            params.put("offset", pageInfo.offset);
            params.put("limit", pageInfo.limit);

            params.put("activityId",activityId);

            if(!isNull(orderId)){
                params.put("orderId",orderId);
            }
            if(!isNull(itemId)){
                params.put("itemId",itemId);
            }

            Paging<BuyingOrderRecord> pagingResult =  buyingOrderRecordDao.getByActivityId(params);

            if(pagingResult.getTotal()==0){
                result.setResult(Paging.empty(BuyingActivityOrderDto.class));
                return result;
            }

            List<BuyingOrderRecord> buyingOrderRecordsList = pagingResult.getData();
            List<BuyingActivityOrderDto> buyingActivityOrderDtoList = Lists.newArrayList();
            for (BuyingOrderRecord buyingOrderRecord : buyingOrderRecordsList) {
                Order order = orderQueryService.findById(buyingOrderRecord.getOrderId()).getResult();
                BuyingActivityOrderDto buyingActivityOrderDto = new BuyingActivityOrderDto();
                if (order != null) {
                    buyingActivityOrderDto.setItemId(buyingOrderRecord.getItemId());
                    buyingActivityOrderDto.setDiscount(buyingOrderRecord.getDiscount());
                    buyingActivityOrderDto.setOriginPrice(buyingOrderRecord.getItemOriginPrice());
                    buyingActivityOrderDto.setPrice(buyingOrderRecord.getItemBuyingPrice());
                    buyingActivityOrderDto.setCreatedAt(order.getCreatedAt());
                    buyingActivityOrderDto.setOrderId(order.getId());
                    buyingActivityOrderDto.setStatus(order.getStatus());
                    buyingActivityOrderDto.setBuyerId(order.getBuyerId());
                    buyingActivityOrderDto.setName(((User)accountService.findUserById(order.getBuyerId()).getResult()).getName());
                }
                buyingActivityOrderDtoList.add(buyingActivityOrderDto);
            }
            result.setResult(new Paging<BuyingActivityOrderDto>(pagingResult.getTotal(), buyingActivityOrderDtoList));

        }catch (IllegalArgumentException e){
            log.error("param can not null");
            result.setError(e.getMessage());
        }catch (Exception e){
            log.error("failed to find BuyingActivityOrderDto by activityId {}, cause:{}", activityId, Throwables.getStackTraceAsString(e));
            result.setError("get.BuyingActivityOrderDto.by.activityId.query.failed");
        }
        return result;
    }

    @Override
    public Response<Boolean> updateOrderId(Long oldId, Long newId) {
        Response<Boolean> result = new Response<Boolean>();
        try {
            checkArgument(!isNull(oldId),"illegal.param");
            checkArgument(!isNull(newId),"illegal.param");
            buyingOrderRecordDao.updateOrderId(oldId,newId);
            result.setResult(Boolean.TRUE);
        }catch (IllegalArgumentException e){
            log.error("update buyingOrderRecord set newOrderId={},oldOrderId={} fail,error={}",newId,oldId,e.getMessage());
            result.setError(e.getMessage());
        }catch (Exception e){
            log.error("update buyingOrderRecord set newOrderId={},oldOrderId={} fail,cause:{}",newId,oldId,Throwables.getStackTraceAsString(e));
            result.setError("buying.order.record.update.failed");
        }
        return result;
    }

}
