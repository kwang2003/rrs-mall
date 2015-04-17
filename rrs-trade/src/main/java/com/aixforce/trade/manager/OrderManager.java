package com.aixforce.trade.manager;

import com.aixforce.agreements.dao.PreAuthorizationDao;
import com.aixforce.agreements.model.PreAuthorizationDepositOrder;
import com.aixforce.common.model.Paging;
import com.aixforce.common.model.Response;
import com.aixforce.common.utils.BeanMapper;
import com.aixforce.common.utils.JsonMapper;
import com.aixforce.exception.ServiceException;
import com.aixforce.item.service.ItemService;
import com.aixforce.shop.model.Shop;
import com.aixforce.shop.service.ShopService;
import com.aixforce.trade.dao.*;
import com.aixforce.trade.dto.*;
import com.aixforce.trade.model.*;
import com.aixforce.trade.service.*;
import com.aixforce.user.enums.Business;
import com.google.common.base.*;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multiset;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static com.aixforce.common.utils.Arguments.*;
import static com.aixforce.common.utils.Arguments.notEmpty;
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;


/**
 * Author:  <a href="mailto:jlchen.cn@gmail.com">jlchen</a>
 * Date: 2013-12-02
 */
@Component
public class OrderManager {

    private static final Logger log = LoggerFactory.getLogger(OrderManager.class);

    @Autowired
    private CartDao cartDao;

    @Autowired
    private OrderDao orderDao;

    @Autowired
    private OrderItemDao orderItemDao;

    @Autowired
    private OrderExtraDao orderExtraDao;

    @Autowired
    private ItemService itemService;

    @Autowired
    private ExpressInfoDao expressInfoDao;

    @Autowired
    private OrderLogisticsInfoDao orderLogisticsInfoDao;

    @Autowired
    private InstallInfoDao installInfoDao;

    @Autowired
    private DeliveryMethodService deliveryMethodService;

    @Autowired
    private UserTradeInfoService userTradeInfoService;

    @Autowired
    private FreightCountService freightCountService;

    @Autowired
    private ShopService shopService;

    @Autowired
    private UserVatInvoiceService userVatInvoiceService;
    

    @Autowired
    private OrderJobOverDayConfigService orderJobOverDayConfigService;

    @Autowired
    private OrderJobOverDayService orderJobOverDayService;

    @Autowired
    private PreAuthorizationDao preAuthorizationDao;

//    @Autowired
//    private PreDepositService preDepositService;

    private static final JsonMapper mapper = JsonMapper.nonDefaultMapper();

    @Transactional
    public OrderResult create(Long buyerId, Long tradeInfoId, List<FatOrder> fatOrders,
                              Map<Long, SkuAndItem> skuAndItems, Map<Long,Integer> skuIdAndDiscount,String bank) throws Exception {
        OrderResult orderResult = new OrderResult();
        Map<Long,Long> sellerIdAndOrderIds = Maps.newHashMap();
        List<StockChange> stockChanges = Lists.newArrayList();
        for (FatOrder fatOrder : fatOrders) {
            //如果卖家和买家一样则不创建订单
            if(Objects.equal(fatOrder.getSellerId(), buyerId)) {
                log.warn("buyerId can not same as sellerId={}, this order will be ignored", buyerId);
                throw new ServiceException("can.not.buy.self.item");
            }

            if(fatOrder.getSkuIdAndQuantity() == null || fatOrder.getSkuIdAndQuantity().isEmpty()) {
                log.error("skuAndQuantity can not be null or empty by fatOrder", fatOrder);
                throw new ServiceException("sku.and.quantity.illegal");
            }

            Response<Shop> shopR = shopService.findByUserId(fatOrder.getSellerId());
            if (!shopR.isSuccess()) {
                log.error("failed to find shop by sellerId:{}, error code :{}", fatOrder.getSellerId(), shopR.getError());
                throw new ServiceException("shop.not.found");
            }
            final Shop shop = shopR.getResult();
            Long businessId = shop.getBusinessId();

            Order order = simpleOrderFactory(buyerId,tradeInfoId,fatOrder,businessId);

            //获取用户邮寄地址，用于计算运费信息
            Response<UserTradeInfo> userTradeInfoRes = userTradeInfoService.findById(tradeInfoId);
            if(!userTradeInfoRes.isSuccess()){
                log.error("find user trade info failed, tradeInfoId={}, error code={}", tradeInfoId, userTradeInfoRes.getError());
            }
            UserTradeInfo userTradeInfo = userTradeInfoRes.getResult();

            //订单总价
            int totalFee = 0;
            //运费总价
            int freightFee = 0;
            //优惠总价
            Integer totalDiscount = 0;

            Map<Long , Integer> itemFreightFees = Maps.newHashMap();

            for (Long skuId : fatOrder.getSkuIdAndQuantity().keySet()) {
                Integer quantity = fatOrder.getSkuIdAndQuantity().get(skuId);
                SkuAndItem skuAndItem = skuAndItems.get(skuId);
                totalFee += quantity * skuAndItem.getSku().getPrice();

                Integer discount = 0;
                if(skuIdAndDiscount != null) {
                    discount = skuIdAndDiscount.get(skuId);
                    discount = discount != null ? discount : 0;
                }
                totalDiscount += quantity * discount;


                //需要计算的子订单的运费价格信息（优化计算时间）
                //到店自提:1 = 运费为0
                log.info("delivertype=="+fatOrder.getDeliverType());
                itemFreightFees.put(skuId ,(notNull(fatOrder.getDeliverType())&&notEmpty(fatOrder.getDeliverType())&&("1").equals(fatOrder.getDeliverType()))? 0:freightCountService.countFeeByItem(userTradeInfo.getProvinceCode() , skuAndItem.getItem(), quantity));
                freightFee += itemFreightFees.get(skuId);

            }

            order.setFee(totalFee + freightFee - totalDiscount);

            order.setDeliverFee(freightFee);
            order.setChannel(bank);//添加支付渠道

            //创建订单
            Long orderId = orderDao.create(order);
            sellerIdAndOrderIds.put(fatOrder.getSellerId(), orderId);
            //保存发票和买家留言

            //针对货到付款--新增deliverType不为空时，添加订单额外信息
            if (notEmpty(fatOrder.getBuyerNotes()) || fatOrder.getInvoiceType() != null || notEmpty(fatOrder.getDeliverTime())
                    || notEmpty(fatOrder.getDeliverType())) {
                OrderExtra orderExtra = simpleOrderExtraFactory(orderId, fatOrder);

                //配送方式。null或空默认为物流配送
                if(notNull(fatOrder.getDeliverType()) || notEmpty(fatOrder.getDeliverType())) {
                    orderExtra.setDeliverType(Integer.valueOf(fatOrder.getDeliverType()));
                }else{
                    orderExtra.setDeliverType(OrderExtra.DeliverTypeEnum.DELIVER.value());
                }

                //没有发票信息，如果家电频道默认创建
                if (fatOrder.getInvoiceType() == null) {
                    if (equalWith(order.getBusiness(), Business.APPLIANCE.value())) {
                        orderExtra.setInvoice(getPersonalInvoice());
                    }
                }
                if(equalWith(fatOrder.getInvoiceType(), Integer.valueOf(OrderExtra.Type.VAT.value()))) {
                    //增值税发票根据已经填写的信息自动生成
                    String vatInvoice = getExistVATInvoice(buyerId);
                    if(Strings.isNullOrEmpty(vatInvoice)) {
                        log.error("fail to create order when invoice type is VAT but userVatInvoice is null");
                        throw new ServiceException("vat.invoice.not.found");
                    }
                    orderExtra.setInvoice(vatInvoice);
                }
                // 送达时段
                orderExtra.setDeliverTime(fatOrder.getDeliverTime());

                orderExtraDao.create(orderExtra);

            } else if (equalWith(order.getBusiness(), Business.APPLIANCE.value())) {  // 家电频道默认创建个人发票
                OrderExtra orderExtra = new OrderExtra();
                orderExtra.setOrderId(orderId);
                orderExtra.setInvoice(getPersonalInvoice());

                //配送方式。null或空默认为物流配送
                if(notNull(fatOrder.getDeliverType()) || notEmpty(fatOrder.getDeliverType())) {
                    orderExtra.setDeliverType(Integer.valueOf(fatOrder.getDeliverType()));
                }else{
                    orderExtra.setDeliverType(OrderExtra.DeliverTypeEnum.DELIVER.value());
                }

                orderExtraDao.create(orderExtra);
            }

            for (Map.Entry<Long, Integer> entry : fatOrder.getSkuIdAndQuantity().entrySet()) {
                Long skuId = entry.getKey();
                Integer quantity = entry.getValue();
                SkuAndItem skuAndItem = skuAndItems.get(skuId);
                if (skuAndItem == null) {
                    log.error("can not find sku and item where sku id={},skip", skuId);
                    continue;
                }

                //每个子订单优惠价格
                Integer discount = 0;
                if (skuIdAndDiscount != null) {
                    discount = skuIdAndDiscount.get(skuId);
                    discount = discount != null ? discount : 0;
                }

                OrderItem orderItem = simpleOrderItemFactory(buyerId, skuAndItem, skuId, quantity, order, businessId);

                //需要计算一把子订单的运费价格信息
                orderItem.setDeliverFee(itemFreightFees.get(skuId));
                orderItem.setFee(skuAndItem.getSku().getPrice() * quantity + itemFreightFees.get(skuId) - discount * quantity);

                orderItem.setChannel(bank);//添加支付渠道
                orderItem.setIsBaskOrder(Boolean.FALSE);
                orderItemDao.create(orderItem);

                StockChange stockChange = simpleStockChangeFactory(orderItem);
                stockChanges.add(stockChange);
            }
        }
        //remove from cart
        Multiset<Long> cart = cartDao.getPermanent(buyerId);
        for (FatOrder fatOrder : fatOrders) {
            for (Map.Entry<Long, Integer> entry : fatOrder.getSkuIdAndQuantity().entrySet()) {
                cart.remove(entry.getKey(), entry.getValue());
            }
        }
        cartDao.setPermanent(buyerId, cart);
        orderResult.setSellerIdAndOrderId(sellerIdAndOrderIds);
        orderResult.setStockChanges(stockChanges);
        return orderResult;
    }

