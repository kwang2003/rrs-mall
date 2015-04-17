package com.aixforce.rrs.buying.service;

import com.aixforce.annotations.ParamInfo;
import com.aixforce.common.model.PageInfo;
import com.aixforce.common.model.Paging;
import com.aixforce.common.model.Response;
import com.aixforce.common.utils.JsonMapper;
import com.aixforce.exception.ServiceException;
import com.aixforce.item.model.Item;
import com.aixforce.item.model.Sku;
import com.aixforce.item.service.ItemService;
import com.aixforce.redis.utils.JedisTemplate;
import com.aixforce.rrs.buying.dao.BuyingActivityDefinitionDao;
import com.aixforce.rrs.buying.dao.BuyingItemDao;
import com.aixforce.rrs.buying.dao.BuyingOrderRecordDao;
import com.aixforce.rrs.buying.dao.BuyingTempOrderDao;
import com.aixforce.rrs.buying.dto.BuyingActivityDto;
import com.aixforce.rrs.buying.dto.BuyingPreOrder;
import com.aixforce.rrs.buying.manager.BuyingActivityManger;
import com.aixforce.rrs.buying.model.BuyingActivityDefinition;
import com.aixforce.rrs.buying.model.BuyingItem;
import com.aixforce.rrs.buying.model.BuyingOrderRecord;
import com.aixforce.rrs.buying.model.BuyingTempOrder;
import com.aixforce.rrs.code.model.CodeUsage;
import com.aixforce.rrs.code.service.ActivityBindService;
import com.aixforce.shop.model.Shop;
import com.aixforce.shop.service.ShopService;
import com.aixforce.trade.dto.BuyingFatOrder;
import com.aixforce.trade.dto.RichOrderItem;
import com.aixforce.trade.dto.SkuAndItem;
import com.aixforce.trade.model.*;
import com.aixforce.trade.service.*;
import com.aixforce.user.base.BaseUser;
import com.aixforce.user.enums.Business;
import com.alibaba.dubbo.common.json.JSON;
import com.google.common.base.Objects;
import com.google.common.base.Strings;
import com.google.common.base.Throwables;
import com.google.common.collect.Maps;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import redis.clients.jedis.BinaryClient;
import redis.clients.jedis.Jedis;

import javax.annotation.Nullable;
import javax.annotation.PreDestroy;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.*;

import static com.aixforce.common.utils.Arguments.*;
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;

/**
 * 抢购活动service
 *
 * Mail: 964393552@qq.com <br>
 * Date: 2014-09-23 PM  <br>
 * Author: songrenfei
 */
@Slf4j
@Service
public class BuyingActivityDefinitionServiceImpl implements BuyingActivityDefinitionService{

    public static final int DEFAULT_QUEUE_SIZE = 1000;

    private final ExecutorService executorService = new ThreadPoolExecutor(1, 2, 60L, TimeUnit.MINUTES,
            new ArrayBlockingQueue<Runnable>(DEFAULT_QUEUE_SIZE),
            new ThreadFactoryBuilder().setNameFormat("activity-worker-%d").build(),
            new RejectedExecutionHandler() {
                @Override
                public void rejectedExecution(Runnable runnable, ThreadPoolExecutor executor) {
                    BuyingActivityFinishTask task = (BuyingActivityFinishTask) runnable;
                    log.error("BuyingActivityFinishTask ({}) call request is rejected", task.getTaskName());
                }
            });

    static abstract class BuyingActivityFinishTask implements Runnable {

        private String taskName;

        public BuyingActivityFinishTask(String taskName) {
            this.taskName = taskName;
        }

        public String getTaskName() {
            return taskName;
        }
    }

    @Autowired
    private JedisTemplate jedisTemplate;

    @Autowired
    private BuyingActivityDefinitionDao buyingActivityDefinitionDao;

    @Autowired
    private BuyingItemDao buyingItemDao;

    @Autowired
    private BuyingTempOrderDao buyingTempOrderDao;

    @Autowired
    private BuyingActivityManger buyingActivityManger;

    private static final DateTimeFormatter DATE_TIME_FORMAT = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");

    private static final DateTimeFormatter TIME_OUT_FORMATTER = DateTimeFormat.forPattern("yyyy-MM-dd-HH");

    @Autowired
    private ItemService itemService;

    @Autowired
    private DeliveryMethodService deliveryMethodService;

    @Autowired
    private ShopService shopService;

    @Autowired
    private UserTradeInfoService userTradeInfoService;

    @Autowired
    private FreightCountService freightCountService;

    @Autowired
    private UserVatInvoiceService userVatInvoiceService;

    @Autowired
    private ActivityBindService activityBindService;

    @Autowired
    private OrderWriteService orderWriteService;

    @Autowired
    private BuyingOrderRecordDao buyingOrderRecordDao;

    @Value("#{app.eHaierSellerId}")
    private String eHaierSellerId;

    private final static JsonMapper JSON_MAPPER = JsonMapper.nonDefaultMapper();

