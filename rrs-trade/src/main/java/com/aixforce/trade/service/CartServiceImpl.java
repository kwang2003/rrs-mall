package com.aixforce.trade.service;

import com.aixforce.annotations.ParamInfo;
import com.aixforce.common.model.Response;
import com.aixforce.common.utils.JsonMapper;
import com.aixforce.item.model.Item;
import com.aixforce.item.model.Sku;
import com.aixforce.item.service.ItemService;
import com.aixforce.shop.model.Shop;
import com.aixforce.shop.model.ShopExtra;
import com.aixforce.shop.service.ShopExtraService;
import com.aixforce.shop.service.ShopService;
import com.aixforce.trade.dao.CartDao;
import com.aixforce.trade.dto.PreOrder;
import com.aixforce.trade.dto.RichOrderItem;
import com.aixforce.trade.model.CartItem;
import com.aixforce.trade.model.DeliveryMethod;
import com.aixforce.trade.model.UserCart;
import com.aixforce.user.base.BaseUser;
import com.aixforce.user.service.AccountService;
import com.google.common.base.Function;
import com.google.common.base.Objects;
import com.google.common.base.Strings;
import com.google.common.base.Throwables;
import com.google.common.collect.*;
import com.rrs.coupons.model.RrsCou;
import com.rrs.coupons.model.RrsCouponsItemList;
import com.rrs.coupons.service.CouponsItemListService;
import com.rrs.coupons.service.CouponsRrsService;
import com.rrs.coupons.service.RrsCouUserService;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;

import static com.aixforce.common.utils.Arguments.isNull;

/**
 * Author:  <a href="mailto:jlchen.cn@gmail.com">jlchen</a>
 * Date: 2013-03-08
 */
@Service
public class CartServiceImpl implements CartService {

    private static final Logger log = LoggerFactory.getLogger(CartServiceImpl.class);

    public static final JsonMapper JSON_MAPPER = JsonMapper.nonEmptyMapper();

    private final ItemService itemService;

    private final CartDao cartDao;

    private final ShopService shopService;

    private final AccountService<? extends BaseUser> accountService;

    private final DeliveryMethodService deliveryMethodService;

    private final static DateTimeFormatter DFT = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");

    @Value("#{app.eHaierSellerId}")
    private String eHaierSellerId;

    private final ShopExtraService shopExtraService;

    private final CouponsRrsService couponsRrsService;

    private final RrsCouUserService rrsCouUserService;

    private final CouponsItemListService couponsItemListService;

    @Autowired
    public CartServiceImpl(ItemService itemService, ShopService shopService, CartDao cartDao,
                           AccountService<? extends BaseUser> accountService, DeliveryMethodService deliveryMethodService,
                           ShopExtraService shopExtraService,CouponsRrsService couponsRrsService,RrsCouUserService rrsCouUserService,CouponsItemListService couponsItemListService) {
        this.itemService = itemService;
        this.shopService = shopService;
        this.cartDao = cartDao;
        this.accountService = accountService;
        this.deliveryMethodService = deliveryMethodService;
        this.shopExtraService = shopExtraService;
        this.couponsRrsService = couponsRrsService;
        this.rrsCouUserService = rrsCouUserService;
        this.couponsItemListService =couponsItemListService;
    }

    /**
     * 获取永久购物车中的物品
     *
     * @param baseUser 系统自动注入的用户
     * @return 永久购物车中的物品
     */
    @Override
    public Response<List<UserCart>> getPermanent(BaseUser baseUser) {
        Response<List<UserCart>> result = new Response<List<UserCart>>();
        Long userId = baseUser.getId();
        try {
            Multiset<Long> skuIds = cartDao.getPermanent(userId);
            List<UserCart> userCarts = buildUserCart(skuIds);
            //查询购物车内商品对应的店铺是否有店铺优惠券
            if(null !=userCarts){
	            Long shopId=0l;
	            for(int i=0;i<userCarts.size();i++){
	            	shopId=userCarts.get(i).getShopId();
	            	System.err.println("========findCouponsbyShopId========:"+shopId);
	            	//计算用户已领取店铺优惠券数量
	            	Map<String, Object> params = Maps.newHashMap();
	            	params.put("userId", userId);
	            	params.put("shopId", shopId);
	            	int countCou = couponsItemListService.queryUserShopCou(params);
	            	userCarts.get(i).setCountCou(countCou);

					List<RrsCouponsItemList> couponsList = couponsItemListService.findCouponsbyShopId(shopId);
//					for(RrsCouponsItemList cous:couponsList){
//						Map<String, Object> cou_params = Maps.newHashMap();
//						cou_params.put("userId", userId);
//						cou_params.put("couponId", cous.getCouponsId());
//						int userSum=couponsItemListService.sumUserCou(cou_params);
//						cous.setUserSum(userSum);
//
//					}
					userCarts.get(i).setShopCoupons(couponsList);
	            }
            }
            result.setResult(userCarts);
            return result;
        } catch (Exception e) {
            log.error("failed to get permanent shop carts for user(id={}),cause:{}",
                    userId, Throwables.getStackTraceAsString(e));
            result.setError("cart.find.fail");
            return result;
        }
    }