    private String getPersonalInvoice() {
        Map<String, String> mapped = Maps.newTreeMap();
        mapped.put("title", "个人");
        mapped.put("type", OrderExtra.Type.PLAIN.value());
        return mapper.toJson(mapped);
    }

    //增值税发票
    private String getExistVATInvoice(Long userId) {
        Response<UserVatInvoice> userVatInvoiceR = userVatInvoiceService.getByUserId(userId);
        if(!userVatInvoiceR.isSuccess() || userVatInvoiceR.getResult() == null) {
            log.error("fail to get vat invoice by userId={}, error code={}",userId, userVatInvoiceR.getError());
            throw new ServiceException("get.vat.invoice.fail");
        }
        Map<String, Object> mapped = Maps.newHashMap();
        mapped.put("type", OrderExtra.Type.VAT.value());
        mapped.put("vat", userVatInvoiceR.getResult());
        return mapper.toJson(mapped);
    }


    @Transactional
    public ItemBundleOrderResult itemBundleOrderCreate(Long buyerId, Long tradeInfoId, ItemBundleFatOrder itemBundleFatOrder, Map<Long, SkuAndItem> skuAndItems) {
        ItemBundleOrderResult result = new ItemBundleOrderResult();
        List<StockChange> stockChanges = Lists.newArrayList();
        if(Objects.equal(itemBundleFatOrder.getSellerId(), buyerId)) {
            log.warn("buyerId can not same as sellerId={}, this order will be ignored", buyerId);
            throw new ServiceException("illegal.buyer");
        }

        Response<Shop> shopR = shopService.findByUserId(itemBundleFatOrder.getSellerId());
        if (!shopR.isSuccess()) {
            log.error("failed to find shop by sellerId:{}, error code :{}", itemBundleFatOrder.getSellerId(), shopR.getError());
            throw new ServiceException("shop.not.found");
        }
        final Shop shop = shopR.getResult();
        Long businessId = shop.getBusinessId();

        Order order = simpleOrderFactory(buyerId,tradeInfoId,itemBundleFatOrder, businessId);
        //组合商品价格
        order.setFee(itemBundleFatOrder.getItemBundle().getPrice());
        //创建订单
        Long orderId = orderDao.create(order);
        result.setOrderId(orderId);

        //保存发票和买家留言
        if (!Strings.isNullOrEmpty(itemBundleFatOrder.getBuyerNotes()) ||
                !Strings.isNullOrEmpty(itemBundleFatOrder.getInvoice()) ||
                !Strings.isNullOrEmpty(itemBundleFatOrder.getDeliverTime())) {
            OrderExtra orderExtra = simpleOrderExtraFactory(orderId, itemBundleFatOrder);
            if (isEmpty(itemBundleFatOrder.getInvoice()) && equalWith(order.getBusiness(), Business.APPLIANCE.value())) {
                // 买家填了留言没填发票信息的情况， 家电频道默认创建个人发票
                orderExtra.setInvoice(getPersonalInvoice());
            }

            orderExtraDao.create(orderExtra);
        } else if (isEmpty(itemBundleFatOrder.getInvoice()) && equalWith(order.getBusiness(), Business.APPLIANCE.value())) {  // 家电频道默认创建一个个人发票
            OrderExtra orderExtra = new OrderExtra();
            orderExtra.setOrderId(orderId);
            orderExtra.setInvoice(getPersonalInvoice());
            orderExtraDao.create(orderExtra);
        }

        for (Map.Entry<Long, Integer> entry : itemBundleFatOrder.getSkuIdAndQuantity().entrySet()) {
            Long skuId = entry.getKey();
            Integer quantity = entry.getValue();
            SkuAndItem skuAndItem = skuAndItems.get(skuId);
            if (skuAndItem == null) {
                log.error("can not find sku and item where sku id={},skip", skuId);
                throw new ServiceException("illegal.sku");
            }
            //模板商品暂时不考虑码的使用
            OrderItem orderItem = simpleOrderItemFactory(buyerId,skuAndItem,skuId,quantity,order,businessId);
            orderItemDao.create(orderItem);

            StockChange stockChange = simpleStockChangeFactory(orderItem);
            stockChanges.add(stockChange);
        }
        result.setStockChanges(stockChanges);
        return result;
    }

    private Order simpleOrderFactory(Long buyerId, Long tradeInfoId, FatOrder fatOrder, Long businessId) {
        Order order = new Order();
        order.setSellerId(fatOrder.getSellerId());
        order.setBuyerId(buyerId);
        order.setTradeInfoId(tradeInfoId);
        order.setType(Order.Type.PLAIN.value());
        order.setPaymentType(fatOrder.getPaymentType());

        order.setBusiness(businessId);
        Integer status;
        //货到付款
        if(Objects.equal(Order.PayType.COD.value(), order.getPaymentType())) {
            status = Order.Status.PAID.value();
            order.setStatus(status);
        }else { //在线付款
            status = Order.Status.WAIT_FOR_PAY.value();
            order.setStatus(status);
        }

        return order;
    }

    private OrderExtra simpleOrderExtraFactory(Long orderId, FatOrder fatOrder) {
        OrderExtra orderExtra = new OrderExtra();
        orderExtra.setOrderId(orderId);
        orderExtra.setInvoice(fatOrder.getInvoice());
        orderExtra.setBuyerNotes(fatOrder.getBuyerNotes());
        orderExtra.setDeliverTime(fatOrder.getDeliverTime());
        return orderExtra;
    }

    private OrderItem simpleOrderItemFactory(Long buyerId, SkuAndItem skuAndItem,Long skuId,
                                             Integer quantity, Order order, Long businessId) {
        OrderItem orderItem = new OrderItem();
        orderItem.setBuyerId(buyerId);
        orderItem.setSellerId(skuAndItem.getItem().getUserId());
        orderItem.setItemId(skuAndItem.getItem().getId());
        orderItem.setItemName(skuAndItem.getItem().getName());
        orderItem.setBrandId(skuAndItem.getItem().getBrandId());
        orderItem.setBusinessId(businessId);
        orderItem.setSkuId(skuId);
        orderItem.setQuantity(quantity);
        orderItem.setFee(skuAndItem.getSku().getPrice() * quantity);
        orderItem.setOrderId(order.getId());
        orderItem.setStatus(order.getStatus());
        orderItem.setPayType(order.getPaymentType());
        orderItem.setType(OrderItem.Type.PLAIN.value());
        orderItem.setIsBaskOrder(Boolean.FALSE);

        Response<DeliveryMethod> deliveryMethodR = deliveryMethodService.findById(skuAndItem.getItem().getDeliveryMethodId());
        if(!deliveryMethodR.isSuccess() || deliveryMethodR.getResult() == null) {
            log.error("fail to find delivery method by id={} when create order",skuAndItem.getItem().getDeliveryMethodId());
        }else {
            orderItem.setDeliveryPromise(deliveryMethodR.getResult().getName());
        }
        return orderItem;
    }

