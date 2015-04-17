package com.aixforce.rrs.presale.service;

import com.aixforce.alipay.event.AlipayEventBus;
import com.aixforce.alipay.event.TradeCloseEvent;
import com.aixforce.alipay.request.Token;
import com.aixforce.annotations.ParamInfo;
import com.aixforce.category.model.BackCategory;
import com.aixforce.category.model.Spu;
import com.aixforce.category.service.BackCategoryService;
import com.aixforce.category.service.SpuService;
import com.aixforce.common.model.PageInfo;
import com.aixforce.common.model.Paging;
import com.aixforce.common.model.Response;
import com.aixforce.common.utils.JsonMapper;
import com.aixforce.constant.ExpireTimes;
import com.aixforce.exception.ServiceException;
import com.aixforce.item.dto.FullItem;
import com.aixforce.item.model.Brand;
import com.aixforce.item.model.DefaultItem;
import com.aixforce.item.model.Item;
import com.aixforce.item.model.Sku;
import com.aixforce.item.service.DefaultItemService;
import com.aixforce.item.service.ItemService;
import com.aixforce.rrs.code.dto.DiscountAndUsage;
import com.aixforce.rrs.grid.dao.ShopAuthorizeInfoDao;
import com.aixforce.rrs.grid.dto.AuthorizeInfo;
import com.aixforce.rrs.grid.model.ShopAuthorizeInfo;
import com.aixforce.rrs.presale.dao.*;
import com.aixforce.rrs.presale.dto.FatOrderPreSale;
import com.aixforce.rrs.presale.dto.FullItemPreSale;
import com.aixforce.rrs.presale.dto.MarketItem;
import com.aixforce.rrs.presale.dto.PreOrderPreSale;
import com.aixforce.rrs.presale.manager.PreSaleManager;
import com.aixforce.rrs.presale.model.AddressStorage;
import com.aixforce.rrs.presale.model.PreSale;
import com.aixforce.rrs.presale.model.StorageStock;
import com.aixforce.rrs.settle.service.SettlementService;
import com.aixforce.shop.model.Shop;
import com.aixforce.shop.service.ShopService;
import com.aixforce.trade.dto.RichOrderItem;
import com.aixforce.trade.model.DeliveryMethod;
import com.aixforce.trade.model.Order;
import com.aixforce.trade.model.OrderItem;
import com.aixforce.trade.service.DeliveryMethodService;
import com.aixforce.trade.service.OrderQueryService;
import com.aixforce.trade.service.OrderWriteService;
import com.aixforce.user.base.BaseUser;
import com.aixforce.user.service.AccountService;
import com.aixforce.user.service.AddressService;
import com.google.common.base.*;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static com.aixforce.common.utils.Arguments.*;
import static com.aixforce.common.utils.Dates.endOfDay;
import static com.aixforce.common.utils.Dates.startOfDay;
import static com.aixforce.common.utils.NumberValidator.gt0;
import static com.aixforce.user.util.UserVerification.isAdmin;
import static com.aixforce.user.util.UserVerification.isSiteOwner;
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;

/**
 * Created by yangzefeng on 14-2-12
 */
@Service
@Slf4j
public class PreSaleServiceImpl implements PreSaleService {
    private final static Splitter splitter = Splitter.on('$');

    private final static DateTimeFormatter DFT = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");

    private final static Splitter shopIdSpliter = Splitter.on(CharMatcher.BREAKING_WHITESPACE).omitEmptyStrings().trimResults();

    @Autowired
    private PreSaleDao preSaleDao;

    @Autowired
    private BrandsSellersDao brandsSellersDao;

    @Autowired
    private ShopAuthorizeInfoDao shopAuthorizeInfoDao;

    @Autowired
    private PreSaleManager preSaleManager;

    @Autowired
    private PreSaleRedisDao preSaleRedisDao;

    @Autowired
    private ItemService itemService;

    @Autowired
    private SpuService spuService;

    @Autowired
    private BackCategoryService backCategoryService;

    @Autowired
    private SettlementService settlementService;

    @Autowired
    private AddressService addressService;

    @Autowired
    private ShopService shopService;

    @Autowired
    private AccountService<? extends BaseUser> accountService;

    @Autowired
    private OrderQueryService orderQueryService;

    @Autowired
    private OrderWriteService orderWriteService;

    @Autowired
    private DefaultItemService defaultItemService;

    @Autowired
    private StorageStockDao storageStockDao;

    @Autowired
    private AddressStorageDao addressStorageDao;

    @Autowired
    private DeliveryMethodService deliveryMethodService;

    private LoadingCache<String, Response<List<Long>>> presaleItemId2ShopIdsCache;

    private DateTimeFormatter DFT_DATE = DateTimeFormat.forPattern("yyyy-MM-dd");



    @Autowired
    private Token token;

    @Autowired
    private AlipayEventBus alipayEventBus;

    @Autowired
    private PreSaleBuyLimitDao preSaleBuyLimitDao;

    @Value("#{app.eHaierSellerId}")
    private String eHaierSellerId;


    private static final JsonMapper JSON_MAPPER = JsonMapper.nonEmptyMapper();

    public PreSaleServiceImpl() {
        this.presaleItemId2ShopIdsCache = CacheBuilder.newBuilder()
                .expireAfterWrite(1, TimeUnit.HOURS)
                .build(new CacheLoader<String, Response<List<Long>>>() {
                    @Override
                    public Response<List<Long>> load(String key) throws Exception {
                        //key的组成是itemId+'$'+regionId
                        List<String> parts = splitter.splitToList(key);

                        return findShopCandidates(Long.valueOf(parts.get(0)), Integer.valueOf(parts.get(1)));
                    }
                });
    }

    /**
     * 根据id查找预售
     *
     * @param id 预售id
     * @return 预售
     */
    @Override
    public Response<PreSale> findById(Long id) {
        Response<PreSale> result = new Response<PreSale>();
        if (id == null) {
            log.error("presale id can not be null");
            result.setError("id.not.null.fail");
            return result;
        }
        try {
            PreSale preSale = preSaleDao.get(id);
            if (preSale == null) {
                log.error("presale(id={}) not found", id);
                result.setError("presale.not.found");
                return result;
            }
            result.setResult(preSale);
            return result;
        } catch (Exception e) {
            log.error("failed to find presale by id(id={}), cause:{}", id, Throwables.getStackTraceAsString(e));
            result.setError("presale.query.fail");
            return result;
        }
    }