    /**
     * 获取永久购物车中的sku的种类个数
     *
     * @param baseUser 系统自动注入的用户
     * @return sku的种类个数
     */
    @Override
    public Response<Integer> getPermanentCount(BaseUser baseUser) {
        Response<Integer> result = new Response<Integer>();
        try {
            Multiset<Long> skuIds = cartDao.getPermanent(baseUser.getId());
            int count = skuIds.elementSet().size();
            result.setResult(count);
            return result;
        } catch (Exception e) {
            log.error("failed to get count of sku in permanent cart by user{},cause:{}",
                    baseUser, Throwables.getStackTraceAsString(e));
            result.setError("cart.count.fail");
            return result;
        }
    }

    /**
     * 获取临时购物车中的sku的种类个数
     *
     * @param cookie cartCookie
     * @return sku的种类个数
     */
    @Override
    public Response<Integer> getTemporaryCount(String cookie) {
        Response<Integer> result = new Response<Integer>();
        if (Strings.isNullOrEmpty(cookie)) {
            log.error("cookie value of cart can not be empty");
            result.setError("cookie.find.fail");
            return result;
        }
        try {
            Multiset<Long> skuIds = cartDao.getTemporary(cookie);
            int count = skuIds.elementSet().size();
            result.setResult(count);
            return result;
        } catch (Exception e) {
            log.error("failed to get count of sku in temporary cart by coolie{},cause:{}",
                    cookie, Throwables.getStackTraceAsString(e));
            result.setError("cart.count.fail");
            return result;
        }
    }

    /**
     * 为skuId进行归组分类
     *
     * @param skuIds skuIds
     * @return 购物车
     */
    private List<UserCart> buildUserCart(Multiset<Long> skuIds) {
        List<CartItem> cartItems = buildCartItems(skuIds);
        return group(cartItems);
    }

    /**
     * 增减临时购物车中的物品
     *
     * @param key      cart cookie key
     * @param skuId    sku id
     * @param quantity 变化数量
     */
    @Override
    public Response<Integer> changeTemporaryCart(String key, Long skuId, Integer quantity) {
        Response<Integer> result = new Response<Integer>();
        if (Strings.isNullOrEmpty(key) || skuId == null || quantity == null) {
            log.error("temporary shop cart args can not be null");
            result.setError("cart.find.fail");
            return result;
        }
        if (quantity == 0) {
            log.error("quantity can not be zero");
            result.setError("quantity.equal.zero");
            return result;
        }
        try {
            cartDao.changeTemporaryCart(key, skuId, quantity);
            Multiset<Long> skuIds = cartDao.getTemporary(key);
            int count = skuIds.elementSet().size();
            result.setResult(count);
            return result;
        } catch (Exception e) {
            log.error("failed to add skuId(id={}) to temporary shop cart(key={}),cause:{}",
                    key, skuId, Throwables.getStackTraceAsString(e)
            );
            result.setError("cart.add.fail");
            return result;
        }
    }