    private StockChange simpleStockChangeFactory(OrderItem orderItem) {
        StockChange stockChange = new StockChange();
        stockChange.setSkuId(orderItem.getSkuId());
        stockChange.setItemId(orderItem.getItemId());
        stockChange.setQuantity(orderItem.getQuantity());
        return stockChange;
    }

    @Transactional
    public void pay(String paymentCode, Date paidAt, Long... orderIds) throws Exception {
        for(Long orderId : orderIds) {
            Order order = orderDao.findById(orderId);
            if (order == null) {
                throw new ServiceException("order.not.found");
            }
            TradeFSM tradeFSM = new TradeFSM(Order.Status.from(order.getStatus()));
            tradeFSM.buyerPaid();
            Order updated = new Order();
            updated.setId(order.getId());
            updated.setStatus(tradeFSM.getCurrentState().value());
            updated.setPaidAt(new Date());
            updated.setPaymentCode(paymentCode);
            updated.setPaidAt(paidAt);
            orderDao.update(updated);

            //更新 orderItem 支付时间
            bathUpdateOrderItemPaidAtByOrderId(OrderItem.Status.WAIT_FOR_PAY.value(),order.getId(),paidAt);

            //更新 orderItem 状态, 只对状态为  WAIT_FOR_PAY 的子订单操作
            bathUpdateOrderItemStatusByOrderId(OrderItem.Status.WAIT_FOR_PAY.value(),
                    tradeFSM.getCurrentState().value(), order.getId(), paymentCode);
        }
    }

    @Transactional
    public void deliver(Order order) throws Exception {
        TradeFSM tradeFSM = new TradeFSM(Order.Status.from(order.getStatus()));
        tradeFSM.sellerDelivered();
        Order updated = new Order();
        updated.setId(order.getId());
        updated.setStatus(tradeFSM.getCurrentState().value());
        updated.setDeliveredAt(new Date());
        orderDao.update(updated);
        //更新 orderItem 状态, 只对状态为 PAID 的子订单操作
        bathUpdateOrderItemStatusByOrderId(OrderItem.Status.PAYED.value(),
                tradeFSM.getCurrentState().value(), order.getId(), null);
    }

    @Transactional
    public void deliver(Order order, OrderLogisticsInfoDto orderLogisticsInfoDto) throws Exception {
        deliver(order);
        // 创建订单物流信息
        orderLogisticsInfoDto.getOrderLogisticsInfo().setOrderId(order.getId());
        createLogisticsInfo(orderLogisticsInfoDto);

        // 试金行动 START
        OrderJobOverDay orderJobDay = new OrderJobOverDay();
        orderJobDay.setOrderId(order.getId());
        Response<Paging<OrderJobOverDay>> orderJobDayR = orderJobOverDayService.findBy(orderJobDay);

        if (orderJobDayR.isSuccess() && !orderJobDayR.getResult().getData().isEmpty()) {

            Response<OrderJobDayConfig> orderJobDayConfigResponse
                    = orderJobOverDayConfigService.findBySku(orderJobDayR.getResult().getData().get(0).getSkuId());
            if (orderJobDayConfigResponse.isSuccess() && orderJobDayConfigResponse.getResult() != null) {

                Date date = new DateTime().withTimeAtStartOfDay().plusDays(orderJobDayConfigResponse.getResult().getExpireDay()).toDate();
                OrderJobOverDay orderJobOverDay = new OrderJobOverDay();
                orderJobOverDay.setOrderId(order.getId());
                orderJobOverDay.setStatus(0);
                orderJobOverDay.setOverDay(date);
                orderJobOverDayService.update(orderJobOverDay);
            }

        }
    }

    /**
     * 创建订单物流信息
     * @param orderLogisticsInfoDto 订单物流信息
     */
    private void createLogisticsInfo(OrderLogisticsInfoDto orderLogisticsInfoDto) {
        OrderLogisticsInfo orderLogisticsInfo = orderLogisticsInfoDto.getOrderLogisticsInfo();
        // 第三方快递
        if (Objects.equal(orderLogisticsInfo.getType(), OrderLogisticsInfo.Type.THIRD.value())){
            ExpressInfo expressInfo = expressInfoDao.findByName(orderLogisticsInfo.getExpressName());
            if (expressInfo == null){
                log.error("express info(name={}) isn't exist.", orderLogisticsInfo.getExpressName());
                throw new ServiceException("express.info.not.exist");
            }
            orderLogisticsInfo.setExpressCode(expressInfo.getCode());
        }
        orderLogisticsInfoDao.create(orderLogisticsInfo);
        OrderExtra orderExtra = new OrderExtra();
        orderExtra.setOrderId(orderLogisticsInfo.getOrderId());
        orderExtra.setHasLogistics(Boolean.TRUE);
        orderExtra.setLogisticsInfo(JsonMapper.JSON_NON_DEFAULT_MAPPER.toJson(orderLogisticsInfo));
        // 是否有安装信息
        if (orderLogisticsInfoDto.getHasInstall() != null && orderLogisticsInfoDto.getHasInstall()){
            InstallInfo installInfo = installInfoDao.findByName(orderLogisticsInfoDto.getInstallName());
            if (installInfo == null || !Objects.equal(InstallInfo.Status.ENABLED.value(), installInfo.getStatus())){
                log.error("install info({}) isn't valid", installInfo);
                throw new ServiceException("install.info.not.exist");
            }
            orderExtra.setHasInstall(Boolean.TRUE);
            orderExtra.setInstallType(orderLogisticsInfoDto.getInstallType());
            orderExtra.setInstallName(orderLogisticsInfoDto.getInstallName());
        }
        orderExtraDao.updateByOrderId(orderExtra);
    }

    @Transactional
    public void confirm(Long orderId, Long buyerId) throws Exception {
        Order order = orderDao.findById(orderId);
        if (order == null) {
            throw new ServiceException("order.not.found");
        }
        if (!Objects.equal(order.getBuyerId(), buyerId)) {
            throw new ServiceException("order.not.permit");
        }
        TradeFSM tradeFSM = new TradeFSM(Order.Status.from(order.getStatus()));
        tradeFSM.confirmed();
        Date now = DateTime.now().toDate();
        Order updated = new Order();
        updated.setId(order.getId());
        updated.setStatus(tradeFSM.getCurrentState().value());
        updated.setDoneAt(now);
        updated.setFinishedAt(now);
        if(Objects.equal(order.getPaymentType(), Order.PayType.COD.value())) {
            updated.setPaidAt(now);
        }
        orderDao.update(updated);
        //更新 orderItem 状态, 只对状态为 DELIVERED 的子订单操作
        bathUpdateOrderItemStatusByOrderId(OrderItem.Status.DELIVERED.value(),
                tradeFSM.getCurrentState().value(), order.getId(), null);
    }

    @Transactional
    public void cancelByBuyer(Order order) throws Exception {
        TradeFSM tradeFSM = new TradeFSM(Order.Status.from(order.getStatus()));
        tradeFSM.buyerCanceled();
        Order updated = new Order();
        updated.setId(order.getId());
        updated.setStatus(tradeFSM.getCurrentState().value());
        Date now = DateTime.now().toDate();
        updated.setFinishedAt(now);
        updated.setCanceledAt(now);
        orderDao.update(updated);

        bathUpdateOrderItemStatusByOrderId(OrderItem.Status.WAIT_FOR_PAY.value(),
                tradeFSM.getCurrentState().value(), order.getId(), null);
    }

    @Transactional
    public void cancelCodByBuyer(Order order) throws Exception {
        TradeFSM tradeFSM = new TradeFSM(Order.Status.from(order.getStatus()));
        tradeFSM.codBuyerCanceled();
        Order updated = new Order();
        updated.setId(order.getId());
        updated.setStatus(tradeFSM.getCurrentState().value());
        Date now = DateTime.now().toDate();
        updated.setFinishedAt(now);
        updated.setCanceledAt(now);
        orderDao.update(updated);

        bathUpdateOrderItemStatusByOrderId(order.getStatus(),
                tradeFSM.getCurrentState().value(), order.getId(), null);
    }

