package com.aixforce.rrs.code.service;

import com.aixforce.common.model.Response;
import com.aixforce.exception.ServiceException;
import com.aixforce.item.model.Sku;
import com.aixforce.item.service.ItemService;
import com.aixforce.rrs.code.dao.ActivityBindDao;
import com.aixforce.rrs.code.dto.DiscountAndUsage;
import com.aixforce.rrs.code.model.ActivityBind;
import com.aixforce.rrs.code.model.ActivityCode;
import com.aixforce.rrs.code.model.ActivityDefinition;
import com.aixforce.rrs.code.model.CodeUsage;
import com.aixforce.trade.dto.BuyingFatOrder;
import com.aixforce.trade.dto.FatOrder;
import com.aixforce.user.base.BaseUser;
import com.aixforce.user.model.User;
import com.aixforce.user.service.AccountService;
import com.google.common.base.Objects;
import com.google.common.base.Strings;
import com.google.common.base.Throwables;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * 优惠码绑定service
 *
 * Mail: 964393552@qq.com <br>
 * Date: 2014-07-03 PM  <br>
 * Author: songrenfei
 */
@Slf4j
@Service
public class ActivityBindServiceImpl implements ActivityBindService{

    @Autowired
    private ActivityBindDao activityBindDao;

    @Autowired
    private ActivityDefinitionService activityDefinitionService;

    @Autowired
    private ActivityCodeService activityCodeService;

    @Autowired
    private ItemService itemService;

    @Autowired
    private AccountService<User> accountService;

    @Override
    public Response<ActivityBind> create(ActivityBind activityBind) {

        Response<ActivityBind> result = new Response<ActivityBind>();
        if (activityBind == null) {
            log.error("params can not be null");
            result.setError("illegal.param");
            return result;
        }

        try {
            activityBindDao.create(activityBind);
            result.setResult(activityBind);
            return result;
        } catch (Exception e) {
            log.error("failed to create activityBind {}, cause:{}", activityBind, Throwables.getStackTraceAsString(e));
            result.setError("activityBind.create.failed");
            return result;
        }
    }

    @Override
    public Response<ActivityBind> create(Long activityId, Long targetId, Integer targetType) {
        Response<ActivityBind> result = new Response<ActivityBind>();
        ActivityBind cb=null;
        try {
            cb = new ActivityBind();
            cb.setActivityId(activityId);
            cb.setTargetId(targetId);
            cb.setTargetType(targetType);
            return create(cb);

        } catch (Exception e) {
            log.error("failed to create activityBind{}, cause:{}",
                    cb, Throwables.getStackTraceAsString(e));
            result.setError("activityBind.create.failed");
            return result;
        }

    }


    @Override
    public Response<Boolean> update(ActivityBind activityBind) {
        Response<Boolean> result = new Response<Boolean>();
        try {
            result.setResult(activityBindDao.update(activityBind));
            return result;
        } catch (Exception e) {
            log.error("failed to update activityBind {}, cause:{}", activityBind, Throwables.getStackTraceAsString(e));
            result.setError("activityBind.update.failed");
        }
        return result;
    }

    @Override
    public Response<Boolean> delete(Long id) {
        Response<Boolean> result = new Response<Boolean>();
        try {
            result.setResult(activityBindDao.delete(id));
            return result;
        } catch (Exception e) {
            log.error("failed to delete activityBind (id={}), cause:{}", id, Throwables.getStackTraceAsString(e));
            result.setError("activityBind.delete.failed");
        }
        return result;
    }

    @Override
    public Response<ActivityBind> findById(Long id) {
        Response<ActivityBind> result = new Response<ActivityBind>();
        if (id == null) {
            log.error("params can not be null");
            result.setError("illegal.param");
            return result;
        }

        try {
            ActivityBind cb = activityBindDao.findById(id);
            if (cb == null) {
                log.error("no activityBind(id = {}) found", id);
                result.setError("activityBind.not.found");
                return result;
            }
            result.setResult(cb);
            return result;
        } catch (Exception e) {
            log.error("failed to find activityBind(id = {}), cause:{}", id, Throwables.getStackTraceAsString(e));
            result.setError("activityBind.query.failed");
            return result;
        }
    }

    @Override
    public Response<ActivityBind> findByActivityId(Long activityId) {
        Response<ActivityBind> result = new Response<ActivityBind>();

        try {
            ActivityBind cb = activityBindDao.findByActivityId(activityId);
            if (cb == null) {
                log.error("no activityBind(code = {}) found", activityId);
                result.setError("activityBind.not.found");
                return result;
            }
            result.setResult(cb);
            return result;
        } catch (Exception e) {
            log.error("failed to find activityBind(activityId = {}), cause:{}", activityId, Throwables.getStackTraceAsString(e));
            result.setError("activityBind.query.failed");
            return result;
        }

    }

