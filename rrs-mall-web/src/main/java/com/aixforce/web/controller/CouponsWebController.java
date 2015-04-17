package com.aixforce.web.controller;

import com.aixforce.common.model.Paging;
import com.aixforce.common.model.Response;
import com.aixforce.exception.JsonResponseException;
import com.aixforce.item.service.ItemService;
import com.aixforce.user.base.BaseUser;
import com.aixforce.user.base.UserUtil;
import com.aixforce.web.dto.JsonValue;
import com.aixforce.web.misc.MessageSources;
import com.alibaba.dubbo.common.utils.StringUtils;
import com.rrs.coupons.model.LqMessage;
import com.rrs.coupons.model.RrsCou;
import com.rrs.coupons.model.RrsCouponsItemList;
import com.rrs.coupons.service.CouponsItemListService;
import com.rrs.coupons.service.CouponsRrsService;
import com.rrs.coupons.service.LqCouponService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

/**
 * Created by zhum01 on 2014/8/19.
 */

@Controller
@RequestMapping("/api/coupons")
public class CouponsWebController {
    private final static Logger log = LoggerFactory.getLogger(CouponsWebController.class);
    @Autowired
    private MessageSources messageSources;

    @Autowired
    private LqCouponService lqCouponService;

    @Autowired
    private CouponsRrsService couponsRrsService;

    @Autowired
    private ItemService itemService;

    @Autowired
    private CouponsItemListService couponsItemListService;