    @Transactional
    public void cancelCodBySeller(Order order) throws Exception {
        TradeFSM tradeFSM = new TradeFSM(Order.Status.from(order.getStatus()));
        tradeFSM.codSellerCanceled();
        Order updated = new Order();
        updated.setId(order.getId());
        updated.setStatus(tradeFSM.getCurrentState().value());
        Date now = DateTime.now().toDate();
        updated.setFinishedAt(now);
        updated.setCanceledAt(now);
        orderDao.update(updated);

        bathUpdateOrderItemStatusByOrderId(order.getStatus(),
                tradeFSM.getCurrentState().value(), order.getId(), null);
    }

    @Transactional
    public void cancelPreSaleCodBySeller(Order order, List<Long> orderItemIds) throws Exception {
        Order updated = new Order();
        updated.setId(order.getId());
        updated.setStatus(Order.Status.CANCELED_BY_SELLER.value());
        Date now = DateTime.now().toDate();
        updated.setFinishedAt(now);
        updated.setCanceledAt(now);
        orderDao.update(updated);

        orderItemDao.batchUpdateStatus(orderItemIds, OrderItem.Status.CANCELED_BY_SELLER.value());
    }

    /**
     * 定金未支付的预售订单买家取消
     * @param order 订单
     * @param orderItemIds 子订单id列表
     * @throws Exception
     */
    @Transactional
    public void preSaleCancelByBuyer(Order order, List<Long> orderItemIds) throws Exception {
        Order updated = new Order();
        updated.setId(order.getId());
        updated.setStatus(Order.Status.CANCELED_BY_BUYER.value());
        Date now = DateTime.now().toDate();
        updated.setFinishedAt(now);
        updated.setCanceledAt(now);
        orderDao.update(updated);

        orderItemDao.batchUpdateStatus(orderItemIds, OrderItem.Status.CANCELED_BY_BUYER.value());
    }

    /**
     * 定金已支付的预售订单买家取消
     */
    @Transactional
    public void preSalePaidCancelByBuyer(Order order, List<Long> orderItemIds) throws Exception {
        Order updated = new Order();
        updated.setId(order.getId());
        updated.setStatus(Order.Status.CANCELED_PRESALE_DEPOSIT_BY_BUYER.value());
        Date now = DateTime.now().toDate();
        updated.setFinishedAt(now);
        updated.setPaidAt(now);// 预售订单当买家关闭订单需要更新支付时间
        updated.setCanceledAt(now);
        orderDao.update(updated);

        orderItemDao.batchUpdateStatus(orderItemIds, OrderItem.Status.CANCELED_PRESALE_DEPOSIT_BY_BUYER.value());
    }

    @Transactional
    public void cancelBySeller(Order order) throws Exception {
        TradeFSM tradeFSM = new TradeFSM(Order.Status.from(order.getStatus()));
        tradeFSM.sellerCanceled();
        Order updated = new Order();
        updated.setId(order.getId());
        updated.setStatus(tradeFSM.getCurrentState().value());
        updated.setCanceledAt(DateTime.now().toDate());
        orderDao.update(updated);

        bathUpdateOrderItemStatusByOrderId(OrderItem.Status.WAIT_FOR_PAY.value(),
                tradeFSM.getCurrentState().value(), order.getId(), null);
    }

    @Transactional
    public boolean cancelByRefund(OrderItem orderItem) throws Exception {
        OrderItemTradeFSM orderItemTradeFSM = new OrderItemTradeFSM(OrderItem.Status.from(orderItem.getStatus()));
        orderItemTradeFSM.sellerRefund();
        OrderItem updated = new OrderItem();
        updated.setId(orderItem.getId());
        updated.setStatus(orderItemTradeFSM.getCurrentState().value());
        //普通订单退款只更新退款时间
        updated.setRefundAt(DateTime.now().toDate());
        orderItemDao.update(updated);
        //判断一发是否所有子订单都修改了状态，如果是，修改总订单状态
        return cancelOrder(orderItem.getOrderId(), orderItemTradeFSM.getCurrentState().value());
    }

    @Transactional
    public void cancelByReturnGoods(OrderItem orderItem) throws Exception {
        OrderItemTradeFSM orderItemTradeFSM = new OrderItemTradeFSM(OrderItem.Status.from(orderItem.getStatus()));
        orderItemTradeFSM.confirmReturnGoods();
        OrderItem updated = new OrderItem();
        updated.setId(orderItem.getId());
        updated.setStatus(orderItemTradeFSM.getCurrentState().value());
        //普通订单退货，在线支付：更新退款时间和退货时间 货到付款：更新退货时间
        if(Objects.equal(orderItem.getPayType(), OrderItem.PayType.ONLINE.value())) {
            updated.setRefundAt(DateTime.now().toDate());
            updated.setReturnGoodsAt(DateTime.now().toDate());
        }
        if(Objects.equal(orderItem.getPayType(), OrderItem.PayType.COD.value())) {
            updated.setReturnGoodsAt(DateTime.now().toDate());
        }
        orderItemDao.update(updated);

        cancelOrder(orderItem.getOrderId(), orderItemTradeFSM.getCurrentState().value());
    }

    @Transactional
    public void requestRefund(OrderItem orderItem) throws Exception {
        OrderItemTradeFSM orderItemTradeFSM = new OrderItemTradeFSM(OrderItem.Status.from(orderItem.getStatus()));
        orderItemTradeFSM.requestRefund();
        OrderItem updated = new OrderItem();
        updated.setId(orderItem.getId());
        updated.setStatus(orderItemTradeFSM.getCurrentState().value());
        updated.setReason(orderItem.getReason());
        updated.setRefundAmount(orderItem.getRefundAmount());
        updated.setRequestRefundAt(new Date());
        orderItemDao.update(updated);

        updateOrder(orderItem.getOrderId(), orderItemTradeFSM.getCurrentState().value());
    }

    @Transactional
    public void requestReturnGoods(OrderItem orderItem) throws Exception {
        OrderItemTradeFSM orderItemTradeFSM = new OrderItemTradeFSM(OrderItem.Status.from(orderItem.getStatus()));
        orderItemTradeFSM.requestReturnGoods();
        OrderItem updated = new OrderItem();
        updated.setId(orderItem.getId());
        updated.setStatus(orderItemTradeFSM.getCurrentState().value());
        updated.setReason(orderItem.getReason());
        updated.setRefundAmount(orderItem.getRefundAmount());
        Date now = DateTime.now().toDate();
        updated.setRequestRefundAt(now);
        orderItemDao.update(updated);

        updateOrder(orderItem.getOrderId(), orderItemTradeFSM.getCurrentState().value());
    }

    @Transactional
    public void refuseRefund(OrderItem orderItem) throws Exception {
        OrderItemTradeFSM orderItemTradeFSM = new OrderItemTradeFSM(OrderItem.Status.from(orderItem.getStatus()));
        orderItemTradeFSM.refuseRefund();
        OrderItem updated = new OrderItem();
        updated.setId(orderItem.getId());
        updated.setStatus(orderItemTradeFSM.getCurrentState().value());
        orderItemDao.update(updated);

        updateOrder(orderItem.getOrderId(), orderItemTradeFSM.getCurrentState().value());
    }

    @Transactional
    public void refuseReturnGoods(OrderItem orderItem, Order order) throws Exception {
        OrderItemTradeFSM orderItemTradeFSM = new OrderItemTradeFSM(OrderItem.Status.from(orderItem.getStatus()));
        orderItemTradeFSM.refuseReturnGoods();
        OrderItem updated = new OrderItem();
        updated.setId(orderItem.getId());
        updated.setStatus(orderItemTradeFSM.getCurrentState().value());


        orderItemDao.update(updated);

        //如果卖家拒绝退货, 在将订单状态改为卖家已发货, 等待买家确认的状态, 同时将发货时间改为当前时间, 防止订单被自动确认
        Order uo = new Order();
        uo.setId(order.getId());
        uo.setDeliveredAt(new Date());
        orderDao.update(uo);

        updateOrder(orderItem.getOrderId(), orderItemTradeFSM.getCurrentState().value());
    }

    @Transactional
    public void agreeReturnGoods(OrderItem orderItem) throws Exception {
        OrderItemTradeFSM orderItemTradeFSM = new OrderItemTradeFSM(OrderItem.Status.from(orderItem.getStatus()));
        orderItemTradeFSM.agreeReturnGoods();
        OrderItem updated = new OrderItem();
        updated.setId(orderItem.getId());
        updated.setStatus(orderItemTradeFSM.getCurrentState().value());
        orderItemDao.update(updated);

        updateOrder(orderItem.getOrderId(), orderItemTradeFSM.getCurrentState().value());
    }