    @Override
    public Response<List<Long>> findBindIdsByActivityId(Long activityId,Integer targetType) {
        Response<List<Long>> result = new Response<List<Long>>();
        if (activityId == null) {
            log.error("param can not be null");
            result.setError("illegal.param");
            return result;
        }

        if (targetType == null) {
            log.error("param can not be null");
            result.setError("illegal.param");
            return result;
        }
        try {
            List<Long> list = activityBindDao.findBindIdsByActivityId(activityId, targetType);
            result.setResult(list);
            return result;

        } catch (Exception e) {
            log.error("failed to find ids where activityId={} and targetType={}. cause:{}",
                    activityId, targetType, Throwables.getStackTraceAsString(e));
            result.setError("find.bind.ids.by.activity.id.failed");
            return result;
        }
    }

    @Override
    public Response<DiscountAndUsage> processOrderCodeDiscount(List<? extends FatOrder> fatOrders, User buyer) {
        Response<DiscountAndUsage> result = new Response<DiscountAndUsage>();

        try {

            Map<Long, Integer> skuIdAndDiscount = Maps.newHashMap();
            Map<Long, CodeUsage> sellerIdAndCodeUsage = Maps.newHashMap();
            Map<Long, Integer> activityCodeIdAndUsage = Maps.newHashMap();

            for (FatOrder fo : fatOrders) {

                String codeName = fo.getCodeName();
                Long activityId = fo.getActivityId();

                //如果没有使用优惠券,直接跳过
                if(!hasUserCode(activityId, codeName)) {
                    continue;
                }

                ActivityDefinition activityDef = getActivityDefinition(activityId);

                //如果活动未生效，直接跳过
                if(!hasActive(activityDef)) {
                    continue;
                }

                List<Long> itemIds = getItemRange(activityId);

                Integer orderOriginPrice = 0;
                Integer orderDiscount = 0;
                Integer totalUsed = 0;

                for (Long skuId : fo.getSkuIdAndQuantity().keySet()) {

                    Integer quantity = fo.getSkuIdAndQuantity().get(skuId);

                    Sku sku = getSku(skuId);

                    //判断改sku是否能参加活动,如果不在参加活动范围直接跳过
                    if(!isSkuLegal(sku, itemIds, activityDef)) {
                        continue;
                    }

                    orderOriginPrice += quantity * sku.getPrice();

                    skuIdAndDiscount.put(skuId, activityDef.getDiscount());

                    orderDiscount += quantity * activityDef.getDiscount();

                    totalUsed += quantity;
                }

                //根据activityId,code 查询使用的总量
                ActivityCode activityCode = getActivityCode(activityId, codeName);


                //验证活动定义库存,如果已经使用的加上这次下单一共的使用次数超过库存，直接返回错误
                checkActivityStock(activityDef, totalUsed, activityCode, activityCodeIdAndUsage);

                //更新优惠码使用数量
                countActivityCodeUsage(activityCode.getId(),totalUsed,activityCodeIdAndUsage);

                User seller = getSeller(fo.getSellerId());

                CodeUsage cu = makeOrderCodeUsage(activityDef, codeName, orderOriginPrice, orderDiscount, totalUsed, buyer, seller);

                sellerIdAndCodeUsage.put(fo.getSellerId(), cu);
            }

            result.setResult(makeDiscountAndUsage(sellerIdAndCodeUsage, skuIdAndDiscount, activityCodeIdAndUsage));
            return result;

        }catch (ServiceException se) {
            result.setError(se.getMessage());
            return result;
        }catch (Exception e) {
            log.error("fail to process order code discount by fatOrders={},buyer id={},cause:{}",
                    fatOrders, buyer.getId(), Throwables.getStackTraceAsString(e));
            result.setError("process.code.discount.fail");
            return result;
        }
    }