    @Override
    public Response<Long> create(BuyingActivityDefinition buyingActivityDefinition) {

        Response<Long> result = new Response<Long>();

        try {
            checkArgument(!isNull(buyingActivityDefinition),"illegal.param");
            Long id = buyingActivityDefinitionDao.create(buyingActivityDefinition);
            result.setResult(id);
            return result;
        }catch (IllegalArgumentException e){
            log.error("params can not be null");
            result.setError(e.getMessage());
            return result;
        }catch (Exception e) {
            log.error("failed to create buyingActivityDefinition {}, cause:{}", buyingActivityDefinition, Throwables.getStackTraceAsString(e));
            result.setError("buying.activity.definition.create.failed");
            return result;
        }
    }

    @Override
    public Response<Long> create(BuyingActivityDto buyingActivityDto,Long userId) {

        Response<Long> result = new Response<Long>();

        try {
            checkArgument(!isNull(buyingActivityDto),"illegal.param");
            checkArgument(!isNull(buyingActivityDto.getBuyingActivityDefinition()),"illegal.param");
            checkArgument(!isNull(buyingActivityDto.getBuyingActivityDefinition().getPayLimit()),"illegal.param");
            Long idResult = buyingActivityManger.create(buyingActivityDto, userId);
            result.setResult(idResult);
            return result;
        }catch (IllegalArgumentException e){
            log.error("params can not be null");
            result.setError(e.getMessage());
            return result;
        }catch (ServiceException e){
            log.error("failed to create buyingActivityDefinition buyingActivityDto={}, cause:{}", buyingActivityDto.toString(), Throwables.getStackTraceAsString(e));
            result.setError(e.getMessage());
            return result;
        }catch (Exception e) {
            log.error("failed to create buyingActivityDefinition buyingActivityDto={}, cause:{}", buyingActivityDto.toString(), Throwables.getStackTraceAsString(e));
            result.setError("buying.activity.definition.create.failed");
            return result;
        }
    }

    @Override
    public Response<Boolean> update(BuyingActivityDefinition buyingActivityDefinition) {
        Response<Boolean> result = new Response<Boolean>();
        try {
            checkArgument(!isNull(buyingActivityDefinition),"illegal.param");
            checkArgument(!isNull(buyingActivityDefinition.getId()),"illegal.param");
            result.setResult(buyingActivityDefinitionDao.update(buyingActivityDefinition));
            return result;
        }catch (IllegalArgumentException e){
            log.error("params can not be null");
            result.setError(e.getMessage());
            return result;
        }catch (Exception e) {
            log.error("failed to update buyingActivityDefinition {}, cause:{}", buyingActivityDefinition, Throwables.getStackTraceAsString(e));
            result.setError("buying.activity.definition.update.failed");
        }
        return result;
    }



    @Override
    public Response<Boolean> update(BuyingActivityDto buyingActivityDto,Long userId) {
        Response<Boolean> result = new Response<Boolean>();
        try {
            checkArgument(!isNull(buyingActivityDto),"illegal.param");
            checkArgument(!isNull(buyingActivityDto.getBuyingActivityDefinition().getId()),"illegal.param");
            //检测是否存在
            BuyingActivityDefinition bd = buyingActivityDefinitionDao.findById(buyingActivityDto.getBuyingActivityDefinition().getId());
            checkState(!isNull(bd), "buying.activity.definition.not.found");

            result.setResult(buyingActivityManger.update(buyingActivityDto, userId));
            return result;
        }catch (IllegalArgumentException e){
            log.error("params can not be null");
            result.setError(e.getMessage());
            return result;
        }catch (IllegalStateException e){
            log.error("failed to find buyingActivityDefinition(id = {}),error:{}",buyingActivityDto.getBuyingActivityDefinition().getId(),e.getMessage());
            result.setError(e.getMessage());
            return result;
        }catch (ServiceException e){
            log.error("failed to create buyingActivityDefinition buyingActivityDto={}, cause:{}", buyingActivityDto.toString(), Throwables.getStackTraceAsString(e));
            result.setError(e.getMessage());
            return result;
        }catch (Exception e) {
            log.error("failed to update buyingActivityDefinition buyingActivityDto={}, cause:{}", buyingActivityDto.toString(), Throwables.getStackTraceAsString(e));
            result.setError("buying.activity.definition.update.failed");
        }
        return result;
    }

    @Override
    public Response<Boolean> delete(Long id) {
        Response<Boolean> result = new Response<Boolean>();
        try {
            checkArgument(!isNull(id), "illegal.param");
            result.setResult(buyingActivityDefinitionDao.delete(id));
            return result;
        }catch (IllegalArgumentException e){
            log.error("params can not be null");
            result.setError(e.getMessage());
            return result;
        }catch (Exception e) {
            log.error("failed to delete buyingActivityDefinition (id={}), cause:{}", id, Throwables.getStackTraceAsString(e));
            result.setError("buying.activity.definition.delete.failed");
        }
        return result;
    }