    @Transactional
    @SuppressWarnings("unused")
    public void buyerReturnGoods(OrderItem orderItem) throws Exception {
        OrderItemTradeFSM orderItemTradeFSM = new OrderItemTradeFSM(OrderItem.Status.from(orderItem.getStatus()));
        orderItemTradeFSM.buyerReturnGoods();
        OrderItem updated = new OrderItem();
        updated.setId(orderItem.getId());
        updated.setStatus(orderItemTradeFSM.getCurrentState().value());
        orderItemDao.update(updated);

        updateOrder(orderItem.getOrderId(), orderItemTradeFSM.getCurrentState().value());
    }

    @Transactional
    public void undoRequestRefund(OrderItem orderItem) throws Exception {
        OrderItemTradeFSM orderItemTradeFSM = new OrderItemTradeFSM(OrderItem.Status.from(orderItem.getStatus()));
        orderItemTradeFSM.undoRequestRefund();
        OrderItem updated = new OrderItem();
        updated.setId(orderItem.getId());
        updated.setStatus(orderItemTradeFSM.getCurrentState().value());
        orderItemDao.update(updated);

        updateOrder(orderItem.getOrderId(), orderItemTradeFSM.getCurrentState().value());
    }

    @Transactional
    public void undoRequestReturnGoods(OrderItem orderItem) throws Exception {
        OrderItemTradeFSM orderItemTradeFSM = new OrderItemTradeFSM(OrderItem.Status.from(orderItem.getStatus()));
        orderItemTradeFSM.undoRequestReturnGoods();
        OrderItem updated = new OrderItem();
        updated.setId(orderItem.getId());
        updated.setStatus(orderItemTradeFSM.getCurrentState().value());
        orderItemDao.update(updated);

        updateOrder(orderItem.getOrderId(), orderItemTradeFSM.getCurrentState().value());
    }

    /**
     * 预售付定金或者尾款
     * @param orderItem 子订单
     * @param paidAt 付款时间
     * @throws Exception
     */
    @Transactional
    public void preSalePay(OrderItem orderItem, String paymentCode,Date paidAt) throws Exception {
        OrderItem updated = new OrderItem();
        updated.setId(orderItem.getId());
        updated.setStatus(OrderItem.Status.PAYED.value());
        updated.setPaymentCode(paymentCode);
        updated.setPaidAt(paidAt);
        orderItemDao.update(updated);


        //如果是尾款订单付款，或者定金订单付款并且付款方式是货到付款
        if(Objects.equal(orderItem.getType(), OrderItem.Type.PRESELL_REST.value())
                || (Objects.equal(orderItem.getType(), OrderItem.Type.PRESELL_DEPOSIT.value())
                && Objects.equal(orderItem.getPayType(), OrderItem.PayType.COD.value()))) {
            Order updateOrder = new Order();
            updateOrder.setId(orderItem.getOrderId());
            updateOrder.setStatus(Order.Status.PAID.value());
            updateOrder.setPaidAt(new Date());
            orderDao.update(updateOrder);
        }else {
            //付定金也要更新总订单updated_at这个字段
            Order updateO = new Order();
            updateO.setId(orderItem.getOrderId());
            orderDao.update(updateO);
        }
    }

    /**
     * 预售订单退款退货
     * @param order 订单
     * @param status  订单待更新状态
     */
    @Transactional
    public void cancelPreSaleOrderItem(Order order, Integer status) {
        List<OrderItem> orderItems = orderItemDao.findByOrderId(order.getId());

        for(OrderItem oi : orderItems) {
            OrderItem updated = new OrderItem();
            updated.setId(oi.getId());
            updated.setStatus(status);
            //预售退款，更新定金和尾款退款时间
            Date now = DateTime.now().toDate();
            if(Objects.equal(status, OrderItem.Status.CANCELED_BY_REFUND.value())) {
                updated.setRefundAt(now);
            }
            //预售退货，在线支付：更新定金和尾款退款，退货时间，货到付款：更新定金退款和退货时间，同时更新尾款退货时间
            if(Objects.equal(status, OrderItem.Status.CANCELED_BY_RETURNGOODS.value())) {
                if(Objects.equal(oi.getPayType(), OrderItem.PayType.ONLINE.value())) {
                    updated.setRefundAt(now);
                    updated.setReturnGoodsAt(now);
                }
                if(Objects.equal(oi.getPayType(), OrderItem.PayType.COD.value())) {
                    if(Objects.equal(oi.getType(), OrderItem.Type.PRESELL_DEPOSIT.value())) {
                        updated.setRefundAt(now);
                        updated.setReturnGoodsAt(now);
                    }
                    if(Objects.equal(oi.getType(), OrderItem.Type.PRESELL_REST.value())) {
                        updated.setReturnGoodsAt(now);
                    }
                }
            }
            orderItemDao.update(updated);
        }

        Order updatedOrder = new Order();
        updatedOrder.setId(order.getId());
        updatedOrder.setStatus(status);
        Date now = DateTime.now().toDate();
        updatedOrder.setFinishedAt(now);
        updatedOrder.setCanceledAt(now);
        updatedOrder.setPaidAt(now);
        orderDao.update(updatedOrder);

        //恢复库存
        OrderItem earnestOrRemain = orderItems.get(0);
        itemService.changeSoldQuantityAndStock(earnestOrRemain.getSkuId(), earnestOrRemain.getItemId(),
                earnestOrRemain.getQuantity());
    }

    /**
     * 预售申请退款退货
     * @param order 订单
     * @param earnest 定金子订单
     * @param remain 尾款子订单
     * @param status 待更新状态
     */
    @Transactional
    public void preSaleAddReasonAndRefund(Order order, OrderItem earnest, OrderItem remain, Integer status) {
        orderItemDao.update(earnest);
        remain.setStatus(status);
        orderItemDao.update(remain);
        order.setStatus(status);
        orderDao.update(order);
    }

    /**
     * 押金申请退款退货
     * @param order 订单
     * @param depositOrder 押金子订单
     * @param remainOrder 尾款子订单
     * @param preAuthorizationDepositOrder 尾款子订单
     * @param status 待更新状态
     */
    @Transactional
    public void depositAddReasonAndRefund(Order order, OrderItem depositOrder, OrderItem remainOrder,PreAuthorizationDepositOrder preAuthorizationDepositOrder, Integer status) {
        orderItemDao.update(depositOrder);
        orderItemDao.update(remainOrder);
        preAuthorizationDepositOrder.setStatus(status);
        preAuthorizationDao.update(preAuthorizationDepositOrder);
    }

    /**
     * 预售订单超时同时更新总订单和它所有子订单的状态, 同时还要加回库存以及减少售出数目
     * @param orderId 总订单的状态
     * @param status  需要更新的状态
     */
    @Transactional
    public void updateOrderAndOrderItems(Long orderId, Integer status) {
        Order order = new Order();
        order.setId(orderId);
        order.setStatus(status);
        Date now = DateTime.now().toDate();
        // 需要更新支付时间及结束时间用以结算
        order.setPaidAt(now);
        order.setFinishedAt(now);
        orderDao.update(order);
        List<OrderItem> orderItems = orderItemDao.findByOrderId(orderId);
        orderItemDao.batchUpdateStatus(Lists.transform(orderItems, new Function<OrderItem, Long>() {
            @Override
            public Long apply(OrderItem orderItem) {
                return orderItem.getId();
            }
        }), status);

        //恢复库存以及减少出售数目,预售定金和尾款的数量肯定是相同的，所以任取一个即可
        OrderItem earnestOrRemain = orderItems.get(0);
        Response<Boolean> r = itemService.changeSoldQuantityAndStock(earnestOrRemain.getSkuId(),
                earnestOrRemain.getItemId(),earnestOrRemain.getQuantity());
        if(!r.isSuccess()){
            log.error("failed to change sold quantity and stock for item(id={}),error code:{}"
                    ,earnestOrRemain.getItemId(),r.getError());
        }
    }

    /**
     * 更新子订单的支付时间
     * @param orderId
     * @param paidAt
     */
    public void bathUpdateOrderItemPaidAtByOrderId(final Integer fromStatus,Long orderId,Date paidAt){
        List<OrderItem> orderItems = orderItemDao.findByOrderId(orderId);
        Iterable<OrderItem> filterOrderItem = Iterables.filter(orderItems, new Predicate<OrderItem>() {
            @Override
            public boolean apply(OrderItem input) {
                return Objects.equal(input.getStatus(), fromStatus);
            }
        });
        for (OrderItem orderItem :filterOrderItem){
            OrderItem newOrderItem = new OrderItem();
            newOrderItem.setId(orderItem.getId());
            newOrderItem.setPaidAt(paidAt);
            orderItemDao.update(newOrderItem);
        }

    }