    @Override
    public Response<CodeUsage> makeBuyingOrderCodeUsage(BuyingFatOrder buyingFatOrder, BaseUser buyer, Integer buyingPrice) {
        Response<CodeUsage> result = new Response<CodeUsage>();

        try {

            Long activityId = buyingFatOrder.getActivityId();
            String codeName = buyingFatOrder.getCodeName();
            Long skuId = buyingFatOrder.getSkuId();
            Integer quantity = buyingFatOrder.getQuantity();
            Long sellerId = buyingFatOrder.getSellerId();

            ActivityDefinition activityDef = getActivityDefinition(activityId);

            //如果活动未生效，直接跳过
            if(!hasActive(activityDef)) {
                result.setError("code.activity.def.invalid");
                return result;
            }

            List<Long> itemIds = getItemRange(activityId);

            Sku sku = getSku(skuId);

            //判断改sku是否能参加活动,如果不在参加活动范围直接跳过
            if(!isSkuLegal(sku, itemIds, activityDef)) {
                result.setError("code.activity.sku.not.in.range");
                return result;
            }

            //根据activityId,code 查询使用的总量
            ActivityCode activityCode = getActivityCode(activityId, codeName);

            //验证活动定义库存,如果已经使用的加上这次下单一共的使用次数超过库存，直接返回错误
            checkActivityStock(activityDef, quantity, activityCode, Collections.<Long,Integer>emptyMap());

            User seller = getSeller(sellerId);

            Integer orderOriginPrice = buyingPrice * quantity;
            Integer orderDiscount = activityDef.getDiscount() * quantity;

            CodeUsage cu = makeOrderCodeUsage(activityDef, codeName, orderOriginPrice, orderDiscount, quantity, buyer, seller);

            result.setResult(cu);
            return result;

        }catch (ServiceException se) {
            result.setError(se.getMessage());
            return result;
        }catch (Exception e) {
            log.error("fail to make buying order code usage , cause:{}", e);
            result.setError("make.code.usage.fail");
            return result;
        }
    }

    private boolean hasUserCode(Long activityId, String codeName) {
        if(activityId == null || Strings.isNullOrEmpty(codeName)) {
            log.warn("activityId and code both can not be null, skip directly");
            return false;
        }
        return true;
    }

    private ActivityDefinition getActivityDefinition(Long activityId) {
        Response<ActivityDefinition> activityDefR = activityDefinitionService.findActivityDefinitionById(activityId);
        if (!activityDefR.isSuccess()) {
            log.error("fail to find activityDef by id={},error code={},skip it",
                    activityId, activityDefR.getError());
            throw new ServiceException(activityDefR.getError());
        }
        return activityDefR.getResult();
    }

    private boolean hasActive(ActivityDefinition activityDefinition) {
        if(!Objects.equal(activityDefinition.getStatus(), ActivityDefinition.Status.OK.toNumber())) {
            log.warn("activityDef id={}, is invalidate", activityDefinition.getId());
            return false;
        }
        return true;
    }

    private List<Long> getItemRange(Long activityId) {
        Response<List<Long>> itemIdsR = findBindIdsByActivityId(activityId, ActivityBind.TargetType.ITEM.toNumber());
        if(!itemIdsR.isSuccess()) {
            log.error("fail to find bind ids by activityId={}, targetType=ITEM, error code={}",
                    activityId, itemIdsR.getError());
            throw new ServiceException(itemIdsR.getError());
        }
        return itemIdsR.getResult();
    }

    private boolean isSkuLegal(Sku sku, List<Long> itemIds, ActivityDefinition activityDef) {
        if(!itemIds.contains(sku.getItemId())) {
            log.warn("sku {} can not use code, activityDef id={}, bindIds={}, skip it",
                    sku, activityDef.getId(), itemIds);
            return false;
        }

        if (sku.getPrice() <= activityDef.getDiscount()) {
            log.error("discount={} can not lower than sku price={},id={}, skip it",
                    activityDef.getDiscount(), sku.getPrice(), sku.getId());
            return false;
        }

        return true;
    }

    private Sku getSku(Long skuId) {
        Response<Sku> skuR = itemService.findSkuById(skuId);
        if (!skuR.isSuccess()) {
            log.error("fail to find sku by id={},error code:{},skip it", skuId, skuR.getError());
            throw new ServiceException(skuR.getError());
        }
        return skuR.getResult();
    }

    private ActivityCode getActivityCode(Long activityId, String codeName) {
        Response<ActivityCode> activityCodeR = activityCodeService.findOneByActivityIdAndCode(activityId, codeName);
        if(!activityCodeR.isSuccess()) {
            log.error("fail to find usage by activityId={}, code={}, error code:{}, skip it",
                    activityId, codeName, activityCodeR.getError());
            throw new ServiceException(activityCodeR.getError());
        }
        return activityCodeR.getResult();
    }

    private User getSeller(Long sellerId) {
        Response<User> sellerR = accountService.findUserById(sellerId);
        if (!sellerR.isSuccess()) {
            log.error("fail to find user by id={},error code:{},skip it"
                    , sellerId, sellerR.getError());
            throw new ServiceException("seller.not.found");
        }
        return sellerR.getResult();
    }