    @Override
    public Response<BuyingActivityDefinition> findById(Long id) {
        Response<BuyingActivityDefinition> result = new Response<BuyingActivityDefinition>();

        try {
            checkArgument(!isNull(id),"illegal.param");
            BuyingActivityDefinition bd = buyingActivityDefinitionDao.findById(id);
            checkState(!isNull(bd), "buying.activity.definition.not.found");
            result.setResult(bd);
            return result;
        }catch (IllegalArgumentException e){
            log.error("params can not be null");
            result.setError(e.getMessage());
            return result;
        }catch (IllegalStateException e){
            log.error("failed to find buyingActivityDefinition(id = {}),error:{}",id,e.getMessage());
            result.setError(e.getMessage());
            return result;
        }catch (Exception e) {
            log.error("failed to find buyingActivityDefinition(id = {}), cause:{}", id, Throwables.getStackTraceAsString(e));
            result.setError("buying.activity.definition.query.failed");
            return result;
        }
    }

    /**
     * 分页查询抢购活动定义
     * @param name 活动标题
     * @param sellerId 卖家id
     * @param businessId 频道
     * @param strDate 开始时间
     * @param endDate 结束时间
     * @param status 状态
     * @param pageNo 当前页码
     * @param size  每页显示多少条
     * @param user  当前用户
     * @return  活动集合
     */
    @Override
    public Response<Paging<BuyingActivityDefinition>> pagingBySeller(@ParamInfo("name") @Nullable String name,
                                                                  @ParamInfo("sellerId") @Nullable Long sellerId,
                                                                  @ParamInfo("businessId") @Nullable Long businessId,
                                                                  @ParamInfo("strDate") @Nullable String strDate,
                                                                  @ParamInfo("endDate") @Nullable String endDate,
                                                                  @ParamInfo("status") @Nullable String status,
                                                                  @ParamInfo("pageNo") @Nullable Integer pageNo,
                                                                  @ParamInfo("size") @Nullable Integer size,
                                                                  @ParamInfo("baseUser") BaseUser user) {

        return paging(name, user.getId(), businessId, strDate, endDate, status, pageNo, size);
    }



    @Override
    public Response<Paging<BuyingActivityDefinition>> paging(@ParamInfo("name") @Nullable String name,
                                                             @ParamInfo("sellerId") @Nullable Long sellerId,
                                                             @ParamInfo("businessId") @Nullable Long businessId,
                                                             @ParamInfo("strDate") @Nullable String strDate,
                                                             @ParamInfo("endDate") @Nullable String endDate,
                                                             @ParamInfo("status") @Nullable String status,
                                                             @ParamInfo("pageNo") @Nullable Integer pageNo,
                                                             @ParamInfo("size") @Nullable Integer size) {

        PageInfo page = new PageInfo(pageNo, size);

        Response<Paging<BuyingActivityDefinition>> result = new Response<Paging<BuyingActivityDefinition>>();
        Map<String, Object> params = Maps.newHashMap();
        params.put("offset", page.offset);
        params.put("limit", page.limit);
        if (!Strings.isNullOrEmpty(name)) {
            params.put("activityName", name);
        }
        if(!isNull(sellerId)){
            params.put("sellerId",sellerId);
        }
        if(!isNull(businessId)){
            params.put("businessId",businessId);
        }
        if (!Strings.isNullOrEmpty(strDate)) {
            params.put("startAt", strDate);
        }
        if (!Strings.isNullOrEmpty(endDate)) {
            params.put("endAt", endDate);
        }
        if (!Strings.isNullOrEmpty(status)) {
            params.put("status", Integer.valueOf(status));
        }
        try{

            Paging<BuyingActivityDefinition> paging = buyingActivityDefinitionDao.paging(params);
            result.setResult(paging);
        }catch (Exception e){
            log.error("fail paging BuyingActivityDefinition by params={},cause:{}",params,Throwables.getStackTraceAsString(e));
            result.setError("fail.paging.buying.activity.definition");
        }
        return result;
    }

    @Override
    public Response<BuyingActivityDto> fingBuyingActivityDtoById(@ParamInfo("id") @Nullable String id,
                                                                 @ParamInfo("type") @Nullable Boolean preview,
                                                                 @ParamInfo("baseUser") BaseUser baseUser) {
        Response<BuyingActivityDto> result = new Response<BuyingActivityDto>();
        try {
            BuyingActivityDto buyingActivityDto = new BuyingActivityDto();

            if(!isNull(id)){

                BuyingActivityDefinition buyingActivityDefinition = buyingActivityDefinitionDao.findById(Long.valueOf(id));
                checkState(!isNull(buyingActivityDefinition), "buying.activity.definition.not.found");
                List<BuyingItem> itemList = buyingItemDao.findByActivityId(Long.valueOf(id));

                buyingActivityDto.setBuyingActivityDefinition(buyingActivityDefinition);
                buyingActivityDto.setItemList(itemList);
                buyingActivityDto.setPreview(preview);
            }
            Long userId = baseUser.getId();
            log.info("valid isehaier userId ={}",userId);
            log.info("valid isehaier eHaierSellerId ={}",eHaierSellerId);
            if(Objects.equal(eHaierSellerId,userId.toString())){
                buyingActivityDto.setIsEhaier(Boolean.TRUE);
            }else {
                buyingActivityDto.setIsEhaier(Boolean.FALSE);
            }

            result.setResult(buyingActivityDto);
            return result;
        }catch (IllegalStateException e){
            log.error("buying activity definition not found");
            result.setError(e.getMessage());
        }catch (Exception e){
            log.error("fail find buying activity dto by activity id={},cause:{}",id,Throwables.getStackTraceAsString(e));
            result.setError("buying.activity.definition.query.failed");
        }
        return result;
    }