    /**
     * 增减永久购物车中的物品
     *
     * @param userId   userId
     * @param skuId    sku id
     * @param quantity 变化数量
     */
    @Override
    public Response<Integer> changePermanentCart(Long userId, Long skuId, Integer quantity) {
        Response<Integer> result = new Response<Integer>();
        if (userId == null || skuId == null || quantity == null) {
            log.error("temporary shop cart args can not be null");
            result.setError("cart.find.fail");
            return result;
        }
        if (quantity == 0) {
            log.error("quantity can not be zero");
            result.setError("quantity.equal.zero");
            return result;
        }
        try {
            cartDao.changePermanentCart(userId, skuId, quantity);
            Multiset<Long> skuIds = cartDao.getPermanent(userId);
            int count = skuIds.elementSet().size();
            result.setResult(count);
            return result;
        } catch (Exception e) {
            log.error("failed to add skuId(id={}) to permanent shop cart(id={}),cause:{}",
                    userId, skuId, Throwables.getStackTraceAsString(e)
            );
            result.setError("cart.change.fail");
            return result;
        }
    }


    /**
     * 将临时购物车的物品合并到永久购物车中,并删除临时购物车
     *
     * @param key    cookie中带过来了的key
     * @param userId 用户id
     */
    @Override
    public Response<Boolean> merge(String key, Long userId) {
        Response<Boolean> result = new Response<Boolean>();
        try {
            Multiset<Long> temporaryIds = cartDao.getTemporary(key);
            Multiset<Long> permanentIds = cartDao.getPermanent(userId);
            permanentIds.addAll(temporaryIds);
            cartDao.setPermanent(userId, permanentIds);
            cartDao.delete(key);
            result.setResult(Boolean.TRUE);
            return result;
        } catch (Exception e) {
            log.error("failed to merge shop carts for user(id={}) with his temporary shop cart(key={}),cause:{}",
                    userId, key, Throwables.getStackTraceAsString(e));
            result.setError("cart.merge.fail");
            return result;
        }
    }

    @Override
    public Response<List<PreOrder>> preOrder(@ParamInfo("baseUser") BaseUser baseUser,String skus) {
        Response<List<PreOrder>> result = new Response<List<PreOrder>>();
        if (Strings.isNullOrEmpty(skus)) {
            log.warn("skus can not be empty");
            result.setError("order.preOrder.fail");
            return result;
        }

        //System.out.println("当前用户ID"+baseUser.getId());
        //获取当前用户未使用的商家优惠券信息
//        Response<List<RrsCouUserView>> listResponse = rrsCouUserService.queryCouponsAllByUser(baseUser, skus, 1L);


        try {
            Map<Long, Integer> skuIdAndQuantity = JSON_MAPPER.fromJson(skus, JSON_MAPPER.createCollectionType(HashMap.class, Long.class, Integer.class));
            if(skuIdAndQuantity == null){
                log.error("failed to parse skuIdAndQuantity:{}", skus);
                result.setError("order.preOrder.fail");
                return result;
            }
            Multimap<Long, RichOrderItem> grouped = groupBySellerId(skuIdAndQuantity);
            List<PreOrder> preOrders = Lists.newArrayListWithCapacity(grouped.keySet().size());
            //get user name and shop name
            System.out.println("---订单数量--"+grouped.size());
            for (Long sellerId : grouped.keySet()) {
                System.out.println("--每个订单对应产品数量---"+grouped.get(sellerId).size());
                Response<? extends BaseUser> ur = accountService.findUserById(sellerId);
                if (!ur.isSuccess()) {
                    log.error("failed to find seller(id={}),error code:{}", sellerId, ur.getError());
                    continue;
                }
                String sellerName = ur.getResult().getName();
                Response<Shop> sr = shopService.findByUserId(sellerId);
                if (!sr.isSuccess()) {
                    log.error("failed to find shop for seller(id={}),error code:{}", sellerId, sr.getError());
                    continue;
                }
                Shop shop = sr.getResult();
                PreOrder preOrder = new PreOrder();
                preOrder.setShopName(shop.getName());
                preOrder.setSellerName(sellerName);
                preOrder.setSellerId(sellerId);
                preOrder.setIsCod(shop.getIsCod());
                preOrder.setEInvoice(shop.getEInvoice());
                preOrder.setVatInvoice(shop.getVatInvoice());
                preOrder.setRois(Lists.newArrayList(grouped.get(sellerId)));

                //是否是ehaier商家
                preOrder.setIsEhaier(Objects.equal(eHaierSellerId,shop.getUserId().toString()));
                DateTime sysDate = new DateTime(new Date());
                preOrder.setSystemDate(DFT.print(sysDate));//系统当前时间

                //是否支持到店支付
                Response<ShopExtra> shopExtraRes = shopExtraService.findByShopId(shop.getId());
                if (shopExtraRes.isSuccess()) {
                    ShopExtra extra = shopExtraRes.getResult();
                    //为null时不支持到店支付
                    if(isNull(extra.getIsStorePay())){
                        preOrder.setIsStorePay(Boolean.FALSE);
                    }else{
                        preOrder.setIsStorePay(extra.getIsStorePay());
                    }
                }else{
                    log.error("failed to find shopExtra for shopid(id={}),error code:{}", shop.getId(), shopExtraRes.getError());
                    preOrder.setIsStorePay(Boolean.FALSE);
                }

                //获取当前店铺是否有可使用的优惠券 有 true  无 false modify by cwf
                Iterable<RichOrderItem> rois = Lists.newArrayList(grouped.get(sellerId));
                Iterator<RichOrderItem> its = rois.iterator();
                StringBuffer itemIds = new StringBuffer();
                while(its.hasNext()){
                    RichOrderItem richOrderItem =  its.next();
                    itemIds.append(richOrderItem.getSku().getItemId()).append(",");
                }
                String queryItemIds = "";
                if(itemIds!=null && !itemIds.equals("")){
                    queryItemIds = itemIds.substring(0,itemIds.length()-1);
                }
//              * @param baseUser  当前登陆用户
//              * @param userStatus 用户领取的优惠券状态 是否使用优惠券状态 1未使用 2使用 3过期
//              * @param couponStatus 优惠券的状态 优惠券状态：未生效（0）暂停（1）生效（2）失效(3) 1 和2  用户都可以使用优惠券
//              * @param itemIds 传入多个ItemId (1,2) 逗号隔开
                String couponStatus = "1,2";
                Response<List<RrsCou>> listResponse1 =  couponsRrsService.querySellerCouponsByParam(baseUser,1L,couponStatus,queryItemIds);
                if(listResponse1.isSuccess()){  //查询结果正确 且存在返回值
                    if(listResponse1.getResult().size()>0){

                        preOrder.setIsUserCoupons(true);
                        preOrder.setUserCouponsList(listResponse1.getResult());
                    }
                }
                preOrders.add(preOrder);
            }
            result.setResult(preOrders);
            return result;
        } catch (Exception e) {
            log.error("failed to create order for skus {},cause:{}", skus, Throwables.getStackTraceAsString(e));
            result.setError("order.preOrder.fail");
            return result;
        }
    }