    private DiscountAndUsage makeDiscountAndUsage(Map<Long, CodeUsage> sellerIdAndCodeUsage,
                                                  Map<Long, Integer> skuIdAndDiscount,
                                                  Map<Long, Integer> activityCodeIdAndUsage) {
        DiscountAndUsage discountAndUsage = new DiscountAndUsage();

        discountAndUsage.setSellerIdAndUsage(sellerIdAndCodeUsage);
        discountAndUsage.setSkuIdAndDiscount(skuIdAndDiscount);
        discountAndUsage.setActivityCodeIdAndUsage(activityCodeIdAndUsage);

        return discountAndUsage;
    }

    /**
     * 根据活动id 删除绑定
     * @param activityId 活动id
     */
    @Override
    public Response<Boolean> deleteActivityBindByActivityId(Long activityId) {
        Response<Boolean> result = new Response<Boolean>();

        if (activityId == null) {
            log.error("params can not be null");
            result.setError("illegal.param");
            return result;
        }
        try {
            result.setResult(activityBindDao.deleteActivityBindByActivityId(activityId));
            return result;
        } catch (Exception e) {
            log.error("failed to delete activityBind (activityId={}), cause:{}", activityId, Throwables.getStackTraceAsString(e));
            result.setError("activityBind.delete.failed");
        }
        return result;
    }

    private void checkActivityStock(ActivityDefinition activityDef, Integer toBuy, ActivityCode activityCode, Map<Long,Integer> activityCodeIdAndUsage) {
        //如果库存为空代表购买无限制
        if(activityDef.getStock() == null) {
            return;
        }
        Long activityId = activityDef.getId();

        //统计已经入库的使用数量
        int usage = activityCode.getUsage() != null ? activityCode.getUsage() : 0;

        int tempUsage = 0;
        //统计还没有入库，但是这一次提交的订单中使用相同优惠码的数量
        if(!activityCodeIdAndUsage.isEmpty()) {
            for(Long activityCodeId : activityCodeIdAndUsage.keySet()) {
                //如果有同一个码在这次订单提交中被使用了
                if(Objects.equal(activityCodeId, activityCode.getId())) {
                    tempUsage = activityCodeIdAndUsage.get(activityCodeId);
                }
            }
        }

        //如果已经使用数量+这个订单使用数量+这次提交中这个优惠码已经使用的数量(还没有入库) > 库存，返回false，前台提示错误
        if((usage+toBuy+tempUsage) > activityDef.getStock()) {
            log.warn("activityDef id={} stock {}, want to buy {}, has used {}, temp used={}",
                    activityId, activityDef.getStock(), toBuy, usage, tempUsage);
            throw new ServiceException("stock.not.enough");
        }
    }

    private CodeUsage makeOrderCodeUsage(ActivityDefinition activityDef, String codeName, Integer orderOriginPrice,
                                    Integer orderDiscount, Integer totalUsed, BaseUser buyer, User seller) {
        //需要记录使用记录,orderId 在创建订单后填进去
        CodeUsage cu = new CodeUsage();
        cu.setActivityId(activityDef.getId());
        cu.setCode(codeName);
        cu.setDiscount(orderDiscount);
        cu.setBusinessId(activityDef.getBusinessId());
        cu.setBuyerId(buyer.getId());
        cu.setBuyerName(buyer.getName());
        cu.setSellerId(seller.getId());
        cu.setSellerName(seller.getName());
        cu.setUsedCount(totalUsed);
        cu.setActivityName(activityDef.getActivityName());
        cu.setDiscount(orderDiscount);
        cu.setOriginPrice(orderOriginPrice);
        cu.setPrice(orderOriginPrice - orderDiscount);
        cu.setActivityType(activityDef.getActivityType());
        cu.setChannelType(activityDef.getChannelType());
        cu.setUsedAt(DateTime.now().toDate());
        return cu;
    }

    private void countActivityCodeUsage(Long acitivityCodeId, Integer totalUsed, Map<Long, Integer> activityCodeIdAndUsage) {
        // 更新使用数量
        Integer activityCodeUsage = activityCodeIdAndUsage.get(acitivityCodeId);
        if(activityCodeUsage == null) {
            activityCodeIdAndUsage.put(acitivityCodeId, totalUsed);
        }else {
            activityCodeUsage += totalUsed;
            activityCodeIdAndUsage.put(acitivityCodeId, activityCodeUsage);
        }
    }
}