    /**
     * 查询该产品是否和优惠券存在关联 存在 则 显示 优惠信息 没关联则不显示
     * **/
    @RequestMapping(value = "/queryItemCouBy", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public JsonValue queryItemCouponBy(@RequestParam("itemId") Long itemId){
        JsonValue jsonValue = new JsonValue();
        //0 不显示  1 显示
        Response<List<RrsCouponsItemList>> listResponse =   couponsItemListService.queryCouponsItemListBy(null,itemId,null,"");
        if(!listResponse.isSuccess()){
            jsonValue.setStatus(0L);
            return jsonValue;
        }
        if(listResponse.getResult().size()>0){
            jsonValue.setStatus(1L);
            return jsonValue;
        }
        return jsonValue;
    }

    /**
     * 点击优惠券的时候判断优惠券和产品是否符合可使用的范围之内
     * **/
    @RequestMapping(value = "/queryItemCouponBy", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public JsonValue queryItemCouponBy(@RequestParam("couponsId") Long couponsId,
                                       @RequestParam("itemIds") String itemIds,
                                       @RequestParam("itemPrices") String itemPrices){

        log.info("couponsId=  "+couponsId+"   itemIds=  "+itemIds+"   itemPrices=  "+itemPrices);

        JsonValue jsonValue = new JsonValue();
        if (StringUtils.isEmpty(itemIds) || StringUtils.isEmpty(itemPrices)){
            jsonValue.setStatus(0L);
            jsonValue.setMessage("产品不存在,请重新确认");
            return jsonValue;
        }

        Response<List<RrsCouponsItemList>> listResponse = couponsItemListService.queryCouponsItemListBy(couponsId, null, null, "");
        if(!listResponse.isSuccess()){
            jsonValue.setStatus(0L);
            jsonValue.setMessage("产品不存在,请重新确认");
            return jsonValue;
        }
        Response<RrsCou> result = new Response<RrsCou>();
        result = couponsRrsService.queryCouponsById(couponsId);
        if(!result.isSuccess() || result.getResult()==null) {
            jsonValue.setStatus(0L);
            jsonValue.setMessage("优惠券不存在，请重新确认");
            return jsonValue;
        }
        int couponsTerm = result.getResult().getTerm();

        String[] itemIdIndexs = itemIds.split(",");
        String[] itemPriceIndexs = itemPrices.split(",");
        Iterator<RrsCouponsItemList> its =  listResponse.getResult().iterator();
        HashMap<String,String> itemIdHashMap = new HashMap<String,String>();
        while(its.hasNext()){
            RrsCouponsItemList rrsCouponsItemList = its.next();
            String keyCode = String.valueOf(rrsCouponsItemList.getItemId());
            itemIdHashMap.put(keyCode,keyCode);
        }
        Integer totalPrice = 0;
        for(int i=0;i<itemIdIndexs.length;i++){//产品和价格的对应关系
            String itemId = itemIdIndexs[i];
            log.info(i+" itemId = "+itemId);
            if(itemIdHashMap.containsKey(itemId)){
                String itemPrice  =itemPriceIndexs[i];
                totalPrice += Double.valueOf(itemPrice).intValue();
            }
//            String keyCode = String.valueOf(skuIndexs[i])+"-"+String.valueOf(itemIdIndexs[i]);
//            log.info("itemId"+itemIdIndexs[i]+"totalPrice"+itemPriceIndexs[i]);
//            itemPriceMap.put(keyCode, itemPriceIndexs[i]);
        }
        log.info(" totalPrice == = "+(totalPrice));
//        log.info("itemPriceMap size"+itemPriceMap.size());
//        Set set = itemPriceMap.entrySet() ;
//        java.util.Iterator it = itemPriceMap.entrySet().iterator();
//        while(it.hasNext()){
//            java.util.Map.Entry entry = (java.util.Map.Entry)it.next();
//            // entry.getKey() 返回与此项对应的键
//            // entry.getValue() 返回与此项对应的值
//            // System.out.println(entry.getValue());
//        }


        log.info("item Total Price size"+totalPrice+"coupons term price"+result.getResult().getTerm()+" result :"+(couponsTerm > (totalPrice.intValue() * 100)));
        if(couponsTerm > (totalPrice.intValue() * 100) ){//产品价格大于优惠券的最低消费价格 则可以使用 否则不能使用
            jsonValue.setStatus(0L);
            jsonValue.setMessage("使用该优惠券需消费满"+couponsTerm/100+"元");
            return jsonValue;
        }else{
            jsonValue.setStatus(1L);
            return jsonValue;
        }
    }

    @RequestMapping(value = "/queryCouponsBy", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public RrsCou queryCouponsBy(@RequestParam("couponsId") Long couponsId){
        Response<RrsCou> result = new Response<RrsCou>();
        result = couponsRrsService.queryCouponsById(couponsId);
        if(!result.isSuccess()) {
            log.error("find rrsCoupons failed, cause:{}", result.getError());
            throw new JsonResponseException(500, messageSources.get(result.getError()));
        }
        return result.getResult();
    }


    @RequestMapping(value = "/queryCouponsByOrder", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Long queryCouponsByOrder(@RequestParam("imageType") Long imageType){
        Response<Long> result = null;
        if(!result.isSuccess()) {
            log.error("find rrsCoupons failed, cause:{}", result.getError());
            throw new JsonResponseException(500, messageSources.get(result.getError()));
        }
        return result.getResult();
    }

    @RequestMapping(value = "/lQCoupon", method = RequestMethod.GET,produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Response<LqMessage> LqCoupon(@RequestParam("couponId") String couponId) {
        Response<LqMessage> result= new  Response<LqMessage>();
        BaseUser baseUser= UserUtil.getCurrentUser();
        LqMessage lqm=new LqMessage();
        if(baseUser==null){
            lqm.setStatus("1");
            lqm.setMessage("请先登录");
            result.setResult(lqm);
            return result;
        }else{
            result.setResult(lqCouponService.LqCoupon(baseUser,Integer.parseInt(couponId)));
            return result;
        }
    }

    @RequestMapping(value = "/getCouponsBySellerId", method = RequestMethod.GET,produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Response<Paging<RrsCou>> getCouponsBySellerId(@RequestParam("sellerId") Long sellerId, Long pageIndex, Long pageSize) {
    	return couponsRrsService.queryCouponsByShopId(sellerId, pageIndex, pageSize);
    }


    /**
     * 品台券使用的时候 判断 校验
     * **/
//    @RequestMapping(value = "/queryPlatCouponBy", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
//    @ResponseBody
//    public JsonValue queryPlatCouponBy(@RequestParam("couponsId") Long couponsId,
//                                       @RequestParam("itemIds") String itemIds,
//                                       @RequestParam("itemPrices") String itemPrices,
//                                       @RequestParam("provinceCode") String provinceCode
//                                       ){
//        JsonValue jsonValue = new JsonValue();
//        if (StringUtils.isEmpty(itemIds) || StringUtils.isEmpty(itemPrices)){
//            jsonValue.setStatus(0L);
//            jsonValue.setMessage("产品不存在,请重新确认");
//            return jsonValue;
//        }
//
//        Response<RrsCou> result = new Response<RrsCou>();
//        result = couponsRrsService.queryCouponsById(couponsId);
//        if(!result.isSuccess() || result.getResult()==null) {
//            jsonValue.setStatus(0L);
//            jsonValue.setMessage("优惠券不存在，请重新确认");
//            return jsonValue;
//        }
//
//        RrsCou rrsCouObj =  result.getResult();
//        //判断 区域是否在该优惠券 使用范围 内
//        String area = rrsCouObj.getArea();
//        if(!area.contains(provinceCode)){//存在则表示可以使用 不存在 表示不能使用
//            jsonValue.setStatus(0L);
//            jsonValue.setMessage("该优惠券不在您配送的范围之内,不能使用,请重新确认");
//            return jsonValue;
//        }
//
//        //根据优惠券Id 查询 该优惠券的spu黑名单 列表
//        Map<String,Object> exceptCouponsMap = new HashMap<String,Object>();
//        Integer totalPrice = 0;
//        int couponsTerm = rrsCouObj.getTerm();
//        Response<List<RrsCouponsExceptSpus>> couponsExceptSpusList =  couponsRrsService.querySpuListByCouponsId(couponsId);
//        if(couponsExceptSpusList.isSuccess()){
//            List<RrsCouponsExceptSpus> couponsExceptList = couponsExceptSpusList.getResult();
//            for(int ec=0;ec<couponsExceptList.size();ec++){
//                RrsCouponsExceptSpus couponsExceptSpus = couponsExceptList.get(ec);
//                exceptCouponsMap.put(String.valueOf(couponsExceptSpus.getSpuId()),couponsExceptSpus);
//            }
//        }
//
//        String queryItemIds = itemIds.substring(0,itemIds.length()-1);
//        List itemIdList = Arrays.asList(queryItemIds.split(","));
//        Response<List<Item>> itemListResponse = itemService.findByIds(itemIdList);
//        StringBuffer spuIds = new StringBuffer();
//        if(itemListResponse.isSuccess()){
//            List<Item> itemList = itemListResponse.getResult();
//            for(int il=0;il<itemList.size();il++){
//                Item itemObj = itemList.get(il);
//                if(!exceptCouponsMap.containsKey(String.valueOf(itemObj.getSpuId()))){//true 说明 该成品不符合 false 说明该产品可以使用优惠券
//                    totalPrice += Double.valueOf(itemObj.getPrice()).intValue();
//                }
//            }
//        }
//
//        log.info("item Total Price size"+totalPrice+"coupons term price"+result.getResult().getTerm()+" result :"+(totalPrice.intValue() > couponsTerm));
//        if(totalPrice.intValue() < couponsTerm){//产品价格大于优惠券的最低消费价格 则可以使用 否则不能使用
//            jsonValue.setStatus(0L);
//            jsonValue.setMessage("使用该优惠券需消费满"+couponsTerm/100+"元");
//            return jsonValue;
//        }else{
//            jsonValue.setStatus(1L);
//            return jsonValue;
//        }

//        Response<List<RrsCouponsItemList>> listResponse = couponsItemListService.queryCouponsItemListBy(couponsId, null, null, "");
//        if(!listResponse.isSuccess()){
//            jsonValue.setStatus(0L);
//            jsonValue.setMessage("产品不存在,请重新确认");
//            return jsonValue;
//        }

//        int couponsTerm = rrsCouObj.getTerm();
//
//        String[] itemIdIndexs = itemIds.split(",");
//        String[] itemPriceIndexs = itemPrices.split(",");
//        HashMap<String,String> itemPriceMap = new HashMap<String,String>();
//        for(int i=0;i<itemIdIndexs.length;i++){//产品和价格的对应关系
//            log.info("itemId"+itemIdIndexs[i]+"totalPrice"+itemPriceIndexs[i]);
//            itemPriceMap.put(itemIdIndexs[i], itemPriceIndexs[i]);
//        }
//        log.info("itemPriceMap size"+itemPriceMap.size());
//        Iterator<RrsCouponsItemList> its =  listResponse.getResult().iterator();
//        Integer totalPrice = 0;
//        while(its.hasNext()){
//            RrsCouponsItemList rrsCouponsItemList = its.next();
//            String keyCode = String.valueOf(rrsCouponsItemList.getItemId());
//            if(itemPriceMap.containsKey(keyCode)){
//                String itemPrice  = itemPriceMap.get(keyCode);
//                totalPrice += Double.valueOf(itemPrice).intValue();
//            }
//        }

//    }

}