    @Override
    public Response<Boolean> create(PreSale preSale, BaseUser user) {
        Response<Boolean> result = new Response<Boolean>();
        try {

            checkState(isAdmin(user) || isSiteOwner(user), "user.has.no.permission");
            checkPersistArguments(preSale);
            Spu spu = getSpu(preSale.getSpuId());                       // 获取spu
            DefaultItem defaultItem = getDefaultItemOfSpu(spu);         // 获取defaultItem
            preSale.setStatus(PreSale.Status.NOT_RELEASED.value());     // 默认未发布

            preSaleManager.create(preSale, spu, defaultItem);
            result.setResult(Boolean.TRUE);

        } catch (IllegalArgumentException e) {
            log.error("fail to create {}, error:{}", preSale, e.getMessage());
            result.setError(e.getMessage());
        } catch (IllegalStateException e) {
            log.error("fail to create {}, error:{}", preSale, e.getMessage());
            result.setError(e.getMessage());
        } catch (Exception e) {
            log.error("fail to create {}, cause:{}", preSale, Throwables.getStackTraceAsString(e));
            result.setError("preSale.create.fail");
        }

        return result;
    }

    private DefaultItem getDefaultItemOfSpu(Spu spu) {
        Response<DefaultItem> defaultItemR = defaultItemService.findDefaultItemBySpuId(spu.getId());
        checkState(defaultItemR.isSuccess(), defaultItemR.getError());
        DefaultItem defaultItem = defaultItemR.getResult();
        checkState(notNull(defaultItem), "defaultItem.not.found");
        return defaultItem;
    }

    private Spu getSpu(Long spuId) {
        Response<Spu> spuR = spuService.findById(spuId);
        checkState(spuR.isSuccess(), spuR.getError());
        return spuR.getResult();
    }

    private void checkPersistArguments(PreSale preSale) {
        checkArgument(gt0(preSale.getSpuId()), "spu.id.invalid");
        // 付尾款时间应晚于预售结束时间
        checkArgument(new DateTime(preSale.getRemainFinishAt()).isAfter(new DateTime(preSale.getPreSaleFinishAt())),
                "presale.remain.before.finish");
        checkArgument(new DateTime(preSale.getRemainFinishAt()).isAfter(new DateTime(preSale.getRemainStartAt())),
                "presale.remain.finish.before.begin");
    }



    @Override
    public Response<Boolean> update(PreSale updating, BaseUser user) {
        Response<Boolean> result = new Response<Boolean>();
        try {
            checkState(isAdmin(user) || isSiteOwner(user), "user.has.no.permission");
            checkArgument(notNull(updating.getId()), "id.can.not.be.empty");
            PreSale exist = preSaleDao.get(updating.getId());
            checkState(notNull(exist), "presale.not.found");

            checkPersistArguments(updating);
            checkArgument(equalWith(exist.getStatus(), PreSale.Status.NOT_RELEASED.value()), "presale.can.not.update");
            preSaleDao.update(updating);
            updateItemAndSkuPriceIfNeed(updating, exist);
            result.setResult(Boolean.TRUE);

        } catch (IllegalArgumentException e) {
            log.error("fail to update preSale{}, error:{}", updating, e.getMessage());
            result.setError(e.getMessage());
        } catch (IllegalStateException e) {
            log.error("fail to update preSale{}, error:{}", updating, e.getMessage());
            result.setError(e.getMessage());
        } catch (Exception e) {
            log.error("fail to update preSale{}, cause:{}", updating, Throwables.getStackTraceAsString(e));
            result.setError("preSale.update.fail");
        }

        return result;
    }

    /**
     * 修改价格之后要同步修改对应商品和sku的价格
     *
     * @param updating  待更新的预售
     * @param exist     未更新的预售
     */
    private void updateItemAndSkuPriceIfNeed(PreSale updating, PreSale exist) {
        //判断前后两个预售的定金+尾款总和是否改变
        if (equalWith(exist.getEarnest()+exist.getRemainMoney(), updating.getEarnest()+updating.getRemainMoney())) {
            return;
        }


        Long itemId = exist.getItemId();
        Item updateItem = new Item();
        updateItem.setId(itemId);
        updateItem.setPrice(updating.getEarnest() + updating.getRemainMoney());
        updateItem.setOriginPrice(updating.getPrice());

        Response<List<Sku>> skusR = itemService.findSkusByItemId(itemId);
        checkState(skusR.isSuccess(), skusR.getError());
        List<Sku> skus = skusR.getResult();
        for(Sku sku : skus) {
            sku.setPrice(updating.getEarnest() + updating.getRemainMoney());
        }

        preSaleManager.updateItemAndSku(updateItem,skus);
    }


    @Override
    public Response<Boolean> release(Long id) {
        Response<Boolean> result = new Response<Boolean>();


        try {
            checkArgument(notNull(id), "id.can.not.be.empty");

            PreSale exist = preSaleDao.get(id);
            checkState(notNull(exist), "presale.not.found");
            Date now = DateTime.now().toDate();
            checkState(now.before(exist.getPreSaleFinishAt()), "presale.need.between.start.end");

            PreSale updating = new PreSale();
            updating.setStatus(PreSale.Status.RELEASED.value());
            updating.setId(id);

            preSaleManager.release(updating);
            result.setResult(Boolean.TRUE);
        } catch (IllegalArgumentException e) {
            log.error("release preSale id={} fail, error:{}", id, e.getMessage());
            result.setError(e.getMessage());
        } catch (IllegalStateException e) {
            log.error("release preSale id={} fail, error:{}", id, e.getMessage());
            result.setError(e.getMessage());
        } catch (Exception e) {
            log.error("release preSale id={} fail, cause:{}", id, Throwables.getStackTraceAsString(e));
            result.setError("preSale.release.fail");
        }

        return result;
    }

    public Response<Boolean> stop(Long id) {
        Response<Boolean> result = new Response<Boolean>();
        try {
            checkArgument(notNull(id), "id.can.not.be.empty");
            PreSale preSale = preSaleDao.get(id);
            checkState(notNull(preSale), "presale.not.found");

            boolean success = preSaleDao.stop(id);
            checkState(success, "presale.persist.fail");

            result.setResult(Boolean.TRUE);
        } catch (IllegalArgumentException e) {
            log.error("fail to stop preSale(id:{}), error:{}", id, e.getMessage());
            result.setError(e.getMessage());
        } catch (IllegalStateException e) {
            log.error("fail to stop preSale(id:{}), error:{}", id, e.getMessage());
            result.setError(e.getMessage());
        } catch (Exception e) {
            log.error("fail to stop preSale(id:{}), cause:{}", id, Throwables.getStackTraceAsString(e));
            result.setError("presale.stop.fail");
        }

        return result;
    }