    /**
     * - - 押金预授权业务
     * 当更新总订单状态时调用该方法判断它的哪些子订单需要修改
     *
     */
    public void bathUpdateOrderItemStatusByOrderId2(final Integer fromStatus, Integer toStatus,
                                                    Long orderId, String paymentCode) {
        List<OrderItem> orderItems = orderItemDao.findByOrderId(orderId);
        if(paymentCode != null) {
            orderItemDao.batchUpdatePaymentCode(Lists.transform(orderItems, new Function<OrderItem, Long>() {
                @Override
                public Long apply(OrderItem input) {
                    return input.getId();
                }
            }), paymentCode);
        }
        if(Objects.equal(toStatus, OrderItem.Status.DONE.value())) {
            for(OrderItem oi : orderItems) { //必须保证所有子订单是退货款完成，或者已发货，或者预售定金订单，才能关闭交易
                if(! (Objects.equal(oi.getStatus(), OrderItem.Status.CANCELED_BY_REFUND.value())
                        || Objects.equal(oi.getStatus(), OrderItem.Status.CANCELED_BY_RETURNGOODS.value())
                        || Objects.equal(oi.getStatus(), OrderItem.Status.DELIVERED.value())
                        || Objects.equal(oi.getType(), OrderItem.Type.PRESELL_DEPOSIT.value()))) {
                    log.error("orderItem{} status not right, need to be -3,-4,2 or preSale deposit type");
                    throw new ServiceException("orderItem.status.not.right");
                }
            }
        }
        Iterable<OrderItem> filterOrderItem = Iterables.filter(orderItems, new Predicate<OrderItem>() {
            @Override
            public boolean apply(OrderItem input) {
                return Objects.equal(input.getStatus(), fromStatus);
            }
        });
        List<Long> orderItemIds = Lists.newArrayList();
        for(OrderItem oi : filterOrderItem) {
            orderItemIds.add(oi.getId());
        }
        orderItemDao.batchUpdateStatus(orderItemIds, toStatus);
    }

    /**
     * 当更新总订单状态时调用该方法判断它的哪些子订单需要修改
     *
     */
    public void bathUpdateOrderItemStatusByOrderId(final Integer fromStatus, Integer toStatus,
                                                   Long orderId, String paymentCode) {
        List<OrderItem> orderItems = orderItemDao.findByOrderId(orderId);
        if(paymentCode != null) {
            orderItemDao.batchUpdatePaymentCode(Lists.transform(orderItems, new Function<OrderItem, Long>() {
                @Override
                public Long apply(OrderItem input) {
                    return input.getId();
                }
            }), paymentCode);
        }
        if(Objects.equal(toStatus, OrderItem.Status.DONE.value())) {
            for(OrderItem oi : orderItems) { //必须保证所有子订单是退货款完成，或者已发货，或者预售定金订单，才能关闭交易
                if(! (Objects.equal(oi.getStatus(), OrderItem.Status.CANCELED_BY_REFUND.value())
                        || Objects.equal(oi.getStatus(), OrderItem.Status.CANCELED_BY_RETURNGOODS.value())
                        || Objects.equal(oi.getStatus(), OrderItem.Status.DELIVERED.value())
                        || Objects.equal(oi.getType(), OrderItem.Type.PRESELL_DEPOSIT.value()))) {
                    log.error("orderItem{} status not right, need to be -3,-4,2 or preSale deposit type");
                    throw new ServiceException("orderItem.status.not.right");
                }
            }
        }
        if(Objects.equal(toStatus, OrderItem.Status.DELIVERED.value())) {
            for(OrderItem oi : orderItems) { //必须保证所有子订单是退款完成，或者已经付款，或者预售定金订单，才能发货
                if(!(Objects.equal(oi.getStatus(), OrderItem.Status.PAYED.value())
                        || Objects.equal(oi.getStatus(), OrderItem.Status.CANCELED_BY_REFUND.value())
                        || Objects.equal(oi.getType(), OrderItem.Type.PRESELL_DEPOSIT.value())) ) {
                    throw new ServiceException("orderItem.status.not.right");
                }
            }
        }
        Iterable<OrderItem> filterOrderItem = Iterables.filter(orderItems, new Predicate<OrderItem>() {
            @Override
            public boolean apply(OrderItem input) {
                return Objects.equal(input.getStatus(), fromStatus);
            }
        });
        List<Long> orderItemIds = Lists.newArrayList();
        for(OrderItem oi : filterOrderItem) {
            orderItemIds.add(oi.getId());
        }        
        orderItemDao.batchUpdateStatus(orderItemIds, toStatus);
    }

    /**
     * 当取消子订单状态后调用这个方法判断总订单是不是需要修改状态
     * @param orderId 订单id
     * @param toStatus 可能需要修改的状态
     */
    public boolean cancelOrder(Long orderId, Integer toStatus) {
        List<OrderItem> orderItems = orderItemDao.findByOrderId(orderId);
        boolean orderUpdate = true;
        for(OrderItem oi : orderItems) {
            if(!Objects.equal(oi.getStatus(), toStatus)
                    &&!Objects.equal(oi.getStatus(), OrderItem.Status.CANCELED_BY_REFUND.value())
                    &&!Objects.equal(oi.getType(), OrderItem.Type.PRESELL_DEPOSIT.value())){
                orderUpdate = false;
                break;
            }
        }
        if(orderUpdate) {
            Order updateOrder = new Order();
            updateOrder.setId(orderId);
            updateOrder.setStatus(toStatus);
            updateOrder.setCanceledAt(new Date());
            updateOrder.setFinishedAt(new Date());
            orderDao.update(updateOrder);
        }
        return orderUpdate;
    }

    /**
     * 实现子订单的运费的更改（需要添加事务处理）
     * @param orderItemId   子订单编号
     * @param newFee        新的运费价格
     */
    @Transactional
    public void updateDeliverFee(Long orderItemId, Integer newFee){
        //更改子订单的统计数据
        OrderItem orderItem = orderItemDao.findById(orderItemId);

        OrderItem updater = new OrderItem();
        updater.setId(orderItem.getId());
        updater.setFee(orderItem.getFee() - orderItem.getDeliverFee() + newFee);
        updater.setDeliverFee(newFee);
        orderItemDao.update(updater);
        log.debug("success update orderItem deliver fee and total fee.");

        //更改总订单的统计数据
        Order order = orderDao.findById(orderItem.getOrderId());

        Order updateOrder = new Order();
        updateOrder.setId(order.getId());
        updateOrder.setFee(order.getFee() - orderItem.getDeliverFee() + newFee);
        updateOrder.setDeliverFee(order.getDeliverFee() - orderItem.getDeliverFee() + newFee);
        orderDao.update(updateOrder);
        log.debug("success update orderItem deliver fee and total fee.");
    }

    /**
     * 功能类似上一个方法
     */
    private void updateOrder(Long orderId, Integer toStatus) {
        List<OrderItem> orderItems = orderItemDao.findByOrderId(orderId);
        boolean orderUpdate = true;
        for(OrderItem oi : orderItems) {
            //如果除了正在操作的子订单之外，其他子订单全部都关闭了或者是预售定金订单，总订单状态和子订单状态同步
            if(!Objects.equal(oi.getStatus(), toStatus)
                    && !Objects.equal(oi.getStatus(), OrderItem.Status.CANCELED_BY_REFUND.value())
                    && !Objects.equal(oi.getStatus(), OrderItem.Status.CANCELED_BY_RETURNGOODS.value())
                    && !Objects.equal(oi.getStatus(), OrderItem.Status.CANCELED_BY_BUYER.value())
                    && !Objects.equal(oi.getStatus(), OrderItem.Status.CANCELED_BY_SELLER.value())
                    && !Objects.equal(oi.getStatus(), OrderItem.Status.CANCELED_BY_EARNEST_EXPIRE.value())
                    && !Objects.equal(oi.getStatus(), OrderItem.Status.CANCELED_BY_REMAIN_EXPIRE.value())
                    && !Objects.equal(oi.getType(), OrderItem.Type.PRESELL_DEPOSIT.value())){
                orderUpdate = false;
                break;
            }
        }
        if(orderUpdate) {
            Order updateOrder = new Order();
            updateOrder.setId(orderId);
            updateOrder.setStatus(toStatus);
            orderDao.update(updateOrder);
        }
    }