    @Override
    public void batchUpdateStatus() {

        log.info("execute updateToRuning start date={},",DATE_TIME_FORMAT.print(new DateTime(new Date())));
        buyingActivityDefinitionDao.updateToRuning(new Date());
        log.info("execute updateToRuning end date={},", DATE_TIME_FORMAT.print(new DateTime(new Date())));

        log.info("execute updateToFinish start date={},", DATE_TIME_FORMAT.print(new DateTime(new Date())));
        Date now = new Date();
        List<BuyingActivityDefinition> activities = buyingActivityDefinitionDao.findAboutToStop(now);
        if (activities!=null && !activities.isEmpty()) {
            for (final BuyingActivityDefinition activity : activities) {
                // Try to collect all orders within the same buying activity for later scan.
                executorService.submit(new BuyingActivityFinishTask("FinishActivityTask-"+activity.getId()) {
                    @Override
                    public void run() {
                        List<BuyingOrderRecord> records = buyingOrderRecordDao.findByActivityId(activity.getId());
                        if (records==null || records.isEmpty())
                            return;
                        final ArrayList<Long> ids = new ArrayList<Long>(records.size());
                        for (BuyingOrderRecord record : records) {
                            ids.add(record.getOrderId());
                        }
                        DateTime now = new DateTime(new Date());
                        final String timeOutKey = "BUYING-FINISHED:"+TIME_OUT_FORMATTER.print(now.plusHours(activity.getPayLimit())) + ":" + Math.round(now.getMinuteOfHour()/5)*5;
                        log.info("schedule activity id={} finished task after limited hours key={}", activity.getId(), timeOutKey);
                        jedisTemplate.execute(new JedisTemplate.JedisActionNoResult() {
                            @Override
                            public void action(Jedis jedis) {
                                for (Long id : ids)
                                    jedis.lpush(timeOutKey,String.valueOf(id));
                            }
                        });
                    }
                });
            }
        }
        buyingActivityDefinitionDao.updateToFinish(now);
        buyingActivityDefinitionDao.updateToStop(now);
        log.info("execute updateToFinish end date={},", DATE_TIME_FORMAT.print(new DateTime(new Date())));

        // Scan for existing redis key for possible unpaid orders.
        final String timeOutKey = "BUYING-FINISHED:"+TIME_OUT_FORMATTER.print(new DateTime(now)) + ":" + Math.round(new DateTime(now).getMinuteOfHour()/5)*5;
        executorService.submit(new BuyingActivityFinishTask("ExpireUnpaidOrderTask-"+timeOutKey) {
            @Override
            public void run() {
                JedisTemplate.JedisAction<ArrayList<Long>> fetchOrdersAction = new JedisTemplate.JedisAction<ArrayList<Long>>() {
                    @Override
                    public ArrayList<Long> action(Jedis jedis) {
                        ArrayList<Long> ids = null;
                        if (jedis.exists(timeOutKey)) {
                            List<String> values = jedis.lrange(timeOutKey, 0, -1);
                            if (values != null && !values.isEmpty()) {
                                ids = new ArrayList<Long>(values.size());
                                for (String value : values) {
                                    ids.add(Long.valueOf(value));
                                }
                            }
                            jedis.del(timeOutKey);
                        }
                        return ids;
                    }
                };
                ArrayList<Long> ids = jedisTemplate.execute(fetchOrdersAction);
                if (ids!=null && !ids.isEmpty()) {
                    log.info("Scan {} orders for unpaid order begin.", ids.size());
                    orderWriteService.notPaidExpire(ids);
                    log.info("Scan {} orders done.", ids.size());
                }
            }
        });
    }