    /**
     * 更新预售的虚拟销量
     *
     * @param id       预售id
     * @param quantity 更新后的虚拟销量
     * @return 操作是否成功
     */
    @Override
    public Response<Boolean> updateQuantity(Long id, Integer quantity) {
        Response<Boolean> result = new Response<Boolean>();

        try {
            checkArgument(notNull(id), "id.can.not.be.empty");
            checkArgument(quantity >= 0, "sold.quantity.should.be.positive");

            PreSale updating = new PreSale();
            updating.setId(id);
            updating.setFakeSoldQuantity(quantity);
            boolean success = preSaleDao.update(updating);
            checkState(success, "presale.persist.fail");
            result.setResult(Boolean.TRUE);

        } catch (IllegalArgumentException e) {
            log.error("fail to update quantity:{} of presale:(id:{}), error:{}", id, quantity, e.getMessage());
            result.setError(e.getMessage());
        } catch (IllegalStateException e) {
            log.error("fail to update quantity:{} of presale:(id:{}), error:{}", id, quantity, e.getMessage());
            result.setError(e.getMessage());
        } catch (Exception e) {
            log.error("fail to update quantity:{} of presale:(id:{}), cause:{}", id, quantity, Throwables.getStackTraceAsString(e));
            result.setError("presale.update.fail");
        }
        return result;
    }

    @Override
    public Response<Paging<MarketItem>> pagination(@ParamInfo("pageNo") @Nullable Integer pageNo,
                                                   @ParamInfo("size") @Nullable Integer size,
                                                   @ParamInfo("status") @Nullable Integer status,
                                                   @ParamInfo("start") @Nullable String start,
                                                   @ParamInfo("end") @Nullable String end,
                                                   @ParamInfo("itemId") @Nullable Long itemId,
                                                   @ParamInfo("baseUser")BaseUser user) {

        Response<Paging<MarketItem>> result = new Response<Paging<MarketItem>>();
        //添加了新的逻辑->预售管理需要频道运营能够编辑和创建

        try {
            checkState(isAdmin(user) || isSiteOwner(user), "user.has.no.permission");
            PreSale criteria = new PreSale();
            if (notNull(status)) {
                criteria.setStatus(status);
            }
            if (notNull(itemId)) {
                criteria.setItemId(itemId);
            }


            Map<String, Object> params = Maps.newHashMap();
            params.put("criteria", criteria);
            PageInfo pageInfo = new PageInfo(pageNo, size);
            params.put("offset", pageInfo.offset);
            params.put("limit", pageInfo.limit);

            if (notEmpty(start)) {
                params.put("createdStartAt", startOfDay(DFT_DATE.parseDateTime(start).toDate()));
            }
            if (notEmpty(end)) {
                params.put("createdEndAt", endOfDay(DFT_DATE.parseDateTime(end).toDate()));
            }

            Paging<PreSale> paging = preSaleDao.findBy(params);
            result.setResult(convertToMarketItem(paging));
            return result;


        } catch (IllegalStateException e) {
            log.error("pagination presale fail, pageNo:{}, size:{}, status:{}, start:{}, end:{}, itemId:{}, user:{}, error:{}",
                    pageNo, size, status, start, end, itemId, user, e.getMessage());
            result.setError(e.getMessage());
        } catch (Exception e) {
            log.error("pagination presale fail, pageNo:{}, size:{}, status:{}, start:{}, end:{}, itemId:{}, user:{}, cause:{}",
                    pageNo, size, status, start, end, itemId, user, Throwables.getStackTraceAsString(e));
            result.setError("presale.query.fail");
        }

        return result;
    }

    private Paging<MarketItem> convertToMarketItem(Paging<PreSale> paging) {
        List<PreSale> preSales = paging.getData();
        List<MarketItem> marketItems = Lists.newArrayListWithCapacity(preSales.size());

        for (PreSale ps : preSales) {
            try {
                Long itemId = ps.getItemId();
                Item item = itemService.findById(itemId).getResult();
                MarketItem mi = new MarketItem();
                mi.setItem(item);
                mi.setPreSale(ps);
                marketItems.add(mi);
            } catch (Exception e) {
                log.error("fail to convert presale:{} to marketItem, cause:{}, skipped",
                        ps, Throwables.getStackTraceAsString(e));
            }
        }

        return new Paging<MarketItem>(paging.getTotal(), marketItems);
    }


    @Override
    public Response<Paging<MarketItem>> paginationByUser(Integer pageNo, Integer size) {
        Response<Paging<MarketItem>> result = new Response<Paging<MarketItem>>();
        try {
            PreSale criterion = new PreSale();
            criterion.setStatus(PreSale.Status.RUNNING.value());//只展示运行中
            Paging<MarketItem> marketItemPaging = paginationMarketItem(pageNo, size, criterion);
            result.setResult(marketItemPaging);
            return result;
        } catch (Exception e) {
            log.error("pagination marketItem fail, cause:{}", e);
            result.setError("preSale.query.fail");
            return result;
        }
    }

    private Paging<MarketItem> paginationMarketItem(Integer pageNo, Integer size, PreSale criterion) {
        PageInfo pageInfo = new PageInfo(pageNo, size);
        List<PreSale> preSales = preSaleDao.findByCriterion(criterion, pageInfo.getOffset(), pageInfo.getLimit());
        List<MarketItem> marketItems = Lists.newArrayListWithCapacity(preSales.size());
        for (PreSale ps : preSales) {
            try {
                Long itemId = ps.getItemId();
                Item item = itemService.findById(itemId).getResult();
                MarketItem mi = new MarketItem();
                mi.setItem(item);
                mi.setPreSale(ps);
                marketItems.add(mi);
            } catch (Exception e) {
                log.error("fail to find item by preSale={}, cause:{}, skip it",
                        ps, Throwables.getStackTraceAsString(e));
            }
        }
        Long total = preSaleDao.countBy(criterion);
        return new Paging<MarketItem>(total, marketItems);
    }