    /**
     * 更新订单及子订单状态
     *
     * @param updating      订单更新信息
     * @param orderItems    子订单更新
     */
    @Transactional
    public void updateOrderAndOrderItems4Fix(Order updating, List<OrderItem> orderItems) {
        orderDao.update(updating);
        orderDao.emptyCanceledAndFinishedAt(updating.getId());

        for (OrderItem oi : orderItems) {
            orderItemDao.update(oi);
        }
    }


    /**
     * 重置子订单的id并返回重置后的子订单对象
     *
     * @param oi    需要重置的子订单对象
     * @return  重置后的子订单对象
     */
    @Transactional
    public OrderItem resetOrderItem(OrderItem oi, String channel) {
        OrderItem newOrderItem = new OrderItem();
        BeanMapper.copy(oi, newOrderItem);
        newOrderItem.setChannel(channel);
        //保留原始子订单号，避免物理删除后导致无法查找。
        if(isNull(oi.getOriginId())){
            newOrderItem.setOriginId(oi.getId());
        }
        boolean success = orderItemDao.delete(oi.getId());
        checkState(success, "order.item.delete.fail");

        newOrderItem.setId(null);
        orderItemDao.create(newOrderItem);
        checkState(notNull(newOrderItem.getId()), "order.item.create.fail'");
        return newOrderItem;
    }

    @Transactional
    public OrderIdAndEarnestId preSaleOrderCreate(Order order, OrderItem earnest, OrderItem remain, OrderExtra orderExtra) {
        OrderIdAndEarnestId result = new OrderIdAndEarnestId();

        orderDao.create(order);
        result.setOrderId(order.getId());

        earnest.setOrderId(order.getId());
        orderItemDao.create(earnest);
        result.setEarnestId(earnest.getId());

        remain.setOrderId(order.getId());
        orderItemDao.create(remain);

        orderExtra.setOrderId(order.getId());
        orderExtraDao.create(orderExtra);

        return result;
    }

    /**
     * 重置订单的id并返回重置后的订单对象
     *
     * @param order    需要重置的订单对象
     * @return  重置后的订单对象
     */
    @Transactional
    public Order resetOrder(Order order, String channel) {
        Order newOrder = new Order();
        BeanMapper.copy(order, newOrder);
        newOrder.setChannel(channel);
        //保留原始订单号，避免物理删除后导致无法查找。
        if(isNull(order.getOriginId())){
            newOrder.setOriginId(order.getId());
        }

        orderDao.delete(order.getId());  //删除原订单
        newOrder.setId(null);
        orderDao.create(newOrder);//创建新订单
        checkState(notNull(newOrder.getId()), "order.create.fail'");
        Boolean isUpdate = orderItemDao.updateOrderId(order.getId(),newOrder.getId());//更新子订单中的总订单Id
        checkState(isUpdate, "orderItem.update.fail");
        orderExtraDao.updateOrderId(order.getId(),newOrder.getId());//更新orderExtras中总订单id,orderExtra 可能不存在
        return newOrder;
    }

    @Transactional
    public OrderResult createForSku(Long buyerId, Long tradeInfoId, List<FatOrder> fatOrders,
                              Map<Long, SkuAndItem> skuAndItems, Map<Long,Integer> skuIdAndDiscount,String bank) throws Exception {
        OrderResult orderResult = new OrderResult();
        Map<Long,Long> sellerIdAndOrderIds = Maps.newHashMap();
        List<StockChange> stockChanges = Lists.newArrayList();

        // 试金行动 START
        List<Long> skuIdList = new ArrayList<Long>();
        // 试金行动订单，sku自动收货天数设定。
        Map<String, Object> params = Maps.newLinkedHashMap();
        Response<Paging<OrderJobDayConfig>> orderJobDayConfigR = orderJobOverDayConfigService.findBy(params);
        Map<Long, Integer> orderJobDayConfigMap = Maps.newLinkedHashMap();
        if (notNull(orderJobDayConfigR.getResult().getTotal()) && orderJobDayConfigR.getResult().getTotal() > 0 ) {

            for (OrderJobDayConfig orderJobDayConfig : orderJobDayConfigR.getResult().getData()) {

                orderJobDayConfigMap.put(orderJobDayConfig.getSkuId(), orderJobDayConfig.getExpireDay());
            }

        }
        // 试金行动 END

        for (FatOrder fatOrder : fatOrders) {
            //如果卖家和买家一样则不创建订单
            if(Objects.equal(fatOrder.getSellerId(), buyerId)) {
                log.warn("buyerId can not same as sellerId={}, this order will be ignored", buyerId);
                throw new ServiceException("can.not.buy.self.item");
            }

            if(fatOrder.getSkuIdAndQuantity() == null || fatOrder.getSkuIdAndQuantity().isEmpty()) {
                log.error("skuAndQuantity can not be null or empty by fatOrder", fatOrder);
                throw new ServiceException("sku.and.quantity.illegal");
            }

            Response<Shop> shopR = shopService.findByUserId(fatOrder.getSellerId());
            if (!shopR.isSuccess()) {
                log.error("failed to find shop by sellerId:{}, error code :{}", fatOrder.getSellerId(), shopR.getError());
                throw new ServiceException("shop.not.found");
            }
            final Shop shop = shopR.getResult();
            Long businessId = shop.getBusinessId();

            Order order = simpleOrderFactory(buyerId,tradeInfoId,fatOrder,businessId);

            //获取用户邮寄地址，用于计算运费信息
            Response<UserTradeInfo> userTradeInfoRes = userTradeInfoService.findById(tradeInfoId);
            if(!userTradeInfoRes.isSuccess()){
                log.error("find user trade info failed, tradeInfoId={}, error code={}", tradeInfoId, userTradeInfoRes.getError());
            }
            UserTradeInfo userTradeInfo = userTradeInfoRes.getResult();

            //订单总价
            int totalFee = 0;
            //运费总价
            int freightFee = 0;
            //优惠总价
            Integer totalDiscount = 0;

            Map<Long , Integer> itemFreightFees = Maps.newHashMap();

            for (Long skuId : fatOrder.getSkuIdAndQuantity().keySet()) {
                // 试金行动 START
                skuIdList = new ArrayList<Long>();
                skuIdList.add(skuId);
                // 试金行动 END

                Integer quantity = fatOrder.getSkuIdAndQuantity().get(skuId);
                SkuAndItem skuAndItem = skuAndItems.get(skuId);
                totalFee += quantity * skuAndItem.getSku().getPrice();

                Integer discount = 0;
                if(skuIdAndDiscount != null) {
                    discount = skuIdAndDiscount.get(skuId);
                    discount = discount != null ? discount : 0;
                }
                totalDiscount += quantity * discount;


                //需要计算的子订单的运费价格信息（优化计算时间）
                itemFreightFees.put(skuId , freightCountService.countFeeByItem(userTradeInfo.getProvinceCode() , skuAndItem.getItem(), quantity));

                freightFee += itemFreightFees.get(skuId);
            }
            order.setFee(totalFee + freightFee - totalDiscount);

            order.setDeliverFee(freightFee);
            order.setChannel(bank);//添加支付渠道

            //创建订单
            Long orderId = orderDao.create(order);

            // 试金行动 START
            for (Long skuId : skuIdList) {
                if (notNull(orderJobDayConfigMap.get(skuId))) {

                    Date date = new DateTime().withTimeAtStartOfDay().plusDays(orderJobDayConfigMap.get(skuId)).toDate();
                    OrderJobOverDay orderJobOverDay = new OrderJobOverDay();
                    orderJobOverDay.setSkuId(skuId);
                    orderJobOverDay.setStatus(2);
                    orderJobOverDay.setOrderId(orderId);
                    orderJobOverDay.setCreatedAt(new Date());
                    orderJobOverDay.setUpdatedAt(new Date());
                    orderJobOverDay.setOverDay(date);
                    Response<Boolean> longResponse = orderJobOverDayService.create(orderJobOverDay);
                    checkState(longResponse.isSuccess(), longResponse.getError());
                    break;
                }
            }
            // 试金行动 END

            sellerIdAndOrderIds.put(fatOrder.getSellerId(), orderId);
            //保存发票和买家留言

            if (notEmpty(fatOrder.getBuyerNotes()) || fatOrder.getInvoiceType() != null || notEmpty(fatOrder.getDeliverTime())) {
                OrderExtra orderExtra = simpleOrderExtraFactory(orderId, fatOrder);
                //没有发票信息，如果家电频道默认创建
                if (fatOrder.getInvoiceType() == null) {
                    if (equalWith(order.getBusiness(), Business.APPLIANCE.value())) {
                        orderExtra.setInvoice(getPersonalInvoice());
                    }
                }
                if(equalWith(fatOrder.getInvoiceType(), Integer.valueOf(OrderExtra.Type.VAT.value()))) {
                    //增值税发票根据已经填写的信息自动生成
                    String vatInvoice = getExistVATInvoice(buyerId);
                    if(Strings.isNullOrEmpty(vatInvoice)) {
                        log.error("fail to create order when invoice type is VAT but userVatInvoice is null");
                        throw new ServiceException("vat.invoice.not.found");
                    }
                    orderExtra.setInvoice(vatInvoice);
                }
                // 送达时段
                orderExtra.setDeliverTime(fatOrder.getDeliverTime());
                orderExtraDao.create(orderExtra);

            } else if (equalWith(order.getBusiness(), Business.APPLIANCE.value())) {  // 家电频道默认创建个人发票
                OrderExtra orderExtra = new OrderExtra();
                orderExtra.setOrderId(orderId);
                orderExtra.setInvoice(getPersonalInvoice());
                orderExtraDao.create(orderExtra);
            }

            for (Map.Entry<Long, Integer> entry : fatOrder.getSkuIdAndQuantity().entrySet()) {
                Long skuId = entry.getKey();
                Integer quantity = entry.getValue();
                SkuAndItem skuAndItem = skuAndItems.get(skuId);
                if (skuAndItem == null) {
                    log.error("can not find sku and item where sku id={},skip", skuId);

                    continue;
                }

                //每个子订单优惠价格
                Integer discount = 0;
                if(skuIdAndDiscount != null) {
                    discount = skuIdAndDiscount.get(skuId);
                    discount = discount != null ? discount : 0;
                }

                OrderItem orderItem = simpleOrderItemFactory(buyerId,skuAndItem,skuId,quantity,order,businessId);

                //需要计算一把子订单的运费价格信息
                orderItem.setDeliverFee(itemFreightFees.get(skuId));

                orderItem.setFee(skuAndItem.getSku().getPrice() * quantity + itemFreightFees.get(skuId) - discount * quantity);
                orderItem.setChannel(bank);//添加支付渠道
                orderItem.setIsBaskOrder(Boolean.FALSE);
                orderItemDao.create(orderItem);

                StockChange stockChange = simpleStockChangeFactory(orderItem);
                stockChanges.add(stockChange);
            }
        }
        //remove from cart
        Multiset<Long> cart = cartDao.getPermanent(buyerId);
        for (FatOrder fatOrder : fatOrders) {
            for (Map.Entry<Long, Integer> entry : fatOrder.getSkuIdAndQuantity().entrySet()) {
                cart.remove(entry.getKey(), entry.getValue());
            }
        }
        cartDao.setPermanent(buyerId, cart);
        orderResult.setSellerIdAndOrderId(sellerIdAndOrderIds);
        orderResult.setStockChanges(stockChanges);

        return orderResult;
    }