    @Override
    public Response<Map<String, Object>> findDetails(@ParamInfo("itemId") Long itemId, @ParamInfo("activityId") Long activityId) {
        Response<Map<String, Object>> result = new Response<Map<String, Object>>();
        Map<String, Object> detail = new HashMap<String, Object>();
        try{
            checkArgument(!isNull(itemId),"illegal.param");
            checkArgument(!isNull(activityId),"illegal.param");

            //活动信息
            BuyingActivityDefinition buyingActivityDefinition = buyingActivityDefinitionDao.findById(activityId);
            checkState(!isNull(buyingActivityDefinition), "buying.activity.definition.not.found");
            //判断该商品是否属于该活动
            BuyingItem buyingItem = buyingItemDao.findByActivityIdAnditemId(activityId,itemId);
            checkState(!isNull(buyingItem), "buying.item.not.found");

            detail.put("activity",buyingActivityDefinition);

            detail.put("buyingItem",buyingItem);

            //送达承诺
            Response<Item> itemResponse = itemService.findById(itemId);

            checkState(itemResponse.isSuccess(), itemResponse.getError());
            Item item = itemResponse.getResult();
            if(notNull(item.getDeliveryMethodId())){
                Response<DeliveryMethod> deliveryMethodResponse =deliveryMethodService.findById(item.getDeliveryMethodId());
                checkState(deliveryMethodResponse.isSuccess(), deliveryMethodResponse.getError());
                detail.put("delivery",deliveryMethodResponse.getResult().getName());
            }

            Integer saleQuantity  = buyingTempOrderDao.getSaleQuantity(activityId,itemId);
            if(isNull(saleQuantity)){
                saleQuantity=0;
            }
            detail.put("saleQuantity",saleQuantity);        //已抢购数量



            //商品信息
            Response<Map<String, Object>> itemResult = itemService.findWithDetailsById(itemId);
            Map<String, Object> itemMap = itemResult.getResult();

            detail.putAll(itemMap);
            result.setResult(detail);
            return result;
        }catch (IllegalArgumentException e){
            log.error("param can not null");
            result.setError(e.getMessage());
            return result;
        }catch (IllegalStateException e){
            log.error("buying activity definition not found");
            result.setError(e.getMessage());
            return result;
        }catch (Exception e){
            log.error("fail find buying activity definition by activity(id={}) and item(id={}),cause:{}",activityId,itemId,Throwables.getStackTraceAsString(e));
            result.setError("fail.find.buying.activity.item");
            return result;
        }

    }

    @Override
    public Response<BuyingPreOrder> buyingPreOrder(Long skuId,Integer quantity,Long buyingTempOrderId) {
        Response<BuyingPreOrder> result = new Response<BuyingPreOrder>();

        try {
            BuyingPreOrder buyingPreOrder = getBuyingPreOrder(skuId,quantity,buyingTempOrderId);
            if(buyingPreOrder == null) {
                result.setError("buying.pre.order.fail");
                return result;
            }

            buyingPreOrder.setSystemDate(DATE_TIME_FORMAT.print(DateTime.now()));
            result.setResult(buyingPreOrder);
            return result;

        }catch (Exception e) {
            log.error("fail to pre view buying order by skuId={}, quantity={},temp order id={}, cause:{}",
                    skuId, quantity, buyingTempOrderId, Throwables.getStackTraceAsString(e));
            result.setError("buying.pre.order.fail");
            return result;
        }
    }

    private BuyingPreOrder getBuyingPreOrder(Long skuId, Integer quantity, Long buyingTempOrderId) {

        SkuAndItem skuAndItem = getSkuAndItem(skuId);

        if(skuAndItem == null) {
            log.error("fail to get sku and item by sku id={}, quantity={}",skuId, quantity);
            return null;
        }

        Sku sku = skuAndItem.getSku();
        Item item = skuAndItem.getItem();

        BuyingTempOrder tempOrder = buyingTempOrderDao.findById(buyingTempOrderId);

        BuyingItem buyingItem = buyingItemDao.findByActivityIdAnditemId(tempOrder.getBuyingActivityId(),item.getId());

        RichOrderItem roi = makeRichOrderItem(sku,item,buyingItem.getItemBuyingPrice(),quantity,tempOrder.getTradeInfoId());

        Response<Shop> shopR = shopService.findByUserId(item.getUserId());
        if(!shopR.isSuccess() || shopR.getResult() == null) {
            log.error("fail to find shop by user id={} when pre view buying order, error code:{}",
                    item.getUserId(), shopR.getError());
            return null;
        }
        Shop shop = shopR.getResult();

        return makeBuyingPreOrder(shop,roi,tempOrder,buyingItem.getDiscount(),quantity);
    }

    private RichOrderItem makeRichOrderItem(Sku sku, Item item, Integer buyingPrice, Integer quantity, Long tradeInfoId) {
        RichOrderItem roi = new RichOrderItem();
        roi.setSku(sku);
        roi.setItemName(item.getName());
        roi.setItemImage(item.getMainImage());
        roi.setFee(buyingPrice * quantity);
        roi.setCount(quantity);
        roi.setDeliverFee(getDeliverFee(tradeInfoId, item, quantity));
        roi.setDeliveryPromise(getDeliverPromise(item.getDeliveryMethodId()));
        return roi;
    }

    private BuyingPreOrder makeBuyingPreOrder(Shop shop, RichOrderItem roi, BuyingTempOrder tempOrder,
                                              Integer discount, Integer quantity) {
        BuyingPreOrder buyingPreOrder = new BuyingPreOrder();
        buyingPreOrder.setDiscount(discount*quantity);
        buyingPreOrder.setSellerId(shop.getUserId());
        buyingPreOrder.setBuyingTempOrderId(tempOrder.getId());
        buyingPreOrder.setTradeInfoId(tempOrder.getTradeInfoId());
        buyingPreOrder.setBuyingActivityId(tempOrder.getBuyingActivityId());
        buyingPreOrder.setEInvoice(shop.getEInvoice());
        buyingPreOrder.setIsCod(shop.getIsCod());
        buyingPreOrder.setShopName(shop.getName());
        buyingPreOrder.setVatInvoice(shop.getVatInvoice());
        buyingPreOrder.setRichOrderItem(roi);
        buyingPreOrder.setIsEhaier(Objects.equal(eHaierSellerId,shop.getUserId().toString()));
        return buyingPreOrder;
    }

