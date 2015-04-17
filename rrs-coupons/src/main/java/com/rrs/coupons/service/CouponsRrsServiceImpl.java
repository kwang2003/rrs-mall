package com.rrs.coupons.service;

import com.aixforce.annotations.ParamInfo;
import com.aixforce.category.model.BackCategory;
import com.aixforce.category.model.Spu;
import com.aixforce.category.service.BackCategoryHierarchy;
import com.aixforce.category.service.SpuService;
import com.aixforce.common.model.PageInfo;
import com.aixforce.common.model.Paging;
import com.aixforce.common.model.Response;
import com.aixforce.common.utils.JsonMapper;
import com.aixforce.item.model.Item;
import com.aixforce.item.model.Sku;
import com.aixforce.item.service.DefaultItemService;
import com.aixforce.item.service.ItemService;
import com.aixforce.search.Pair;
import com.aixforce.shop.model.Shop;
import com.aixforce.shop.service.ShopService;
import com.aixforce.trade.dto.PreOrder;
import com.aixforce.trade.dto.RichOrderItem;
import com.aixforce.user.base.BaseUser;
import com.aixforce.user.service.AccountService;
import com.alibaba.dubbo.common.utils.StringUtils;
import com.google.common.base.Objects;
import com.google.common.base.Strings;
import com.google.common.base.Throwables;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.rrs.coupons.dao.CouponsRrsDao;
import com.rrs.coupons.dao.RrsCouUserDao;
import com.rrs.coupons.dao.RrsCouponsDao;
import com.rrs.coupons.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Nonnull;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

//import com.aixforce.item.manager.ItemManager;

/**
 * Created by zhum01 on 2014/8/19.
 */

@Service
public class CouponsRrsServiceImpl implements CouponsRrsService{

    public static final JsonMapper JSON_MAPPER = JsonMapper.nonEmptyMapper();

    @Autowired
    private CouponsRrsDao couponsRrsDao;

    @Autowired
    private RrsCouponsDao rrsCouponsDao;

    private final static Logger log = LoggerFactory.getLogger(CouponsRrsServiceImpl.class);

//    @Autowired
//    private ItemManager itemManager;

    @Autowired
    private ItemService itemService;

    @Autowired
    private DefaultItemService defaultItemService;

    @Autowired
    private  ShopService shopService;

    @Autowired
    private  AccountService<? extends BaseUser> accountService;

    @Autowired
    private SpuService spuService;
    @Autowired
    private BackCategoryHierarchy bch;

    @Override
    public int adminCount() {
        return couponsRrsDao.adminCount();
    }

    @Override
    public Response<List<RrsCou>> queryCouponsBy(Long userId) {
        return null;
    }

    @Override
    public Response<List<RrsShowCouponView>> getCouponByUser(@ParamInfo("baseUser") BaseUser baseUser) {
        Long userId = baseUser.getId();
		//System.err.println("===getCouponByUser=====" + userId);

        Response<List<RrsShowCouponView>> result = new Response<List<RrsShowCouponView>>();
        //查询用户对应的有效优惠券信息
		List<RrsShowCouponView> userCouponList = couponsRrsDao
				.getCouByUserId(userId);

		RrsShowCouponView  rsc =null;
		if (null != userCouponList) {
			long endTime=0L;
			long nowTime=0L;
			for (int i=0;i<userCouponList.size();i++) {
				rsc=userCouponList.get(i);
				endTime =rsc.getEndTime().getTime();
				nowTime = System.currentTimeMillis();
				if((endTime-nowTime)/(3600000L * 24L) <=3 && endTime>nowTime){
					rsc.setIndate(1);//即将到期
				}
			}
		}
		result.setResult(userCouponList);
        result.setSuccess(true);
        return result;
    }

    //此处参考端点方法 为改动
    public Response<List<PreOrder>> preOrder(String skus) {
        Response<List<PreOrder>> result = new Response<List<PreOrder>>();
        if (Strings.isNullOrEmpty(skus)) {
            log.warn("skus can not be empty");
            result.setError("order.preOrder.fail");
            return result;
        }

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
            for (Long sellerId : grouped.keySet()) {
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
                preOrder.setRois(Lists.newArrayList(grouped.get(sellerId)));
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

    //此处参考端点方法 为改动
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
                //todo: should we throw exception here?
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
            grouped.put(sellerId, roi);
        }
        return grouped;
    }