    @Override
    public Response<FullItemPreSale> findFullItemPreSale(Long itemId) {
        Response<FullItemPreSale> result = new Response<FullItemPreSale>();
        if (itemId == null) {
            log.error("itemId can not be null when find fullItemPreSale");
            result.setError("illegal.param");
            return result;
        }
        final Response<Item> itemR = itemService.findById(itemId);
        if (!itemR.isSuccess()) {
            log.error("failed to find item by itemId={}, error code:{}", itemId, itemR.getError());
            result.setError(itemR.getError());
            return result;
        }

        try {
            Response<Map<String, Object>> mapR = itemService.findWithDetailsById(itemId);
            if (!mapR.isSuccess()) {
                log.error("failed to find fullItem by itemId={}, error code:{}", itemId, mapR.getError());
                result.setError("item.not.found");
                return result;
            }
            Map<String, Object> map = mapR.getResult();
            FullItem fullItem = (FullItem) map.get("fullItem");
            PreSale preSale = preSaleDao.findByItemId(itemId);
            FullItemPreSale fips = new FullItemPreSale();
            fips.setPreSale(preSale);
            fips.setFullItem(fullItem);
            result.setResult(fips);
            return result;
        } catch (Exception e) {
            log.error("failed to find fullItemPreSale with itemId={},cause:{}", itemId, Throwables.getStackTraceAsString(e));
            result.setError("item.not.found");
            return result;
        }
    }

    @Override
    public Response<PreOrderPreSale> preOrderPreSale(String skus, Integer regionId, BaseUser baseUser) {
        Response<PreOrderPreSale> result = new Response<PreOrderPreSale>();
        if (Strings.isNullOrEmpty(skus)) {
            log.warn("skus can not be empty");
            result.setError("order.preOrder.fail");
            return result;
        }
        try {
            Map<Long, Integer> skuIdAndQuantity = JSON_MAPPER.fromJson(skus, JSON_MAPPER.createCollectionType(HashMap.class, Long.class, Integer.class));
            //从所有符合授权信息的商家中随机选择一个商家
            Long skuId = null;
            int quantity = 0;
            for (Long id : skuIdAndQuantity.keySet()) {
                quantity = skuIdAndQuantity.get(id);
                skuId = id;
            }
            Response<Sku> skuR = itemService.findSkuById(skuId);
            if (!skuR.isSuccess()) {
                log.error("fail to find sku by id={}, error code:{}", skuId, skuR.getError());
                result.setError(skuR.getError());
                return result;
            }
            Sku sku = skuR.getResult();
            Long itemId = sku.getItemId();

            PreSale preSale = preSaleDao.findByItemId(itemId);
            Long shopId;
            //如果是分仓的预售, 直接取预售绑定的店铺id就好, 但是需要判定对应的库存是否足够
            if (Objects.equal(preSale.getByStorage(), Boolean.TRUE)) {
                try {
                    boolean hasEnoughStock = enoughStock(itemId, regionId, quantity);
                    if (!hasEnoughStock) { //没有足够的库存
                        log.warn("no enough stock for itemId={}, regionId={}, count={}", itemId, regionId, quantity);
                        PreOrderPreSale fops = new PreOrderPreSale();
                        fops.setStockNotEnough(Boolean.TRUE);
                        result.setResult(fops);
                        return result;
                    }
                    shopId = Long.valueOf(preSale.getShopIds());
                } catch (Exception e) {
                    log.error("failed to handle presale(id={}) , its shopIds is {}, cause:{}",
                            preSale.getId(), preSale.getShopIds(), Throwables.getStackTraceAsString(e));
                    result.setError("order.preOrder.fail");
                    return result;
                }
            } else {//不是分仓的预售, 按照原有逻辑走
                Response<List<Long>> shopIdsR = presaleItemId2ShopIdsCache.getUnchecked(itemId + "$" + regionId);
                if (!shopIdsR.isSuccess()) {
                    result.setError(shopIdsR.getError());
                    return result;
                }
                List<Long> shopIds = shopIdsR.getResult();
                //商品id mod 符合授权店铺数量， 保证详情页和下单预览页的商家是同一个
                Long index = itemId % shopIds.size();
                shopId = shopIds.get(index.intValue());
            }


            Response<Shop> shopR = shopService.findById(shopId);
            if (!shopR.isSuccess()) {
                log.error("fail to find shop by id={}, error code:{}", shopId, shopR.getError());
                result.setError(shopR.getError());
                return result;
            }
            Shop shop = shopR.getResult();
            Long sellerId = shop.getUserId();

            Response<Item> itemR = itemService.findById(itemId);
            if (!itemR.isSuccess()) {
                log.error("fail to find item by id={},error code:{}", itemId, itemR.getError());
                result.setError(itemR.getError());
                return result;
            }

            Item item = itemR.getResult();

            Response<? extends BaseUser> ur = accountService.findUserById(sellerId);
            String sellerName = ur.getResult().getName();
            RichOrderItem roi = new RichOrderItem();
            roi.setSku(sku);
            roi.setItemName(item.getName());
            roi.setItemImage(item.getMainImage());
            roi.setFee(sku.getPrice() * quantity);
            roi.setCount(quantity);

            Response<DeliveryMethod> deliveryMethodR = deliveryMethodService.findById(item.getDeliveryMethodId());
            if(!deliveryMethodR.isSuccess() || deliveryMethodR.getResult() == null) {
                log.error("fail to find delivery method by id={}, error code={}",
                        item.getDeliveryMethodId(), deliveryMethodR.getError());
            }else {
                roi.setDeliveryPromise(deliveryMethodR.getResult().getName());
            }

            PreOrderPreSale fops = new PreOrderPreSale();
            fops.setPreSale(preSale);
            fops.setRichOrderItem(roi);
            fops.setSellerId(sellerId);
            fops.setIsCod(shop.getIsCod());
            fops.setSellerName(sellerName);
            fops.setShopName(shop.getName());
            fops.setEInvoice(shop.getEInvoice());
            fops.setVatInvoice(shop.getVatInvoice());
            fops.setIsEhaier(Objects.equal(eHaierSellerId,shop.getUserId().toString())); //是否是ehaier商家
            DateTime startDate = new DateTime(preSale.getRemainStartAt());
            fops.setRemainStartAt(DFT.print(startDate)); //尾款开始时间
            result.setResult(fops);
            return result;
        } catch (Exception e) {
            log.error("failed to find preSale fatOrder by skus{}, cause:{}", skus, e);
            result.setError("order.preOrder.fail");
            return result;
        }
    }