    @Transactional
    public Long buyingOrderCreate(Order order, OrderItem orderItem, OrderExtra orderExtra) {
        orderDao.create(order);

        orderItem.setOrderId(order.getId());
        orderItemDao.create(orderItem);

        orderExtra.setOrderId(order.getId());
        orderExtraDao.create(orderExtra);

        return order.getId();
    }

    @Transactional
    public void deliver2(Order order) throws Exception {
//        TradeFSM tradeFSM = new TradeFSM(Order.Status.from(order.getStatus()));
//        tradeFSM.sellerDelivered();
        Order updated = new Order();
        updated.setId(order.getId());
//        updated.setStatus(tradeFSM.getCurrentState().value());
        updated.setDeliveredAt(new Date());
        orderDao.update(updated);
        //更新 orderItem 状态, 只对状态为 PAID 的子订单操作
//        bathUpdateOrderItemStatusByOrderId(OrderItem.Status.PAYED.value(),
//                tradeFSM.getCurrentState().value(), order.getId(), null);
//        bathUpdateOrderItemStatusByOrderId2(OrderItem.Status.PAYED.value(),
//                tradeFSM.getCurrentState().value(), order.getId(), null);
    }

    @Transactional
    public void deliver2(Order order, OrderLogisticsInfoDto orderLogisticsInfoDto) throws Exception {
        deliver2(order);
        // 创建订单物流信息
        orderLogisticsInfoDto.getOrderLogisticsInfo().setOrderId(order.getId());
        createLogisticsInfo(orderLogisticsInfoDto);
    }

    @Transactional
    public void agreeReturnDeposit(OrderItem orderItem) throws Exception {
        OrderItem depositOrder = new OrderItem();
        depositOrder.setId(orderItem.getId());
        depositOrder.setStatus(1);
        PreAuthorizationDepositOrder preAuthorizationDepositOrder = preAuthorizationDao.findOneByOrderId(orderItem.getOrderId());
        if(preAuthorizationDepositOrder.getDeliverStatus()==1){
            preAuthorizationDepositOrder.setStatus(-2);
        }else {
            preAuthorizationDepositOrder.setStatus(-3);
        }
        orderItemDao.update(depositOrder);
        preAuthorizationDao.update(preAuthorizationDepositOrder);
    }

    @Transactional
    // 试金支付尾款交易成功
    public Response<Boolean> updateOrderCallBack(Long orderId) throws Exception {

        checkArgument(notNull(orderId), "orderId is null");
        Response<Boolean> result = new Response<Boolean>() ;
        result.setResult(Boolean.TRUE);

        //订单表交易
        Order order = orderDao.findById(orderId);
        if(isNull(order)){
            result.setError("order is null  , return directly");
            result.setResult(Boolean.FALSE);
            return result;
        }

        //商品子订单状态
        List<OrderItem> list =  orderItemDao.findByOrderId(orderId);
        if(isEmpty(list)){
            result.setError("OrderItem is empty ,   return directly");
            result.setResult(Boolean.FALSE);
            return result;
        }

        //押金订单表
        PreAuthorizationDepositOrder preOrder =  preAuthorizationDao.findOneByOrderId(orderId);
        if(isNull(preOrder)){
            result.setError("PreAuthorizationDepositOrder is null   ,   return directly");
            result.setResult(Boolean.FALSE);
            return result;
        }

        int orderState ,preOrderState ;
        int orderItemStateOne = -3;
        int orderItemStateTwo = 3;

        if(preOrder.getStatus() == PreAuthorizationDepositOrder.DepositPayType.PAYED.value() ||
                preOrder.getStatus() == PreAuthorizationDepositOrder.DepositPayType.PAYFINNSH.value()){
            //购买
            orderState = 3;
            preOrderState = 3;

        }else if (preOrder.getStatus() == PreAuthorizationDepositOrder.DepositPayType.DELIVERED.value()){
            //退订
            orderState = -3;
            orderItemStateTwo = 0;
            preOrderState = -3;

            //对于分仓的预售还要恢复库存
            //result = preDepositService.recoverPreDepositStorageIfNecessary(orderId);
            if(!result.isSuccess()){
                log.error("failed to recover storage for order(id={}), error code:{}", orderId);
            }
        }else{
            result.setError("preOrder statueP{} !=-2 and statue !='1,3'  , return directly");
            result.setResult(Boolean.FALSE);
            return result;
        }

        try {
            //订单表状态更新
            order.setStatus(orderState);
            orderDao.update(order) ;

        } catch (Exception e) {
            log.error("fail to update order, item whit orderId:{}, error:{}", e);
            result.setError("order update fail");
            result.setResult(Boolean.FALSE);
        }

        try {
            //商品子订单状态
            for(OrderItem item : list){
                if(item.getType()==2){
                    item.setStatus(orderItemStateOne);
                }else if(item.getType()==3){
                    item.setStatus(orderItemStateTwo);
                }
                orderItemDao.update(item);
            }
        } catch (Exception e) {
            log.error("fail to update OrderItem, item whit item:{}, error:{}", e);
            result.setError("OrderItem update fail");
            result.setResult(Boolean.FALSE);
        }

        try {
            //押金订单表
            preOrder.setStatus(preOrderState);
            preAuthorizationDao.updateById(preOrder);

        } catch (Exception e) {
            log.error("fail to update PreAuthorizationDepositOrder with preOrder, cause:{}", e);
            result.setError("preOrder update fail");
            result.setResult(Boolean.FALSE);
        }

        return result;
    }
}