    //添加了一个新的关于运费计算的逻辑By MichaelZhao
    //对sku按照seller id进行归组
    private Multimap<Long, RichOrderItem> groupBySellerId(Map<Long, Integer> skuIdsAndQuantity) {

        Multimap<Long, RichOrderItem> grouped = HashMultimap.create();
        for (Long skuId : skuIdsAndQuantity.keySet()) {
            Integer quantity = skuIdsAndQuantity.get(skuId);
            if (quantity <= 0) {
                log.error("sku quantity can not litter than 1");
                continue;
            }
            Response<Sku> sr = itemService.findSkuById(skuId);
            if (!sr.isSuccess()) {
                log.error("failed to find sku where id = {},error code:{}", skuId, sr.getError());
                continue;
            }
            Sku sku = sr.getResult();
            if (sku.getStock() < quantity) {
                log.warn("no enough stock for sku where id={} (required:{},stock:{})", skuId, quantity, sku.getStock());
                continue;
            }
            Response<Item> ir = itemService.findById(sku.getItemId());
            if (!ir.isSuccess()) {
                log.error("failed to find item(id={}),error code:{}", sku.getItemId(), ir.getError());
                continue;
            }
            Item item = ir.getResult();
            if (!Objects.equal(item.getStatus(), Item.Status.ON_SHELF.toNumber())) {
                log.warn("item(id={}) is not onShelf,so skip this {}", item.getId(), sku);
                continue;
            }

            Long sellerId = item.getUserId();
            RichOrderItem roi = new RichOrderItem();
            roi.setSku(sku);
            roi.setItemName(item.getName());
            roi.setItemImage(item.getMainImage());
            roi.setFee(sku.getPrice() * quantity);
            roi.setCount(quantity);

            Response<DeliveryMethod> deliveryMethodR = deliveryMethodService.findById(item.getDeliveryMethodId());
            if(!deliveryMethodR.isSuccess() || deliveryMethodR.getResult() == null) {
                log.error("fail to find delivery method by id={},error code:{}",
                        item.getDeliveryMethodId(), deliveryMethodR.getError());
            }else {
                roi.setDeliveryPromise(deliveryMethodR.getResult().getName());
            }
            grouped.put(sellerId, roi);
        }
        return grouped;
    }