    private SkuAndItem getSkuAndItem(Long skuId) {
        Response<Sku> skuR = itemService.findSkuById(skuId);
        if(!skuR.isSuccess()) {
            log.error("fail to find sku by id={} when pre view buying order, error code:{}",
                    skuId, skuR.getError());
            return null;
        }
        Sku sku = skuR.getResult();

        Response<Item> itemR = itemService.findById(sku.getItemId());
        if(!itemR.isSuccess()) {
            log.error("fail to find item by id={} when pre view buying order, error code:{}",
                    sku.getItemId(), itemR.getError());
            return null;
        }
        Item item = itemR.getResult();

        SkuAndItem skuAndItem = new SkuAndItem();
        skuAndItem.setItem(item);
        skuAndItem.setSku(sku);

        return skuAndItem;
    }

    /**
     * 创建抢购真实订单
     *
     * @param buyer          买家
     * @param tradeInfoId    收货信息id
     * @param bank           银行编号
     * @param buyingFatOrder 预订单
     * @return 创建的订单id
     */
    @Override
    public Response<Long> createBuyingOrder(BaseUser buyer, Long tradeInfoId, String bank,
                                            BuyingFatOrder buyingFatOrder) {
        Response<Long> result = new Response<Long>();

        try {
            if(Objects.equal(buyer.getId(), buyingFatOrder.getSellerId())) {
                log.error("buyer id is same as seller id, can't create order");
                result.setError("can.not.buy.self.item");
                return result;
            }

            if(!canOrderCreate(buyingFatOrder.getBuyingTempOrderId())) {
                log.error("buying order can not create cause now time is not between start time and end time, temp order id={}",buyingFatOrder.getBuyingTempOrderId());
                result.setError("buying.order.create.time.not.between.interval");
                return result;
            }

            SkuAndItem skuAndItem = getSkuAndItem(buyingFatOrder.getSkuId());

            Item item = skuAndItem.getItem();

            BuyingItem buyingItem = getBuyingItem(buyingFatOrder.getBuyingActivityId(), item.getId());

            //创建优惠码对象
            CodeUsage codeUsage = getCodeUsageIfNecessary(buyingFatOrder, buyer, buyingItem.getItemBuyingPrice());

            //创建订单对象
            Order order = makeOrder(buyer.getId(),tradeInfoId,bank,buyingFatOrder,skuAndItem,buyingItem.getItemBuyingPrice(),codeUsage.getDiscount());
            OrderItem orderItem = makeOrderItem(order,skuAndItem,buyingFatOrder.getQuantity(),buyingItem.getDiscount(), codeUsage.getDiscount());
            OrderExtra orderExtra = makeOrderExtra(buyer.getId(),buyingFatOrder.getBuyerNotes(),buyingFatOrder.getInvoiceType(),
                    buyingFatOrder.getInvoice(), buyingFatOrder.getDeliverTime(), order.getBusiness());

            BuyingOrderRecord buyingOrderRecord = makeBuyingOrderRecord(buyingFatOrder.getBuyingActivityId(),item.getId(),buyer.getId(),
                    buyingFatOrder.getSellerId(),buyingFatOrder.getQuantity(),buyingItem.getItemOriginPrice(),buyingItem.getDiscount());

            BuyingTempOrder buyingTempOrderToUpdate = makeBuyingTempOrder(buyingFatOrder.getBuyingTempOrderId());

            Long orderId = buyingActivityManger.createBuyingOrder(order,orderItem,orderExtra,
                    buyingOrderRecord,buyingTempOrderToUpdate, codeUsage);

            result.setResult(orderId);
            return result;

        }catch (ServiceException e) {
            log.error("fail to create buying order ,cause:{}",e.getMessage());
            result.setError(e.getMessage());
            return result;
        }catch (Exception e) {
            log.error("fail to create buying order by buyer id={}, tradeInfo id={}, bank={}, buying fat order={},cause:{}",
                    buyer.getId(), tradeInfoId, bank, buyingFatOrder, Throwables.getStackTraceAsString(e));
            result.setError("buying.order.create.fail");
            return result;
        }
    }

    /**
     * 获取优惠码信息
     */
    private CodeUsage getCodeUsageIfNecessary(BuyingFatOrder buyingFatOrder, BaseUser buyer, Integer itemBuyingPrice) {

        if(!hasUserCode(buyingFatOrder.getActivityId(), buyingFatOrder.getCodeName())) {
            return new CodeUsage();
        }

        Response<CodeUsage> codeUsageR = activityBindService.makeBuyingOrderCodeUsage(buyingFatOrder, buyer, itemBuyingPrice);
        if(!codeUsageR.isSuccess()) {
            log.error("fail to get code usage when create buying order, error code:{}", codeUsageR.getError());
            throw new ServiceException(codeUsageR.getError());
        }
        return codeUsageR.getResult();
    }