    /**
     * 根据商品id和地区id为预售订单匹配符合授权的商家
     *
     * @param itemId   预售订单对应的商品id
     * @param regionId 区域id,from cookie
     * @param count    购买数量
     * @return 返回符合授权的商家店铺id列表，如没有匹配到，返回false
     */
    @Override
    public Response<List<Long>> findShopForPreOrder(Long itemId, Integer regionId, Integer count) {
        Response<List<Long>> result = new Response<List<Long>>();
        try {
            PreSale preSale = preSaleDao.findByItemId(itemId);
            if (preSale == null) {
                log.error("failed to find preSale(itemId={})", itemId);
                result.setError("preSale.not.found");
                return result;
            }
            //如果是分仓的预售, 直接取预售绑定的店铺id就好, 但是需要判定对应的库存是否足够
            if (Objects.equal(preSale.getByStorage(), Boolean.TRUE)) {
                try {
                    boolean hasEnoughStock = enoughStock(itemId, regionId, count);
                    if (!hasEnoughStock) { //没有足够的库存
                        log.warn("no enough stock for itemId={}, regionId={}, count={}", itemId, regionId, count);
                        result.setError("storage.stock.not.enough");
                        return result;
                    }
                    final ImmutableList<Long> shopId = ImmutableList.of(Long.valueOf(preSale.getShopIds()));
                    result.setResult(shopId);
                    return result;
                } catch (Exception e) {
                    log.error("failed to handle presale(id={}) , its shopIds is {}, cause:{}",
                            preSale.getId(), preSale.getShopIds(), Throwables.getStackTraceAsString(e));
                    result.setError("order.preOrder.fail");
                    return result;
                }
            } else {//不是分仓的预售, 按照原有逻辑走
                return presaleItemId2ShopIdsCache.getUnchecked(itemId + "$" + regionId);
            }
        } catch (Exception e) {
            log.error("failed to find shop for pre sale(itemId={}), cause:{}", itemId, Throwables.getStackTraceAsString(e));
            result.setError("order.preOrder.fail");
            return result;
        }
    }

    /**
     * 根据商品id和地区id为预售订单匹配符合授权的商家
     * 实现思路: 首先根据itemId可以找到spuId, 进而找到品牌id和二级类目,
     * 再从brand_sellers表中可以找到可以卖这个品牌的商家列表(这里要求创建授权的时候, 就要写brand_sellers表),
     * 这样只需要根据商家的id列表去找商家的授权信息, 进行地区和二级类目的匹配即可.
     * <p/>
     * 一个优化, 创建一个 key为 itemId+regionId, value为shopId列表的缓存, 5分钟过期
     *
     * @param itemId   预售订单对应的商品id
     * @param regionId 区域id,from cookie
     * @return 返回符合授权的商家店铺id列表，如没有匹配到，返回false
     */
    private Response<List<Long>> findShopCandidates(Long itemId, Integer regionId) {
        Response<List<Long>> result = new Response<List<Long>>();
        PreSale preSale = preSaleDao.findByItemId(itemId);
        if (preSale == null) {
            log.error("failed to find preSale(itemId={})", itemId);
            result.setError("preSale.not.found");
            return result;
        }

        Long spuId = preSale.getSpuId();
        Response<Spu> spuR = spuService.findById(spuId);
        if (!spuR.isSuccess()) {
            log.error("fail to find spu by id{}, error code:{}", spuId, spuR.getError());
            result.setError(spuR.getError());
            return result;
        }
        Spu spu = spuR.getResult();

        Long categoryId = spu.getCategoryId();
        Response<List<Long>> categoryAncestorsR = backCategoryService.ancestorsOfNoCache(categoryId);
        if (!categoryAncestorsR.isSuccess()) {
            log.error("fail to find ancestors by categoryId={}, error code:{}", categoryId, categoryAncestorsR.getError());
            result.setError(categoryAncestorsR.getError());
            return result;
        }
        List<Long> categoryAncestors = categoryAncestorsR.getResult();
        categoryId = categoryAncestors.get(1); //找二级类目

        //根据品牌去找shopId列表
        Long brandId = spu.getBrandId().longValue();

        List<Long> shopIds;

        String shopIdsString = preSale.getShopIds();

        if (!Strings.isNullOrEmpty(preSale.getShopIds())) {//如果绑定了店铺id, 就不用去数据库中找了
            shopIds = Lists.transform(shopIdSpliter.splitToList(shopIdsString), new Function<String, Long>() {
                @Override
                public Long apply(String shopId) {
                    return Long.valueOf(shopId);
                }
            });
        } else {
            shopIds = brandsSellersDao.findShopIdsByBrandId(brandId);
            if (shopIds.isEmpty()) {
                log.warn("no shops are authorized to sell this brand(id={})", brandId);
                result.setError("order.preOrder.fail");
                return result;
            }

        }

        Response<List<Integer>> regionsR = addressService.ancestorsOf(regionId);
        if (!regionsR.isSuccess()) {
            log.error("fail to find ancestors by regionId {},error code:{}", regionId, regionsR.getError());
            result.setError(regionsR.getError());
            return result;
        }
        List<Long> regionAncestors = Lists.transform(regionsR.getResult(), new Function<Integer, Long>() {
            @Override
            public Long apply(Integer input) {
                return input.longValue();
            }
        });


        List<Long> candidates = Lists.newArrayListWithCapacity(shopIds.size());
        for (Long shopId : shopIds) {
            try {
                //根据shopId去检查授权
                List<ShopAuthorizeInfo> shopAuthorizeInfos = shopAuthorizeInfoDao.findByShopId(shopId);
                for (ShopAuthorizeInfo sai : shopAuthorizeInfos) {
                    AuthorizeInfo authorizeInfo = JSON_MAPPER.fromJson(sai.getJsonAuthorize(), AuthorizeInfo.class);

                    //检查品牌是否已授权, 实际上这里是一个double check, 因为前面已经根据品牌去找shopId了
                    List<Brand> brands = authorizeInfo.getBrands();
                    boolean hasBrand = false;
                    for (Brand brand : brands) {
                        if (Objects.equal(brand.getId(), brandId)) {
                            hasBrand = true;
                            break;
                        }
                    }
                    if (!hasBrand) { //品牌未授权, 略过
                        continue;
                    }

                    //检查类目是否授权
                    List<BackCategory> backCategories = authorizeInfo.getCategories();
                    boolean hasCategory = false;
                    for (BackCategory backCategory : backCategories) {
                        if (Objects.equal(backCategory.getId(), categoryId)) {
                            hasCategory = true;
                            break;
                        }
                    }
                    if (!hasCategory) { //类目未授权, 略过
                        continue;
                    }

                    //检查地区是否授权
                    Set<Long> regionIds = Sets.newHashSet();
                    //找出本条授权信息的地区列表
                    for (Map<String, List<Long>> regions : authorizeInfo.getRegions()) {
                        for (List<Long> rids : regions.values()) {
                            regionIds.addAll(rids);
                        }
                    }
                    boolean hasRegion = false;
                    for (Long rid : regionAncestors) {
                        if (regionIds.contains(rid)) { //如果授权地区包含该用户所在的地区
                            hasRegion = true;
                            break;
                        }
                    }
                    if (hasRegion) { //如果授权了该地区, 则将该店铺加入最后的结果集合, 并不需要继续检查该店铺了
                        candidates.add(shopId);
                        break;
                    }
                }
            } catch (Exception e) {
                log.error("fail to find authorized seller by brandId {},secondCategoryId {},regionId {}" +
                                "shop id={} authorize info exception, skip it,cause:{}",
                        brandId, categoryId, regionId, shopId, Throwables.getStackTraceAsString(e));
            }
        }

        //如果找不到符合授权的卖家，查询预售商品preOrder失败
        if (candidates.isEmpty()) {
            log.error("fail to find authorized seller by brandId{},secondCategoryId {},regionId {} when pre view preSale order",
                    brandId, categoryId, regionId);
            result.setError("find.authorize.shop.fail");
            return result;
        }
        result.setResult(candidates);
        return result;

    }