    @Override
    public List<RrsCouOrder> findByOrderIds(List<Long> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            return Collections.emptyList();
        }

        return rrsCouponsDao.findByOrderIds(ids);
    }

    @Override
    public List<RrsCouOrderItem> findOrderItemsByOrderIds(List<Long> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            return Collections.emptyList();
        }

        return rrsCouponsDao.findOrderItemsByOrderIds(ids);
    }

    /**
     * 查看商品所属类目
     * **/
    @Override
    public Response<List<Pair>> queryPairByItemId(@ParamInfo("itemId") Long itemId) {
        Response<List<Pair>> result = new Response<List<Pair>>();
        if(itemId == null) {
            log.error("item id can not be null when find bread crumbs");
            result.setError("params.illegal");
            return result;
        }
        try {
            Response<Item> itemResponse = itemService.findById(itemId);//itemManager.findById(itemId);
            Item itemR = ((itemResponse!=null && itemResponse.isSuccess())?itemResponse.getResult():null);
            if (itemR == null) {
                log.error("item(id={}) mot found", itemId);
                result.setError("item.not.found");
                return result;
            }
            Long spuId = itemR.getSpuId();
            Response<Spu> spuR = spuService.findById(spuId);
            Long categoryId = spuR.getResult().getCategoryId();
            List<BackCategory> backCategories = bch.ancestorsOf(categoryId);
            List<Pair> pairs = Lists.newArrayListWithCapacity(backCategories.size());

            for (BackCategory bc : backCategories) {
                pairs.add(new Pair(bc.getName(), bc.getId()));
            }//当前商品所属类目信息
            result.setResult(pairs);
            return result;
        }catch (Exception e) {
            log.error("failed to find bread crumbs by itemId={}, cause:{}", itemId, Throwables.getStackTraceAsString(e));
            result.setError("breadCrumbs.query.fail");
            return result;
        }
    }

    @Override
    public Response<Long> checkJoin(@Nonnull Long itemId) {
        Response<Long> result = new Response<Long>();
        //查看商品所属类目
        Response<List<Pair>> resultPair = new Response<List<Pair>>();
        resultPair = queryPairByItemId(itemId);
        if(!resultPair.isSuccess()){
            result.setResult(0L);
        }
        try {
            //获取有效的优惠卷信息
            DateFormat format2 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String nowDate = format2.format(new Date());
            List<RrsCou> resultList = rrsCouponsDao.queryRrsCouponsBy(nowDate,2L);
            StringBuffer categoryIds = new StringBuffer();
            if(resultList!=null && resultList.size()>0){
                Iterator<RrsCou> its = resultList.iterator();
                while(its.hasNext()){
                    RrsCou rrsCou = its.next();
                    categoryIds.append(rrsCou.getCategoryId()).append(",");
                }
            }else{
                result.setResult(0L);
            }
            //比较当前优惠券类目和 当前商品所属类目是否一致  一致 则显示 优惠信息
            Map<Long,String> categoryIdList = new HashMap<Long,String>();
            if(categoryIds!=null && categoryIds.length()>0){
                String[] categoryDataIds = categoryIds.toString().split(",");
                List<String> list = Arrays.asList(categoryDataIds);
                categoryIdList = listToMap(list);
            }else{
                result.setResult(0L);
            }

            //查询最有一个栏目是否属于优惠券范围 属于则可以显示用优惠券
            List<Pair> pairList = resultPair.getResult();
            for(int i=0;i<pairList.size();i++){
                Pair pair = pairList.get(i);
                if(categoryIdList.containsKey(pair.getId())){
                    result.setResult(1L);
                    break;
                }
            }
            return result;
        }catch (Exception e) {
            log.error("failed to find bread crumbs by itemId={}, cause:{}", itemId, Throwables.getStackTraceAsString(e));
            result.setError("breadCrumbs.query.fail");
            return result;
        }
    }

    @Override
    public Response<Long> checkJoinAndUser(Long itemId, Long userId) {

        Response<Long> result = new Response<Long>();
        //查看商品所属类目
        Response<List<Pair>> resultPair = new Response<List<Pair>>();
        resultPair = queryPairByItemId(itemId);
        if(!resultPair.isSuccess()){
            result.setResult(0L);
        }
        try {
            //获取有效的优惠卷信息
            DateFormat format2 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String nowDate = format2.format(new Date());
            List<RrsCouUserView> resultList = rrsCouUserDao.queryCouponsAllByUser(userId,1L,nowDate);
            StringBuffer categoryIds = new StringBuffer();
            if(resultList!=null && resultList.size()>0){
                Iterator<RrsCouUserView> its = resultList.iterator();
                while(its.hasNext()){
                    RrsCouUserView rrsCou = its.next();
                    categoryIds.append(rrsCou.getCategoryId()).append(",");
                }
            }else{
                result.setResult(0L);
            }
            //比较当前优惠券类目和 当前商品所属类目是否一致  一致 则显示 优惠信息
            Map<Long,String> categoryIdList = new HashMap<Long,String>();
            if(categoryIds!=null && categoryIds.length()>0){
                String[] categoryDataIds = categoryIds.toString().split(",");
                List<String> list = Arrays.asList(categoryDataIds);
                categoryIdList = listToMap(list);
            }else{
                result.setResult(0L);
            }

            //查询最有一个栏目是否属于优惠券范围 属于则可以显示用优惠券
            List<Pair> pairList = resultPair.getResult();
            for(int i=0;i<pairList.size();i++){
                Pair pair = pairList.get(i);
                if(categoryIdList.containsKey(pair.getId())){
                    result.setResult(1L);
                    break;
                }
            }
            return result;
        }catch (Exception e) {
            log.error("failed to find bread crumbs by itemId={}, cause:{}", itemId, Throwables.getStackTraceAsString(e));
            result.setError("breadCrumbs.query.fail");
            return result;
        }
    }


    public static Map<Long,String> listObjToMap(List<Pair> listObj) {
        Map<Long,String> categoryIdList = new HashMap<Long,String>();
        if(listObj!=null){
            for(int m=0;m<listObj.size();m++) {
                Pair pairObj = listObj.get(m);
                categoryIdList.put(pairObj.getId(),String.valueOf(pairObj.getId()));
            }
        }
        return categoryIdList;
    }

    public static Map<Long,String> listToMap(List<String> list) {
        Map<Long,String> categoryIdList = new HashMap<Long,String>();
        if(list!=null){
            for(int m=0;m<list.size();m++) {
                String categoryId = list.get(m);
                if(StringUtils.isInteger(categoryId)){
                    if(!categoryIdList.containsKey(Long.valueOf(categoryId))){
                        categoryIdList.put(Long.valueOf(categoryId),categoryId);
                    }
                }
            }
        }
        return categoryIdList;
    }


    @Override
    public Response<RrsCou> queryCouponsById(@ParamInfo("couponsId") Long couponsId) {
        Response<RrsCou> result = new Response<RrsCou>();
        try{
            RrsCou rrsCou =  rrsCouponsDao.queryCouponsById(couponsId);
            result.setResult(rrsCou);
            return result;
        }catch(Exception e){
            log.error("failed to update brand, cause:", e);
            result.setError("brand.update.fail");
            return result;
        }
    }

    @Override
    public Response<Boolean> updateRrsCou(RrsCou rrsCou) {
        Response<Boolean> result = new Response<Boolean>();
        try{
            Boolean istrue =  rrsCouponsDao.updateRrsCou(rrsCou);
            result.setResult(istrue);
            return result;
        }catch(Exception e){
            //log.error("failed to update brand, cause:", e);
            result.setError("brand.update.fail");
            return result;
        }
    }

    @Autowired
    private RrsCouUserDao rrsCouUserDao;

    @Override
    public Response<List<RrsCouUserView>> preCouponsBySku(@ParamInfo("skus") String skus, @ParamInfo("baseUser") BaseUser baseUser) {
        //查看sku 对应的产品是否符合优惠信息
        Response<List<PreOrder>> preOrderRes = preOrder(skus);

        HashMap<Object, Object> pairOrderItem = new HashMap<Object, Object>();//得到该产品对应的类目信息
        HashMap<Long,Integer> categoryFreeMap = new HashMap<Long,Integer>();
        List<Pair> allJoinList = new ArrayList<Pair>(); //获取所有参加商品的栏目
        if(preOrderRes.isSuccess()) {
            List<PreOrder> orderList = preOrderRes.getResult();
            List<RichOrderItem> orderItemList = new ArrayList<RichOrderItem>();
            for (int i = 0; i < orderList.size(); i++) {//订单数量
                PreOrder preOrder = orderList.get(i);
                Iterator<RichOrderItem> its = preOrder.getRois().iterator();
                while (its.hasNext()) {
                    orderItemList.add(its.next());
                }
            }
            if (!orderItemList.isEmpty()) {
                Iterator<RichOrderItem> items = orderItemList.iterator();
                List<RichOrderItem> richOrderItems = new ArrayList<RichOrderItem>();

                while (items.hasNext()) {
                    RichOrderItem richOrderItem = items.next();
                    Response<List<Pair>> pairRes = queryPairByItemId(richOrderItem.getSku().getItemId());
                    List<Pair> pairList = pairRes.getResult();
                    Response<Long> isjoin = checkJoin(richOrderItem.getSku().getItemId());//判断该产品是否符合类目的优惠券
                    //1参加  2 不参加  产品所属分类信息
                    if (Objects.equal(isjoin.getResult(), 1L)) {
                        for(int pi=0;pi<pairList.size();pi++){
                            //计算栏目对应优惠的价格
                            Pair pair = pairList.get(pi);
                            log.info(" pair id"+pair.getId());
                            if (!Objects.equal(pair.getId(), 0L)) {//去除顶级目录
                                Long mapKey =pair.getId();
                                log.info(" mapKey id"+mapKey);
                                if(categoryFreeMap.containsKey(mapKey)){
                                    Integer freeA = categoryFreeMap.get(mapKey);
                                    categoryFreeMap.put(mapKey,freeA+richOrderItem.getFee());//栏目金额总和
                                }else{
                                    categoryFreeMap.put(mapKey,richOrderItem.getFee());//栏目金额总和
                                }
                            }
                        }
                        allJoinList.addAll(pairList); //获取所有参加商品的栏目
                        richOrderItems.add(richOrderItem);
                        pairOrderItem.put(richOrderItem, pairList);
                    }
                }
            }
        }

        //得到参加活动类目的 产品信息
        Long userId = baseUser.getId();

        Map<Long,String> joinCategoryIdList = listObjToMap(allJoinList);

        Response<List<RrsCouUserView>> result = new Response<List<RrsCouUserView>>();
        //查询用户对应的有效优惠券信息
        DateFormat format2 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String nowDate = format2.format(new Date());
        List<RrsCouUserView> showCoupons = rrsCouUserDao.queryCouponsAllByUser(baseUser.getId(),1L,nowDate);
        log.info("= showCoupons ="+showCoupons);
        log.info("= categoryFreeMap ="+categoryFreeMap);
        List<RrsCouUserView> ListCoupons = new ArrayList();
        //测试数据栏目对应商品信息
        for (int j = 0; j < showCoupons.size(); j++) {
            long feeCount=0;
            RrsCouUserView couponView = showCoupons.get(j);
            for(Long lanmuId : categoryFreeMap.keySet()){
                log.info(lanmuId+"= couponView ="+couponView);
                if(!StringUtils.isEmpty(couponView.getCategoryId())){
                    log.info(couponView.getCategoryId()+"= category ="+lanmuId);
                    if(couponView.getCategoryId().contains(String.valueOf(lanmuId))){//包含该栏目则显示优惠券
                        feeCount+=categoryFreeMap.get(lanmuId);
                        if(feeCount >= couponView.getTerm()){
                            ListCoupons.add(couponView);
                            break;
                        }
                    }
                }
            }
            log.info("====feeCount===="+feeCount+"====couponView===="+couponView.getTerm());
        }


//        List<RrsCouUserView> ListCoupons = new ArrayList();

        for (int j = 0; j < showCoupons.size(); j++) {
            RrsCouUserView couponView = showCoupons.get(j);
            //比较当前优惠券类目和 当前商品所属类目是否一致  一致 则显示 优惠信息
            Map<Long,String> categoryIdList = new HashMap<Long, String>();
            if(!StringUtils.isEmpty(couponView.getCategoryId())){
                String[] categoryDataIds = couponView.getCategoryId().split(",");
                List<String> list = Arrays.asList(categoryDataIds);
                categoryIdList = listToMap(list);
            }

//           for(Long categoryId : categoryIdList.keySet()) {
//               if(joinCategoryIdList.containsKey(categoryId)){//获取所有参加商品的栏目和优惠券category比较
//                   ListCoupons.add(couponView);
//                   joinCategoryIdList.remove(categoryId);
//               }
//            }

//            if(ListCoupons!=null && ListCoupons.size()>0){
//                for(int lc =0;lc<ListCoupons.size();lc++){
//                    RrsCouUserView couponObj = showCoupons.get(lc);
//                    String[] tempDateIds = couponObj.getCategoryId().split(",");
//                    List<String> list = Arrays.asList(tempDateIds);
//                    for(int x=0;x<list.size();x++){
//                        if(categoryFreeMap.containsKey(Long.valueOf(list.get(x)))){
//                            int shopAmount = categoryFreeMap.get(Long.valueOf(list.get(x)));
////                            if(couponObj){
////
////                            }
//                        }
//                    }
//                }
//            }

//            查询最有一个栏目是否属于优惠券范围 属于则可以显示用优惠券
//            List<Pair> pairList = resultPair.getResult();
//            for(int i=0;i<pairList.size();i++){
//                Pair pair = pairList.get(i);
//                if(categoryIdList.containsKey(pair.getId())){
//                    result.setResult(1L);
//                    break;
//                }
//            }
        }

        result.setResult(ListCoupons);
        result.setSuccess(true);

        return result;
    }

    @Override
    public Response<List<RrsCou>> findCouponsByPaging(int businessId,String status) {
        Response<List<RrsCou>> pubDtosResp = new Response<List<RrsCou>>();
        Map<String, Object> qParams = Maps.newHashMap();
        qParams.put("businessId", businessId==0?"":businessId);
        qParams.put("status", status==null?"":status);
        List<RrsCou> result=couponsRrsDao.findCouponsAll(qParams);
        pubDtosResp.setResult(result);
        pubDtosResp.setSuccess(true);
        return pubDtosResp;
    }

    @Override
    public Integer addCoupon(Map<String, Object> paramMap) {
        //Response<Boolean> result = new Response<Boolean>();
        couponsRrsDao.addCoupon(paramMap);
        //result.setSuccess(Boolean.TRUE);
        return Integer.parseInt(paramMap.get("id").toString());
    }

    @Override
    public List<Map<String, Object>> findCategory(Integer categoryId) {
        List<Map<String, Object>> result=couponsRrsDao.findCategory(categoryId);
        return result;
    }

    @Override
    public Response<Boolean> updateCoupon(Map<String, Object> paramMap) {
        Response<Boolean> result = new Response<Boolean>();
        couponsRrsDao.updateCoupon(paramMap);
        result.setSuccess(Boolean.TRUE);
        return result;
    }

    @Override
    public Response<Boolean> updateCouponStatus(Map<String, Object> paramMap) {
        Response<Boolean> result = new Response<Boolean>();
        couponsRrsDao.updateCouponStatus(paramMap);
        result.setSuccess(Boolean.TRUE);
        return result;
    }


    @Override
    public Response<RrsCouUser> queryCouponsUserBy(Long userId,Long couponsId) {
        Response<RrsCouUser> result = new Response<RrsCouUser>();
        //查询用户对应的有效优惠券信息
        List<RrsCouUser> resultList = rrsCouUserDao.queryCouponsUserBy(userId,couponsId);
        if(resultList!=null && resultList.size()>0){
            result.setResult(resultList.get(0));
            result.setSuccess(true);
        }
        return result;
    }

    @Override
    public void updateCouponUser(Long id) {
        rrsCouUserDao.updateCouponUser(id);
    }

    @Override
    public Response<Paging<RrsCou>> queryCouponsByPage(@ParamInfo("businessId") Long businessId, @ParamInfo("beginCreatedAt") String beginCreatedAt,
                                                       @ParamInfo("endCreatedAt") String endCreatedAt, @ParamInfo("status") Long status,@ParamInfo("couponsType") Long couponsType,
                                                       @ParamInfo("pageNo") Integer pageNo, @ParamInfo("size") Integer size) {
        PageInfo pageInfo = new PageInfo(pageNo, size);
        Response<Paging<RrsCou>> result = new Response<Paging<RrsCou>>();
        try {
            Map<String, Object> params = new HashMap<String, Object>();
            params.put("businessId",businessId);
            params.put("beginCreatedAt",beginCreatedAt);
            params.put("endCreatedAt",endCreatedAt);
            params.put("status",status);
            params.put("couponsType",couponsType);

            Paging<RrsCou> coupons = rrsCouponsDao.queryCouponsByPage(pageInfo.getOffset(), pageInfo.getLimit(),params);
            List<RrsCou> couponsList = coupons.getData();
            result.setResult(new Paging<RrsCou>(coupons.getTotal(), couponsList));
            return result;
        } catch (NullPointerException e) {
            log.error("address.query.fail", e);
            result.setError("address.query.fail");
            return result;
        } catch (Exception e) {
            log.error("find shop fail", e);
            result.setError("shop.query.fail");
            return result;
        }
    }

    @Override
    public Response<List<RrsCou>> querySellerCouponsByParam(@ParamInfo("baseUser") BaseUser baseUser,
                                                            @ParamInfo("userStatus") Long userStatus,
                                                            @ParamInfo("couponStatus") String couponStatus,
                                                            @ParamInfo("itemIds") String itemIds) {
        Response<List<RrsCou>> result = new Response<List<RrsCou>>();
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("userId",baseUser.getId());
        params.put("userStatus",userStatus);
        params.put("couponStatus",couponStatus);
        params.put("itemIds",itemIds);
        DateFormat format2 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String nowDate = format2.format(new Date());
        params.put("startTime",nowDate);
        params.put("endTime",nowDate);
        List<RrsCou> rrsCouList = couponsRrsDao.querySellerCouponsByParam(params);
        try{
            result.setResult(rrsCouList);
            return result;
        }catch(Exception e){
            log.error("CouponsRrsServiceImpl querySellerCouponsByParam fail", e);
            return result;
        }
    }

	@Override
	public Response<Paging<RrsCou>> queryCouponsByShopId(Long shopId,
			Long pageIndex, Long pageSize) {
		Response<Paging<RrsCou>> result = new Response<Paging<RrsCou>>();
		Map<String, Object> params = new HashMap<String, Object>();

		if (null == pageSize || pageSize <= 0 || pageSize > 100) {
			pageSize = 4L;
		}

		if (null == pageIndex || pageIndex <= 1) {
			pageIndex = 0L;
		} else {
			pageIndex = (pageIndex-1) * pageSize;
		}

		params.put("shopId", shopId);
		params.put("offset", pageIndex);
		params.put("limit", pageSize);

		try {
			Paging<RrsCou> coupons = couponsRrsDao.queryCouponsByShopId(params);
			SimpleDateFormat formater = new SimpleDateFormat("yyyy-MM-dd");
			for(RrsCou cou : coupons.getData()){
				cou.setStartTimeStr(formater.format(cou.getStartTime()));
				cou.setEndTimeStr(formater.format(cou.getEndTime()));
				cou.setAmountStr(cou.getAmount() / 100);
			}
			result.setResult(coupons);
			return result;
		} catch (Exception e) {
			log.error("queryCouponsByShopId.fail", e);
			result.setError("queryCouponsByShopId.fail");
		}

		return result;
	}

	@Override
	public Response<RrsCou> queryShopCouponsById(Long couponsId) {
		Response<RrsCou> result = new Response<RrsCou>();

		try {
			RrsCou coupons = rrsCouponsDao.queryShopCouponsById(couponsId);
			result.setResult(coupons);
			return result;
		} catch (Exception e) {
			log.error("queryShopCouponsById.fail", e);
			result.setError("queryShopCouponsById.fail");
		}

		return result;
	}

	@Override
    public Response<Paging<ShopCoupons>> queryShopCouponsByPage(@ParamInfo("channel") String channel,
    		@ParamInfo("name") String name,
    		@ParamInfo("shopName") String shopName,
    		@ParamInfo("status") Long status,
    		@ParamInfo("pageNo") Integer pageNo,
    		@ParamInfo("size") Integer size) {
        PageInfo pageInfo = new PageInfo(pageNo, size);
        Response<Paging<ShopCoupons>> result = new Response<Paging<ShopCoupons>>();
        System.err.println("=======queryShopCouponsByPage=======");
        try {
            Map<String, Object> params = new HashMap<String, Object>();
            params.put("channel",channel);
            params.put("name",name);
            params.put("shopName",shopName);
            params.put("status",status);

            Paging<ShopCoupons> coupons = rrsCouponsDao.queryShopCouponsByPage(pageInfo.getOffset(), pageInfo.getLimit(),params);
            List<ShopCoupons> couponsList = coupons.getData();
            result.setResult(new Paging<ShopCoupons>(coupons.getTotal(), couponsList));
//            Paging<Shop> shops = shopDao.shops(pageInfo.getOffset(), pageInfo.getLimit(), shopName, businessId, userName);
//            List<ShopDto> shopDtos = transToShopDto(shops.getData());
//            result.setResult(new Paging<ShopDto>(shops.getTotal(), shopDtos));
            return result;
        } catch (NullPointerException e) {
            log.error("address.query.fail", e);
            result.setError("address.query.fail");
            return result;
        } catch (Exception e) {
            log.error("find shop fail", e);
            result.setError("shop.query.fail");
            return result;
        }
    }

    @Override
    public Response<Boolean> insertItemIds(List<Map<String, Object>> paramMap) {
        Response<Boolean> result = new Response<Boolean>();
        couponsRrsDao.insertItemIds(paramMap);
        result.setSuccess(Boolean.TRUE);
        return result;
    }

    @Override
    public List<Map<String, Object>> findEditItems(String couponsId) {
        return  couponsRrsDao.findEditItems(couponsId);
    }

    @Override
    public Response<Boolean> deleteCouponsId(String couponsId) {
        Response<Boolean> result = new Response<Boolean>();
        couponsRrsDao.deleteCouponsId(couponsId);
        result.setResult(Boolean.TRUE);
        return result;
    }

    @Override
    public Response<Paging<RrsCou>> queryRrsCouponsByPage(@ParamInfo("baseUser") BaseUser baseUser,
                                                          @ParamInfo("cpName") String cpName,
                                                          @ParamInfo("businessId") Long businessId,
                                                          @ParamInfo("beginCreatedAt") String beginCreatedAt,
                                                       @ParamInfo("endCreatedAt") String endCreatedAt,
                                                       @ParamInfo("status") String status,
                                                       @ParamInfo("couponsType") Long couponsType,
                                                       @ParamInfo("sellerName") String sellerName,
                                                       @ParamInfo("shopName") String shopName,
                                                       @ParamInfo("pageNo") Integer pageNo,
                                                       @ParamInfo("size") Integer size) {
        PageInfo pageInfo = new PageInfo(pageNo, size);
        Response<Paging<RrsCou>> result = new Response<Paging<RrsCou>>();
        try {
            Map<String, Object> params = new HashMap<String, Object>();
            params.put("businessId",businessId);
            params.put("cpName",cpName);
            params.put("beginCreatedAt",beginCreatedAt);
            params.put("endCreatedAt",endCreatedAt);
            params.put("status",status);
            params.put("couponsType",couponsType);
            params.put("sellerName",sellerName);
            params.put("shopName",shopName);
            if(Objects.equal(baseUser.getType(),0)){//管理员不需要判断创建者
                params.put("sellerId","");
            }else{
                params.put("sellerId",baseUser.getId());
            }

            Paging<RrsCou> coupons = rrsCouponsDao.queryCouponsByPage(pageInfo.getOffset(), pageInfo.getLimit(),params);
            List<RrsCou> couponsList = coupons.getData();
            result.setResult(new Paging<RrsCou>(coupons.getTotal(), couponsList));
            return result;
        } catch (NullPointerException e) {
            log.error("address.query.fail", e);
            result.setError("address.query.fail");
            return result;
        } catch (Exception e) {
            log.error("find shop fail", e);
            result.setError("shop.query.fail");
            return result;
        }
    }


}