    private boolean hasUserCode(Long activityId, String codeName) {
        if(activityId == null || Strings.isNullOrEmpty(codeName)) {
            log.warn("activityId and code both can not be null, skip directly");
            return false;
        }
        return true;
    }

    /**
     * 构建订单
     */
    private Order makeOrder(Long buyerId, Long tradeInfoId, String bank, BuyingFatOrder buyingFatOrder,
                            SkuAndItem skuAndItem, Integer buyingItemPrice, Integer codeDiscount) {
        Order order = new Order();

        order.setBuyerId(buyerId);
        order.setSellerId(buyingFatOrder.getSellerId());
        order.setStatus(getStatusByPayType(buyingFatOrder.getPayType()));
        order.setType(Order.Type.PLAIN.value());
        order.setBusiness(getBusinessIdBySellerId(buyingFatOrder.getSellerId()));
        order.setTradeInfoId(tradeInfoId);
        order.setDeliverFee(getDeliverFee(tradeInfoId, skuAndItem.getItem(), buyingFatOrder.getQuantity()));
        order.setPaymentType(buyingFatOrder.getPayType());
        order.setChannel(bank);
        order.setIsBuying(Boolean.TRUE);

        Integer orderFee = buyingItemPrice * buyingFatOrder.getQuantity() + order.getDeliverFee();
        // 如果优惠码对应优惠价格不为null，在总订单金额上减去优惠价格
        if(codeDiscount != null) {
            orderFee -= codeDiscount;
        }
        order.setFee(orderFee);

        return order;
    }

    /**
     * 构建子订单
     */
    private OrderItem makeOrderItem(Order order, SkuAndItem skuAndItem, Integer quantity,
                                    Integer buyingDiscount, Integer codeDiscount) {
        OrderItem orderItem = new OrderItem();

        Item item = skuAndItem.getItem();
        Sku sku = skuAndItem.getSku();

        orderItem.setBuyerId(order.getBuyerId());
        orderItem.setSellerId(order.getSellerId());
        orderItem.setDeliverFee(order.getDeliverFee());
        orderItem.setFee(order.getFee());
        orderItem.setSkuId(sku.getId());
        orderItem.setItemId(item.getId());
        orderItem.setItemName(item.getName());
        orderItem.setBrandId(item.getBrandId());
        orderItem.setBusinessId(order.getBusiness());
        orderItem.setQuantity(quantity);
        Integer discount = buyingDiscount * quantity;
        if(codeDiscount != null) {
            discount += codeDiscount;
        }
        orderItem.setDiscount(discount);
        orderItem.setType(OrderItem.Type.PLAIN.value());
        orderItem.setStatus(order.getStatus());
        orderItem.setPayType(order.getPaymentType());
        orderItem.setChannel(order.getChannel());
        orderItem.setDeliveryPromise(getDeliverPromise(item.getDeliveryMethodId()));

        return orderItem;
    }

    /**
     * 构建orderExtra
     */
    private OrderExtra makeOrderExtra(Long buyerId, String buyerNotes, Integer invoiceType,
                                      String invoiceContext, String deliverTime, Long businessId) {

        if (notEmpty(buyerNotes) || invoiceType != null || notEmpty(deliverTime)) {

            OrderExtra orderExtra = new OrderExtra();

            orderExtra.setInvoice(invoiceContext);
            orderExtra.setBuyerNotes(buyerNotes);
            orderExtra.setDeliverTime(deliverTime);

            //没有发票信息，如果家电频道默认创建
            if (invoiceType == null) {
                if (equalWith(businessId, Business.APPLIANCE.value())) {
                    orderExtra.setInvoice(getPersonalInvoice());
                }
            }

            if(equalWith(invoiceType, Integer.valueOf(OrderExtra.Type.VAT.value()))) {

                //增值税发票根据已经填写的信息自动生成
                String vatInvoice = getExistVATInvoice(buyerId);

                if(Strings.isNullOrEmpty(vatInvoice)) {
                    log.error("fail to create order when invoice type is VAT but userVatInvoice is null");
                    throw new ServiceException("vat.invoice.not.found");
                }

                orderExtra.setInvoice(vatInvoice);
            }

            // 送达时段
            orderExtra.setDeliverTime(deliverTime);

            return orderExtra;

        } else if (equalWith(businessId, Business.APPLIANCE.value())) {  // 家电频道默认创建个人发票

            OrderExtra orderExtra = new OrderExtra();

            orderExtra.setInvoice(getPersonalInvoice());

            return orderExtra;
        }

        return null;
    }