    @Override
    public Response<Long> createPreSaleOrder(Long buyerId, Long tradeInfoId,Integer regionId, FatOrderPreSale fatOrderPreSale,
                                             DiscountAndUsage discountAndUsage,String bank) {
        Response<Long> result = new Response<Long>();
        if (buyerId == null || tradeInfoId == null) {
            log.error("buyerId and tradeInfoId all required");
            result.setError("illegal.param");
            return result;
        }

        Long id = fatOrderPreSale.getPreSale().getId();


        PreSale preSale = preSaleDao.get(id);
        if (preSale == null) {
            log.error("presale(id={}) is not found", id);
            result.setError("presale.not.found");
            return result;
        }


        //如果是分仓的预售, 直接取预售绑定的店铺id就好, 但是需要判定对应的库存是否足够
        if (Objects.equal(preSale.getByStorage(), Boolean.TRUE)) {
            try {
                Map<Long, Integer> skuIdAndQuantity = fatOrderPreSale.getSkuIdAndQuantity();
                final Integer quantity = Lists.newArrayList(skuIdAndQuantity.values()).get(0);
                final Long itemId = preSale.getItemId();

                boolean hasEnoughStock = enoughStock(itemId, regionId, quantity);
                if (!hasEnoughStock) { //没有足够的库存
                    log.warn("no enough stock for itemId={}, regionId={}, count={}", itemId, regionId, quantity);
                    result.setError("storage.stock.not.enough");
                    return result;
                }

            } catch (Exception e) {
                log.error("failed to handle presale(id={}) , its shopIds is {}, cause:{}",
                        preSale.getId(), preSale.getShopIds(), Throwables.getStackTraceAsString(e));
                result.setError("order.preOrder.fail");
                return result;
            }
        }

        Response<Shop> shopR = shopService.findByUserId(fatOrderPreSale.getSellerId());
        if (!shopR.isSuccess()) {
            log.error("cannot find shop by userId:{}, error code:{}", fatOrderPreSale.getSellerId(), shopR.getError());
            result.setError(shopR.getError());
            return result;
        }

        try {
            Long earnestId = preSaleManager.createPreSaleOrders(buyerId, tradeInfoId, regionId,fatOrderPreSale,
                    shopR.getResult().getBusinessId(), preSale, discountAndUsage, bank);
            result.setResult(earnestId);
            return result;
        } catch (ServiceException e) {
            log.error("failed to create preSale order {}, cause:{}", fatOrderPreSale, e.getMessage());
            result.setError(e.getMessage());
            return result;
        } catch (Exception e) {
            log.error("fail to create preSale order {}, cause:{}", fatOrderPreSale, Throwables.getStackTraceAsString(e));
            result.setError("preSale.order.create.fail");
            return result;
        }
    }

    @Override
    public Response<PreSale> findPreSaleByItemId(Long itemId) {
        Response<PreSale> result = new Response<PreSale>();
        if (itemId == null) {
            log.error("item id can not be null");
            result.setError("illegal.params");
            return result;
        }
        try {
            PreSale preSale = preSaleDao.findByItemId(itemId);
            result.setResult(preSale);
            return result;
        } catch (Exception e) {
            log.error("failed to find preSale by itemId{}, cause:{}", itemId, Throwables.getStackTraceAsString(e));
            result.setError("preSale.query.fail");
            return result;
        }
    }

    @Override
    public Response<Boolean> removePreSaleOrder(Long orderId) {
        Response<Boolean> result = new Response<Boolean>();
        if (orderId == null) {
            log.error("orderId cannot be null when remove preSale order");
            result.setError("illegal.params");
            return result;
        }
        try {
            preSaleRedisDao.removeOrder(orderId);
            result.setResult(Boolean.TRUE);
            return result;
        } catch (Exception e) {
            log.error("failed to remove preSale by orderId={}, cause:{}", orderId, Throwables.getStackTraceAsString(e));
            result.setError("preSale.remove.fail");
            return result;
        }
    }