    /**
     * 批量删除用户购物车中的skuIds
     *
     * @param userId 用户id
     * @param skuIds 待删除的skuId列表
     */
    @Override
    public Response<Boolean> batchDeletePermanent(Long userId, Iterable<Long> skuIds) {
        Response<Boolean> result = new Response<Boolean>();
        if (userId == null || skuIds == null) {
            log.error("userId and skuIds both can not be null");
            result.setError("cart.batch.delete.fail");
            return result;
        }
        try {
            Multiset<Long> carts = cartDao.getPermanent(userId);
            for (Long skuId : skuIds) {
                carts.setCount(skuId, 0);
            }
            cartDao.setPermanent(userId, carts);
            result.setResult(Boolean.TRUE);
            return result;
        } catch (Exception e) {
            log.error("failed to batch delete skuIds{} of user(id={}),cause:{}",
                    skuIds, userId, Throwables.getStackTraceAsString(e));
            result.setError("cart.batch.delete.fail");
            return result;
        }
    }

    /**
     * 清空用户的购物车
     *
     * @param key   cookie中的key，或者用户id
     */
    @Override
    public Response<Boolean> empty(String key) {
        Response<Boolean> result = new Response<Boolean>();
        try {
            cartDao.delete(key);
            result.setResult(Boolean.TRUE);
            return result;
        } catch (Exception e) {
            log.error("`empty` invoke fail. can't empty cart by key or uid: {}, e:{}", key, e);
            result.setError("cart.empty.fail");
            return result;
        }
    }

//    @Override
//    public Response<List<PreOrder>> preCouponsOrder(@ParamInfo("baseUser") BaseUser baseUser, @ParamInfo("skus") String skus) {
//        System.out.println("当前用户ID"+baseUser.getId());
//        return new  Response<List<PreOrder>>();
//    }


    private List<CartItem> buildCartItems(Multiset<Long> skuIds) {
        List<CartItem> result = Lists.newArrayListWithCapacity(skuIds.elementSet().size());
        for (Long skuId : skuIds.elementSet()) {
            Response<Sku> sr = itemService.findSkuById(skuId);
            if (!sr.isSuccess()) {
                log.error("sku(id={}) is not found,skip", skuId);
                continue;
            }
            Sku sku = sr.getResult();
            Response<Item> ir = itemService.findById(sku.getItemId());
            if (!ir.isSuccess()) {
                log.error("item(id={}) is not found,skip", sku.getItemId());
                continue;
            }
            Item item = ir.getResult();
            CartItem cartItem = new CartItem();
            cartItem.setSku(sku);
            cartItem.setStatus(item.getStatus());
            cartItem.setItemImage(item.getMainImage());
            cartItem.setItemName(item.getName());
            cartItem.setShopId(item.getShopId());
            cartItem.setRegion(item.getRegion());
            cartItem.setCount(skuIds.count(skuId));
            result.add(cartItem);
        }
        return result;
    }

    /**
     * 根据shopId 来group cartItems
     *
     * @param cartItems 购物车中的sku列表
     * @return 分组后的sku
     */
    private List<UserCart> group(List<CartItem> cartItems) {
        ListMultimap<Long, CartItem> groupByShopId = Multimaps.index(cartItems, new Function<CartItem, Long>() {
            @Override
            public Long apply(CartItem cartItem) {
                return cartItem.getShopId();
            }
        });
        List<UserCart> cart = Lists.newArrayListWithCapacity(groupByShopId.keySet().size());
        for (Long shopId : groupByShopId.keySet()) {
            UserCart userCart = new UserCart();
            userCart.setShopId(shopId);
            Shop shop = shopService.findById(shopId).getResult();
            userCart.setSellerId(shop.getUserId());
            userCart.setShopName(shop.getName());
            userCart.setShopImage(shop.getImageUrl());
            userCart.setCartItems(Lists.newArrayList(groupByShopId.get(shopId)));   //f**k dubbo serialization
            cart.add(userCart);
        }
        return cart;
    }
}