    private BuyingOrderRecord makeBuyingOrderRecord(Long buyingActivityId, Long itemId, Long buyerId, Long sellerId,
                                                    Integer quantity, Integer itemOriginPrice, Integer discount) {

        BuyingOrderRecord buyingOrderRecord = new BuyingOrderRecord();

        buyingOrderRecord.setBuyingActivityId(buyingActivityId);
        buyingOrderRecord.setItemId(itemId);
        buyingOrderRecord.setBuyerId(buyerId);
        buyingOrderRecord.setSellerId(sellerId);
        buyingOrderRecord.setQuantity(quantity);
        buyingOrderRecord.setItemOriginPrice(itemOriginPrice);
        buyingOrderRecord.setItemBuyingPrice(itemOriginPrice - discount);
        buyingOrderRecord.setDiscount(discount);

        return buyingOrderRecord;
    }

    private BuyingTempOrder makeBuyingTempOrder(Long tempOrderId) {

        BuyingTempOrder buyingTempOrderToUpdate = new BuyingTempOrder();

        buyingTempOrderToUpdate.setId(tempOrderId);
        buyingTempOrderToUpdate.setCreatedAt(DateTime.now().toDate());
        buyingTempOrderToUpdate.setStatus(BuyingTempOrder.Status.IS_ORDER.value());

        return buyingTempOrderToUpdate;
    }

    private boolean canOrderCreate(Long tempOrderId) {

        BuyingTempOrder buyingTempOrder = buyingTempOrderDao.findById(tempOrderId);

        DateTime orderCreateStartAt = new DateTime(buyingTempOrder.getOrderStartAt());
        DateTime orderCreateEndAt = new DateTime(buyingTempOrder.getOrderEndAt());
        DateTime now = DateTime.now();

        return now.isAfter(orderCreateStartAt) && now.isBefore(orderCreateEndAt);
    }

    /**
     * 生成默认个人发票
     */
    private String getPersonalInvoice() {
        Map<String, String> mapped = Maps.newTreeMap();
        mapped.put("title", "个人");
        mapped.put("type", OrderExtra.Type.PLAIN.value());
        return JSON_MAPPER.toJson(mapped);
    }

    /**
     * 生成默认增值税发票
     */
    private String getExistVATInvoice(Long buyerId) {
        Response<UserVatInvoice> userVatInvoiceR = userVatInvoiceService.getByUserId(buyerId);
        if(!userVatInvoiceR.isSuccess() || userVatInvoiceR.getResult() == null) {
            log.error("fail to get vat invoice by userId={}, error code={}",buyerId, userVatInvoiceR.getError());
            throw new ServiceException("get.vat.invoice.fail");
        }
        Map<String, Object> mapped = Maps.newHashMap();
        mapped.put("type", OrderExtra.Type.VAT.value());
        mapped.put("vat", userVatInvoiceR.getResult());
        return JSON_MAPPER.toJson(mapped);
    }

    /**
     * 获取订单初始状态
     */
    private Integer getStatusByPayType(Integer payType) {
        if(Objects.equal(payType, Order.PayType.ONLINE.value())) {
            return Order.Status.WAIT_FOR_PAY.value();
        }
        if(Objects.equal(payType, Order.PayType.COD.value())) {
            return Order.Status.PAID.value();
        }
        log.error("unknown pay type {}", payType);
        throw new ServiceException("unknown.pay.type");
    }

    /**
     * 获取行业id
     */
    private Long getBusinessIdBySellerId(Long sellerId) {
        Response<Shop> shopR = shopService.findByUserId(sellerId);
        if (!shopR.isSuccess()) {
            log.error("failed to find shop by sellerId:{}, error code :{}", sellerId, shopR.getError());
            throw new ServiceException("shop.not.found");
        }
        final Shop shop = shopR.getResult();
        return shop.getBusinessId();
    }

    /**
     * 获取订单运费
     */
    private Integer getDeliverFee(Long userTradeInfoId, Item item, Integer quantity) {

        Response<UserTradeInfo> userTradeInfoRes = userTradeInfoService.findById(userTradeInfoId);

        if(!userTradeInfoRes.isSuccess()){
            log.error("find user trade info failed, tradeInfoId={}, error code={}", userTradeInfoId, userTradeInfoRes.getError());
            throw new ServiceException("user.trade.info.not.found");
        }

        UserTradeInfo userTradeInfo = userTradeInfoRes.getResult();

        return freightCountService.countFeeByItem(userTradeInfo.getProvinceCode(), item, quantity);
    }

    /**
     * 获取订单优惠金额
     */
    private BuyingItem getBuyingItem(Long buyingActivityId, Long itemId) {

        return buyingItemDao.findByActivityIdAnditemId(buyingActivityId, itemId);
    }

    /**
     * 获取订单送达承诺
     */
    private String getDeliverPromise(Long deliverMethodId) {
        Response<DeliveryMethod> deliveryMethodR = deliveryMethodService.findById(deliverMethodId);
        if(!deliveryMethodR.isSuccess() || deliveryMethodR.getResult() == null) {
            log.error("fail to find delivery method by id={} when create buying order",deliverMethodId);
            return null;
        }else {
            return deliveryMethodR.getResult().getName();
        }
    }

    @PreDestroy
    public void destroy() {
        log.info("shutdown buying activity executor.....");
        executorService.shutdown();
    }
}