    @Override
    public Response<Boolean> verifyPreSaleOrderExpire() {
        Response<Boolean> result = new Response<Boolean>();
        try {
            //redis 中维护一个所有预售商品订单的set

            Iterable<Long> orderIds = preSaleRedisDao.findAllPreSaleItemOrders();
            for (Long orderId : orderIds) {
                Response<List<OrderItem>> orderItemR = orderQueryService.findOrderItemByOrderId(orderId);
                if (!orderItemR.isSuccess()) {
                    log.error("fail to find orderItems by orderId={}, error code:{}", orderId, orderItemR.getError());
                    continue;
                }
                List<OrderItem> orderItems = orderItemR.getResult();
                for (OrderItem oi : orderItems) {
                    //判断预售付款是否超时，对状态不为等待付款的订单不做处理
                    if (!Objects.equal(oi.getStatus(), OrderItem.Status.WAIT_FOR_PAY.value()))
                        continue;
                    //如果订单已经从预售订单列表中移除了，说明定金或者尾款订单已经处理过，直接跳过
                    if(!preSaleRedisDao.orderIdExist(orderId))
                        continue;

                    Long itemId = oi.getItemId();
                    PreSale preSale = preSaleDao.findByItemId(itemId);
                    //定金订单
                    if (Objects.equal(oi.getType(), OrderItem.Type.PRESELL_DEPOSIT.value())) {
                        DateTime finishAt = new DateTime(oi.getCreatedAt()).plusHours(preSale.getEarnestTimeLimit()).plusHours(ExpireTimes.PRESALE_EARNEST_EXPIRE_HOURS);
                        if (finishAt.isBeforeNow()) {
                            log.debug("orderItem{} pay earnest expire", oi);
                            //更新子订单和总订单状态
                            Response<Boolean> orderWriteR = orderWriteService.updateOrderAndOrderItems(orderId, OrderItem.Status.CANCELED_BY_EARNEST_EXPIRE.value());
                            if (!orderWriteR.isSuccess()) {
                                log.error("fail to update order(id={}) status, error code:{}", orderId, orderWriteR.getError());
                                continue;
                            }

                            //如果是分仓的预售, 则恢复库存
                            recoverPreSaleStorageIfNecessary(orderId);

                            //恢复预售购买限制
                            recoverPreSaleBuyLimitIfNecessaryInner(oi.getBuyerId(), preSale.getId(), oi.getQuantity());

                            // 从队列中删除元素
                            preSaleRedisDao.removeOrder(orderId);

                            // 通知支付宝关闭交易
                            alipayEventBus.post(new TradeCloseEvent(token, oi.getOrderId() + "," + oi.getId()));
                            continue;
                        }
                    }
                    if(Objects.equal(oi.getType(), OrderItem.Type.PRESELL_REST.value())) {
                        //尾款订单
                        DateTime finishAt = new DateTime(preSale.getRemainFinishAt()).plusDays(ExpireTimes.NOT_PAY_EXPIRE_DAY);
                        if (finishAt.isBeforeNow()) {
                            log.debug("orderItem{} pay remain expire", oi);
                            //更新子订单和总订单状态
                            Response<Boolean> orderWriteR = orderWriteService.updateOrderAndOrderItems(orderId, OrderItem.Status.CANCELED_BY_REMAIN_EXPIRE.value());
                            if (!orderWriteR.isSuccess()) {
                                log.error("fail to update order status, error code:{}", orderWriteR.getError());
                                continue;
                            }

                            //如果是分仓的预售, 则恢复库存
                            recoverPreSaleStorageIfNecessary(orderId);

                            // 尾款超时,创建结算信息 remove valid
                           /* try {
                                createSettlementAfterExpired(orderId);
                            } catch (IllegalStateException e) {
                                log.error("fail to create settlement with orderId:{}, code:{}", orderId, e.getMessage());
                            }*/
                            preSaleRedisDao.removeOrder(orderId);


                            // 通知支付宝关闭交易
                            alipayEventBus.post(new TradeCloseEvent(token, oi.getOrderId() + "," + oi.getId()));
                        }
                    }
                }
            }
            result.setResult(Boolean.TRUE);
            return result;
        } catch (Exception e) {
            log.error("fail to verify presale orders expire, cause:", e);
            result.setError("presale.order.expire.verify.fail");
            return result;
        }
    }


    public void createSettlementAfterExpired(Long orderId) {
        Response<Long> createdResult = settlementService.generate(orderId);
        checkState(createdResult.isSuccess(), createdResult.getError());
    }


    @Override
    public Response<Boolean> verifyPreSaleExpire() {
        Response<Boolean> result = new Response<Boolean>();
        try {
            List<Long> preSaleIds = preSaleRedisDao.findAllPreSale();
            for (Long preSaleId : preSaleIds) {
                try {
                    PreSale preSale = preSaleDao.get(preSaleId);
                    if (preSale == null) {
                        log.warn("failed to find pre_sale(id={})", preSaleId);
                        //从发布列表中删除
                        preSaleRedisDao.removePreSaleById(preSaleId);
                        continue;
                    }
                    //自动上架
                    if(new DateTime((preSale.getPreSaleStartAt())).isBeforeNow() &&
                            new DateTime(preSale.getPreSaleFinishAt()).isAfterNow()) {
                        if(Objects.equal(preSale.getStatus(), PreSale.Status.RELEASED.value())) {
                            log.info("preSale {} auto running", preSale);
                            PreSale update = new PreSale();
                            update.setId(preSale.getId());
                            update.setStatus(PreSale.Status.RUNNING.value());
                            preSaleDao.update(update);
                        }
                    }
                    if(new DateTime(preSale.getPreSaleFinishAt()).isBeforeNow()) {
                        //预售过期
                        log.info("preSale{} expire", preSale);
                        PreSale update = new PreSale();
                        update.setId(preSale.getId());
                        //更新preSale status 为finish
                        update.setStatus(PreSale.Status.FINISHED.value());
                        //从发布列表中删除
                        preSaleRedisDao.removePreSaleById(preSaleId);
                        preSaleDao.update(update);
                        Long itemId = preSale.getItemId();
                        //下架商品
                        Response<Boolean> updateR = itemService.updateStatusByIds(Lists.newArrayList(itemId), Item.Status.OFF_SHELF.toNumber());
                        if (!updateR.isSuccess()) {
                            log.error("fail to update item status to OFF_SHELF, itemId={}, error code:{}",
                                    itemId, updateR.getError());
                        }
                    }
                } catch (Exception e) {
                    log.error("fail to update preSale(id={}) when expire, cause:{}",
                            preSaleId, Throwables.getStackTraceAsString(e));
                }
            }
            result.setResult(Boolean.TRUE);
            return result;
        } catch (Exception e) {
            log.error("fail to update preSale when preSale expire, cause:{}"
                    , Throwables.getStackTraceAsString(e));
            result.setError("verify.presale.expire.fail");
            return result;
        }
    }

    @Override
    public Response<Boolean> recoverPreSaleBuyLimitIfNecessary(Order order) {
        Response<Boolean> result = new Response<Boolean>();

        if (!Objects.equal(order.getType(), Order.Type.PRE_SELL.value())) {
            log.error("order id={}, is not pre sale order", order.getId());
            result.setError("order.type.incorrect");
            return result;
        }

        Response<List<OrderItem>> orderItemsR = orderQueryService.findOrderItemByOrderId(order.getId());
        if(!orderItemsR.isSuccess()) {
            log.error("fail to find orderItem by orderId={}, error code={}",order.getId(),orderItemsR.getError());
            result.setError(orderItemsR.getError());
            return result;
        }
        List<OrderItem> orderItems = orderItemsR.getResult();

        try {

            for (OrderItem oi : orderItems) {
                if (!Objects.equal(oi.getType(), OrderItem.Type.PRESELL_DEPOSIT.value())) {
                    continue;
                }
                //只对预售定金的子订单恢复购买限制
                Long itemId = oi.getItemId();
                PreSale preSale = preSaleDao.findByItemId(itemId);

                return recoverPreSaleBuyLimitIfNecessaryInner(oi.getBuyerId(),preSale.getId(),oi.getQuantity());
            }

            log.error("pre sale earnest order not found, order id={}", order.getId());
            result.setError("recover.presale.buy.limit.fail");
            return result;

        }catch (Exception e) {
            log.error("fail to recover pre sale buy limit by order id={}, cause:{}",
                    order.getId(), Throwables.getStackTraceAsString(e));
            result.setError("recover.presale.buy.limit.fail");
            return result;
        }
    }

    /**
     * 根据itemId寻找预售
     *
     * @param itemId 商品id
     * @return 对应的预售
     */
    @Override
    public Response<PreSale> findByItemId(Long itemId) {
        Response<PreSale> result = new Response<PreSale>();
        try {
            PreSale preSale = preSaleDao.findByItemId(itemId);
            if (preSale == null) {
                log.error("failed to find preSale by itemId={}", itemId);
                result.setError("presale.not.found");
                return result;
            }
            result.setResult(preSale);
            return result;
        } catch (Exception e) {
            log.error("failed to find preSale by itemId={}, cause:{}", itemId, Throwables.getStackTraceAsString(e));
            result.setError("presale.query.fail");
            return result;
        }
    }


    /**
     * 恢复预售分仓的库存
     *
     * @param orderId 订单id
     * @return 是否成功
     */
    @Override
    public Response<Boolean> recoverPreSaleStorageIfNecessary(Long orderId) {
        Response<Boolean> result = new Response<Boolean>();
        try {
            //如果是分仓预售订单, 还要恢复库存, 这个只需要判断订单对应的分仓信息在redis中的key是否存在即可
            String storageInfo = preSaleRedisDao.findStorageInfoByOrderId(orderId);

            if (!Strings.isNullOrEmpty(storageInfo)) { //表示确实是分仓的预售

                final List<String> storageList = Splitter.on('_').trimResults().splitToList(storageInfo);
                Long storageId = Long.parseLong(storageList.get(0));
                Long itemId = Long.parseLong(storageList.get(1));
                Integer quantity = Integer.parseInt(storageList.get(2));
                storageStockDao.changeSoldCount(-quantity, itemId, storageId);
                result.setResult(Boolean.TRUE);
            } else { //不是分仓的预售, 直接返回
                result.setResult(Boolean.TRUE);
            }
            return result;
        } catch (Exception e) {
            log.error("failed to recover storage for order(id={}), cause:{} ",orderId, Throwables.getStackTraceAsString(e));
            result.setError("storage.recover.fail");
            return result;
        }
    }

    /**
     * 判断对应的仓库的库存是否足够
     *
     * @param itemId   商品id
     * @param regionId 地区id
     * @param quantity 购买数量
     * @return 是否有足够的库存
     */
    @Override
    public boolean enoughStock(Long itemId, Integer regionId, Integer quantity) {
        //首先根据商品id和地区id查找仓库码
        AddressStorage addressStorage = addressStorageDao.findByItemIdAndAddressId(itemId, regionId);
        if (addressStorage == null) {
            log.warn("no addressStorage found by itemId={} and addressId={}", itemId, regionId);
            return false;
        }

        //查找对应仓库中的库存
        Long storageId = addressStorage.getStorageId();
        StorageStock storageStock = storageStockDao.findByItemIdAndStorageId(itemId, storageId);
        if (storageStock == null) {
            log.error("no storageStock found by itemId={} and storageId={}", itemId, storageId);
            return false;
        }

        //检查库存是不是足够
        Integer currentStock = storageStock.getInitStock() - storageStock.getSoldCount();
        if (currentStock < quantity) {
            log.error("not enough stock , requires {}, but storage is:{}", quantity, storageStock);
            return false;
        }
        return true;

    }

    /**
     * 回退预售购买限制
     * @param buyerId 买家id
     * @param preSaleId 预售id
     * @param buyChange 回退的数量，总是正数
     */
    private Response<Boolean> recoverPreSaleBuyLimitIfNecessaryInner(Long buyerId, Long preSaleId, Integer buyChange) {
        Response<Boolean> result = new Response<Boolean>();

        if(buyerId == null || preSaleId == null || buyChange == null || buyChange < 0) {
            log.error("params not right");
            result.setError("illegal.param");
            return result;
        }

        try {

            Integer buyCount = preSaleBuyLimitDao.getPreSaleBuyCount(buyerId, preSaleId);
            //如果购买数量查询为空，说明这个预售没有购买限制，不需要回退
            if (buyCount == null) {
                result.setResult(Boolean.TRUE);
                return result;
            }
            int nowCount = buyCount - buyChange;
            preSaleBuyLimitDao.setPreSaleBuyCountWithoutExpireTime(buyerId, preSaleId, nowCount);

            result.setResult(Boolean.TRUE);
            return result;

        }catch (Exception e) {
            log.error("fail to recover preSale buy limit with buyerId={}, preSaleId={}, buyChange={},cause:{}",
                    buyerId, preSaleId, buyChange, Throwables.getStackTraceAsString(e));
            result.setError("recover.presale.buy.limit.fail");
            return result;
        }

    }
}
