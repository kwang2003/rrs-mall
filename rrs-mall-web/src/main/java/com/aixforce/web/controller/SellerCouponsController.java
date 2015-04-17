package com.aixforce.web.controller;

import com.aixforce.common.model.Paging;
import com.aixforce.common.model.Response;
import com.aixforce.item.model.Item;
import com.aixforce.item.model.ItemWithTags;
import com.aixforce.item.service.ItemService;
import com.aixforce.shop.model.Shop;
import com.aixforce.shop.service.ShopService;
import com.aixforce.user.base.BaseUser;
import com.aixforce.user.base.UserUtil;
import com.rrs.coupons.model.RrsCou;
import com.rrs.coupons.service.CouponsManageService;
import com.rrs.coupons.service.CouponsRrsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by alfred on 2014/8/19.
 */

@Controller
@RequestMapping("/api/SCoupons")
public class SellerCouponsController {
    private final static Logger log = LoggerFactory.getLogger(SellerCouponsController.class);
    @Autowired
    private CouponsRrsService couponsRrsService;

    @Autowired
    private ShopService shopService;

    @Autowired
    private CouponsManageService couponsManageService;

    @Autowired
    private ItemService itemService;

    @Value("#{app.mainSite}")
    private String domainUrl;


    @RequestMapping(value = "/findAllCoupons", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Response<String> queryCouponsBy(@RequestParam("pageCount") int pageCount){
     Response<String> result = new Response<String>();
      BaseUser baseUser = UserUtil.getCurrentUser();
      if(baseUser==null){
        result.setResult("404");
      }else{
          long userId = baseUser.getId();
          int count = couponsManageService.countAllCou(userId);
          if(pageCount*25>=count){
              pageCount = pageCount -1;
          }
          List<RrsCou> list  = couponsManageService.findAllRrsCou(userId,pageCount);
          StringBuilder stb = new StringBuilder();
          if(list!=null&&list.size()!=0){
              for(RrsCou rrsCou:list){
                    int tempTerm = rrsCou.getTerm()/100;
                    int tempAmount = rrsCou.getAmount()/100;
                    SimpleDateFormat df=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    String startDate = df.format(rrsCou.getStartTime());
                    String endDate = df.format(rrsCou.getEndTime());
                    String createAt = df.format(rrsCou.getCreated_at());
                    String sen= rrsCou.getSendNum()==0?"不限":String.valueOf(rrsCou.getSendNum());
                    String status = rrsCou.getStatus()==0?"未生效":rrsCou.getStatus()==1?"暂停":rrsCou.getStatus()==2?"生效":rrsCou.getStatus()==3?"失效":rrsCou.getStatus()==4?"撤销":"获取状态失败";
                    stb.append("<tr><td><p><a href=\"javascript:void(0)\" class=\"couponname\">"+rrsCou.getCpName()+"</a></p>");
                    stb.append("<div class=\"coupon_popup\"><h3>优惠券设置信息<a href=\"javascript:void(0)\" class=\"close_popup\">X</a></h3>");
                    stb.append("<div class=\"coupon_popup_content\"><dl><dt>券名称</dt><dd>"+rrsCou.getCpName()+"</dd></dl><dl><dt>有效期</dt><dd>"+startDate+"至"+endDate+"</dd></dl><dl><dt>领券URL</dt><dd>http://"+domainUrl+"/seller/seller-coupon-detail?couponsId="+rrsCou.getId()+"</dd></dl>");
                    stb.append("<dl><dt>优惠条件[满]</dt><dd>"+tempTerm+"元</dd></dl><dl><dt>优惠条件[减]</dt><dd>"+tempAmount+"元</dd></dl>");
                    stb.append("<dl><dt>发券数量</dt><dd>"+sen+"</dd></dl><dl><dt>ID限领数量</dt><dd>"+rrsCou.getUseLimit()+"</dd></dl><dl><dt>商品范围</dt><dd>限以下分类商品：<p>"+rrsCou.getCategoryName()+"</p>部分或全部商品使用</dd></dl>");
                    stb.append("<dl><dt>备注</dt><dd>"+rrsCou.getMemo()+"</dd></dl></div></div></td>");
                    stb.append("<td>满"+tempTerm+"元减"+tempAmount+"元</td><td class=\"tl\">"+startDate+"至"+endDate+"</td>");
                    stb.append("<td>"+createAt+"</td>");
                    stb.append("<td>"+rrsCou.getSendNum()+"/"+rrsCou.getCouponReceive()+"/<a href=\"/seller/coupons_item_list?couponsId="+rrsCou.getId()+"\">"+rrsCou.getCouponUse()+"</a></td>");
                    stb.append("<td>"+status+"</td>");
                    if(rrsCou.getStatus()==0) {
                        stb.append("<td><a href=\"/seller/sellerCouponEdit?couponId="+rrsCou.getId()+"\" class=\"operate\">编辑</a></td></tr>");
                    }else if(rrsCou.getStatus()==2){
                        stb.append("<td><a href=\"/api/SCoupons/stopCoupons?couponId="+rrsCou.getId()+"\" class=\"operate\">中止</a></td></tr>");
                    }else{
                        stb.append("<td></td></tr>");
                    }
              }

          }
          stb.append("<input type=\"hidden\" value=\""+pageCount+"\" id=\"pageCount\" />");
          result.setResult(stb.toString());
      }
      return result;
    }
    @RequestMapping(value = "/stopCoupons", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public String stopCoupons(@RequestParam("couponId") long couponId,@RequestParam("status") int status){
        Map<String, Object> map=new HashMap<String, Object>();
        map.put("couponsId",couponId);
        map.put("status",status);
        couponsManageService.stopCoupons(map);
        return "redirect:/seller/sellCouponsMan";

    }

    @RequestMapping(value = "/countCoupons", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Response<String> countCoupons(){
        BaseUser baseUser = UserUtil.getCurrentUser();
        int count = couponsManageService.countAllCou(baseUser.getId());
        Response<String> result = new Response<String>();
        result.setResult("共（"+count+"）类优惠券");
        return result;
    }
    @RequestMapping(value = "/countCouponsSearch", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Response<String> countCouponsSearch(@RequestParam("endAt") String endAt,@RequestParam("startAt") String startAt,@RequestParam("couponName") String couponName,@RequestParam("selector") String selector){           Response<String> result = new Response<String>();
        RrsCou rrsCou2 = new RrsCou();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        if(!"nulls".equals(endAt)){
            try {
                rrsCou2.setEndTime(sdf.parse(endAt));
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        if(!"nulls".equals(startAt)){
            try {
                rrsCou2.setStartTime(sdf.parse(startAt));
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        if(!"nulls".equals(couponName)){
            rrsCou2.setCpName(couponName);

        }
        rrsCou2.setStatus(Integer.parseInt(selector));
        BaseUser baseUser = UserUtil.getCurrentUser();
        rrsCou2.setSellerId(baseUser.getId());
        Integer count = couponsManageService.countCouBySearch(rrsCou2);
        result.setResult("共（"+count+"）类优惠券");
        return result;
    }


        @RequestMapping(value = "/findAllCouponsSearch", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
        @ResponseBody
        public Response<String> queryCouponsBySearrch(@RequestParam("endAt") String endAt,@RequestParam("startAt") String startAt,@RequestParam("couponName") String couponName,@RequestParam("selector") String selector,@RequestParam("pageCount") int pageCount) {
            Response<String> result = new Response<String>();
            Map<Object,Object> map = new HashMap<Object,Object>();

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            if(!"nulls".equals(endAt)){
                   map.put("endTime",endAt);
            }
            if(!"nulls".equals(startAt)){
                map.put("startTime",startAt);
            }
            if(!"nulls".equals(couponName)){
                map.put("cpName",couponName);
            }
            map.put("status",Integer.parseInt(selector));
            BaseUser baseUser = UserUtil.getCurrentUser();
            map.put("sellerId",baseUser.getId());
            int count = couponsManageService.countAllCou(baseUser.getId());
            if(pageCount*25>=count){
                pageCount = pageCount -1;
            }
            int tempCount = 0;
            if(pageCount!=0){
                tempCount = pageCount*25;
            }
            map.put("page",tempCount);
            List<RrsCou> list  = couponsManageService.findAllBySearch(map);
            StringBuilder stb = new StringBuilder();
            if(list!=null&&list.size()!=0) {
                for (RrsCou rrsCou : list) {
                    int tempTerm = rrsCou.getTerm() / 100;
                    int tempAmount = rrsCou.getAmount()/100;
                    SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    String startDate = df.format(rrsCou.getStartTime());
                    String endDate = df.format(rrsCou.getEndTime());
                    String createAt = df.format(rrsCou.getCreated_at());
                    String status = rrsCou.getStatus() == 0 ? "未生效" : rrsCou.getStatus() == 1 ? "暂停" : rrsCou.getStatus() == 2 ? "生效" : rrsCou.getStatus() == 3 ? "失效" :rrsCou.getStatus()==4?"撤销": "获取状态失败";
                    stb.append("<tr><td><p><a href=\"javascript:void(0)\" class=\"couponname\">" + rrsCou.getCpName() + "</a></p>");
                    stb.append("<div class=\"coupon_popup\"><h3>优惠券设置信息<a href=\"javascript:void(0)\" class=\"close_popup\">X</a></h3>");
                    stb.append("<div class=\"coupon_popup_content\"><dl><dt>券名称</dt><dd>" + rrsCou.getCpName() + "</dd></dl><dl><dt>有效期</dt><dd>" + startDate + "至" + endDate + "</dd></dl><dl><dt>领券URL</dt><dd>http://"+domainUrl+"/seller/seller-coupon-detail?couponsId="+rrsCou.getId()+"</dd></dl>");
                    stb.append("<dl><dt>优惠条件[满]</dt><dd>" + tempTerm + "元</dd></dl><dl><dt>优惠条件[减]</dt><dd>" + tempAmount + "元</dd></dl>");
                    stb.append("<dl><dt>发券数量</dt><dd>" + rrsCou.getSendNum() + "</dd></dl><dl><dt>ID限领数量</dt><dd>" + rrsCou.getUseLimit() + "</dd></dl><dl><dt>商品范围</dt><dd>限以下分类商品：<p>" + rrsCou.getCategoryName() + "</p>部分或全部商品使用</dd></dl>");
                    stb.append("<dl><dt>备注</dt><dd>" + rrsCou.getMemo() + "</dd></dl></div></div></td>");
                    stb.append("<td>满" + tempTerm + "元减" + tempAmount + "元</td><td class=\"tl\">" + startDate + "至" + endDate + "</td>");
                    stb.append("<td>" + createAt + "</td>");
                    stb.append("<td>" + rrsCou.getSendNum() + "/" + rrsCou.getCouponReceive() + "/<a href=\"/seller/coupons_item_list?couponsId="+rrsCou.getId()+"\">" + rrsCou.getCouponUse() + "</a></td>");
                    stb.append("<td>"+status+"</td>");
                    if(rrsCou.getStatus()==0) {
                        stb.append("<td><a href=\"/seller/sellerCouponEdit?couponId="+rrsCou.getId()+"\" class=\"operate\">编辑</a></td></tr>");
                    }else if(rrsCou.getStatus()==2){
                        stb.append("<td><a href=\"/api/SCoupons/stopCoupons?couponId="+rrsCou.getId()+"\" class=\"operate\">中止</a></td></tr>");
                    }else{
                        stb.append("<td></td></tr>");
                    }
                }
            }
            stb.append("<input type=\"hidden\" value=\""+pageCount+"\" id=\"pageCount\" />");
            result.setResult(stb.toString());

                return result;
        }

    @RequestMapping(value = "/addCoupon", method = RequestMethod.POST)
    @ResponseBody
    public void addCoupon(HttpServletRequest request) {
        log.info("tian jia addCoupon");
        BaseUser baseUser=UserUtil.getCurrentUser();
        Shop shop=shopService.findByUserId(baseUser.getId()).getResult();
        Map<String, Object> paramMap = new HashMap<String, Object>();
        /*平台优惠券需要的参数，不加会报错，是因为用的平台优惠券的添加方法*/
        paramMap.put("channelId", request.getParameter("channelId") == null ? "0" : request.getParameter("channelId"));
        paramMap.put("area", request.getParameter("area") == null ? "1": request.getParameter("area"));
        paramMap.put("sendType", request.getParameter("sendType") == null ? "2" : request.getParameter("sendType"));
        paramMap.put( "sendStartTime", request.getParameter("sendStartTime") == null ? request.getParameter("startTime") + ":00:00" : request.getParameter("sendStartTime"));
        paramMap.put("sendEndTime",request.getParameter("sendEndTime") == null ? request.getParameter("endTime") + ":59:59" : request .getParameter("sendEndTime"));
        paramMap.put("sendOrigin",request.getParameter("sendOrigin") == null ? "" : request.getParameter("sendOrigin"));
        /*平台优惠券需要的参数，不加会报错，是因为用的平台优惠券的添加方法*/
        String cpName=request.getParameter("cpName") == null ? "" : request.getParameter("cpName");
        paramMap.put("cpName", cpName);
        paramMap.put("startTime",request.getParameter("startTime") == null ? "" : request.getParameter("startTime") + ":00:00");
        paramMap.put("endTime", request.getParameter("endTime") == null ? "" : request.getParameter("endTime") + ":59:59");
        if (checkDate(request.getParameter("startTime") == null ? "" : request.getParameter("startTime") + ":00:00",
                request.getParameter("endTime") == null ? "" : request.getParameter("endTime") + ":59:59").equals("0")) {
            // 优惠券生效
            paramMap.put("status", "2");
        } else if(checkDate(request.getParameter("startTime") == null ? "" : request.getParameter("startTime") + ":00:00",
                request.getParameter("endTime") == null ? "" : request.getParameter("endTime") + ":59:59").equals("1")){
            // 优惠券已失效
            paramMap.put("status", "3");
        }else{
            // 优惠券未生效
            paramMap.put("status", "0");
        }
        paramMap.put("nameStr", request.getParameter("nameStr") == null ? "": request.getParameter("nameStr"));
        paramMap.put("term", request.getParameter("term") == null ? "0": new BigDecimal( request.getParameter("term")).multiply(new BigDecimal(100)).toString());
        paramMap.put( "amount",request.getParameter("amount") == null ? "0" : new BigDecimal( request.getParameter("amount")).multiply(new BigDecimal(100)).toString());
        paramMap.put("useLimit", request.getParameter("useLimit") == null ? "0" : request.getParameter("useLimit"));
        paramMap.put("sendNum", request.getParameter("sendNum") == null ? "0": request.getParameter("sendNum"));
        paramMap.put("memo", request.getParameter("memo") == null ? "" : request.getParameter("memo"));
        paramMap.put("couponsType","2");
        paramMap.put("shopid",shop.getId());
        paramMap.put("shopName",shop.getName());
        paramMap.put("sellerId",baseUser.getId());
        paramMap.put("sellerName",baseUser.getName());
        String couponsCode="SP"+getDate();
        paramMap.put("couponsCode",couponsCode);
        int couponsId=couponsRrsService.addCoupon(paramMap);
        List<Map<String,Object>> listMap=new ArrayList<Map<String, Object>>();
        String itemIdStr= request.getParameter("itemIdStr");
        String itemTagStr= request.getParameter("itemTagStr");
        String [] itemTags=itemTagStr.split(",");
        String [] ids=itemIdStr.split(",");
        for(int i=0;i<ids.length;i++){
            Map<String,Object> map=new HashMap<String,Object>();
            map.put("couponsId",couponsId);
            map.put("itemId",ids[i]);
            map.put("shopId",shop.getId());
            map.put("sellerId",baseUser.getId());
            map.put("couponsCode",couponsCode);
            map.put("couponsName",cpName);
            map.put("shopname",shop.getName());
            map.put("itemtag",itemTags[i]);
            listMap.add(map);
        }
        couponsRrsService.insertItemIds(listMap);
    }

    @RequestMapping(value = "/updateCoupon", method = RequestMethod.POST)
    @ResponseBody
    public void updateCoupon(HttpServletRequest request) {
        log.info("updateCoupon start");
        String id=request.getParameter("id");
        Response<Boolean> bool= couponsRrsService.deleteCouponsId(id);
        if(bool.getResult()){
            BaseUser baseUser=UserUtil.getCurrentUser();
            Shop shop=shopService.findByUserId(baseUser.getId()).getResult();
            Map<String, Object> paramMap = new HashMap<String, Object>();
        /*平台优惠券需要的参数，不加会报错，是因为用的平台优惠券的添加方法*/
            paramMap.put("channelId", request.getParameter("channelId") == null ? "0" : request.getParameter("channelId"));
            paramMap.put("area", request.getParameter("area") == null ? "1": request.getParameter("area"));
            paramMap.put("sendType", request.getParameter("sendType") == null ? "2" : request.getParameter("sendType"));
            paramMap.put( "sendStartTime", request.getParameter("sendStartTime") == null ? request.getParameter("startTime") + ":00:00" : request.getParameter("sendStartTime"));
            paramMap.put("sendEndTime",request.getParameter("sendEndTime") == null ? request.getParameter("endTime") + ":59:59": request .getParameter("sendEndTime"));
            paramMap.put("sendOrigin",request.getParameter("sendOrigin") == null ? "" : request.getParameter("sendOrigin"));
        /*平台优惠券需要的参数，不加会报错，是因为用的平台优惠券的添加方法*/
            paramMap.put("id", id);
            String cpName=request.getParameter("cpName") == null ? "" : request.getParameter("cpName");
            paramMap.put("cpName", cpName);
            paramMap.put("startTime",request.getParameter("startTime") == null ? "" : request.getParameter("startTime") + ":00:00");
            paramMap.put("endTime", request.getParameter("endTime") == null ? "" : request.getParameter("endTime") + ":59:59");
            if (checkDate(request.getParameter("startTime") == null ? "" : request.getParameter("startTime") + ":00:00",
                    request.getParameter("endTime") == null ? "" : request.getParameter("endTime") + ":59:59").equals("0")) {
                // 优惠券生效
                paramMap.put("status", "2");
            } else if(checkDate(request.getParameter("startTime") == null ? "" : request.getParameter("startTime") + ":00:00",
                    request.getParameter("endTime") == null ? "" : request.getParameter("endTime") + ":59:59").equals("1")){
                // 优惠券已失效
                paramMap.put("status", "3");
            }else{
                // 优惠券未生效
                paramMap.put("status", "0");
            }
            paramMap.put("nameStr", request.getParameter("nameStr") == null ? "": request.getParameter("nameStr"));
            paramMap.put("term", request.getParameter("term") == null ? "0": new BigDecimal( request.getParameter("term")).multiply(new BigDecimal(100)).toString());
            paramMap.put( "amount",request.getParameter("amount") == null ? "0" : new BigDecimal( request.getParameter("amount")).multiply(new BigDecimal(100)).toString());
            paramMap.put("useLimit", request.getParameter("useLimit") == null ? "0" : request.getParameter("useLimit"));
            paramMap.put("sendNum", request.getParameter("sendNum") == null ? "0": request.getParameter("sendNum"));
            paramMap.put("memo", request.getParameter("memo") == null ? "" : request.getParameter("memo"));
            paramMap.put("couponsType","2");
            paramMap.put("shopid",shop.getId());
            paramMap.put("sellerId",baseUser.getId());
            String couponsCode="SP"+getDate();
            paramMap.put("couponsCode",couponsCode);
            couponsRrsService.updateCoupon(paramMap);
            List<Map<String,Object>> listMap=new ArrayList<Map<String, Object>>();
            String itemIdStr= request.getParameter("itemIdStr");
            String itemTagStr= request.getParameter("itemTagStr");
            String [] itemTags=itemTagStr.split(",");
            String [] ids=itemIdStr.split(",");
            for(int i=0;i<ids.length;i++){
                Map<String,Object> map=new HashMap<String,Object>();
                map.put("couponsId",id);
                map.put("itemId",ids[i]);
                map.put("shopId",shop.getId());
                map.put("sellerId",baseUser.getId());
                map.put("couponsCode",couponsCode);
                map.put("couponsName",cpName);
                map.put("shopname",shop.getName());
                map.put("itemtag",itemTags[i]);
                listMap.add(map);
            }
            couponsRrsService.insertItemIds(listMap);
        }


    }
    @RequestMapping(value = "/findEditCouponsById", method = RequestMethod.POST)
    @ResponseBody
    public Response<Map<String,Object>> findEditCouponsById(HttpServletRequest request) {
       Response<Map<String,Object>> result = new Response<Map<String,Object>>();
       Map<String,Object> map=new HashMap<String, Object>();
       RrsCou rrsCou= couponsManageService.findEditById(Long.parseLong(request.getParameter("id")));
       DateFormat dateFormat=new SimpleDateFormat("yyyy-MM-dd hh");
       map.put("id",rrsCou.getId());
       map.put("cpName",rrsCou.getCpName());
       map.put("startTime",dateFormat.format(rrsCou.getStartTime()));
       map.put("endTime",dateFormat.format(rrsCou.getEndTime()));
       map.put("term",rrsCou.getTerm()/100);
       map.put("amount",rrsCou.getAmount()/100);
       map.put("sendNum",rrsCou.getSendNum());
       map.put("useLimit",rrsCou.getUseLimit());
       map.put("memo",rrsCou.getMemo());
       map.put("nameStr",rrsCou.getCategoryName());
       result.setResult(map);
       return result;
    }
    @RequestMapping(value = "/findEditItems", method = RequestMethod.POST)
    @ResponseBody
    public Response<List<ItemWithTags>> findEditItems(HttpServletRequest request) {
        Response<List<ItemWithTags>> result=new Response<List<ItemWithTags>>();
        List<Map<String,Object>> listMap=couponsRrsService.findEditItems(request.getParameter("id"));
        List<Long> ids=new ArrayList<Long>();
        for (Map map:listMap){
            ids.add(Long.parseLong(map.get("itemId").toString()));
        }
        List<ItemWithTags> list=new ArrayList<ItemWithTags>();
        Response<List<Item>> items=itemService.findByIds(ids);
        for (int i=0;i<items.getResult().size();i++){
            Item item=items.getResult().get(i);
            ItemWithTags iwt=new ItemWithTags();
            iwt.setItemId(item.getId());
            iwt.setImageUrl(item.getMainImage());
            iwt.setItemName(item.getName());
            iwt.setPrice(item.getPrice());
            iwt.setQuantity(item.getQuantity());
            iwt.setSoldQuantity(item.getSoldQuantity());
            List<String> tags=new ArrayList<String>();
            tags.add(listMap.get(i).get("item_tag").toString());
            iwt.setTags(tags);
            list.add(iwt);
        }
        result.setResult(list);
        return result;
    }
    @RequestMapping(value = "/findUnclassifiedItems", method = RequestMethod.POST)
    @ResponseBody
    public Response<List<Item>> findUnclassifiedItems(HttpServletRequest request) {
        BaseUser baseUser=UserUtil.getCurrentUser();
        Response<List<Item>> result=new Response<List<Item>>();
        List<Item> listItems=new ArrayList<Item>();
        Response<Paging<Item>> unclassItems=itemService.findUnclassifiedItems(baseUser,1,1000);
        for (Item item:unclassItems.getResult().getData()){
            if(item.getStatus()==1){
                listItems.add(item);
            }
        }
        result.setResult(listItems);
        return result;
    }

    /**
     * 判断当前日期是否在有效期内
     *
     * @param startTime
     * @param endTime
     * @return
     */
    private static String checkDate(String startTime, String endTime) {
        log.info("CouponsController checkDate format datime start");
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            Date start = sdf.parse(startTime);
            Date end = sdf.parse(endTime);
            Date nowDate = new Date();
            if (nowDate.after(start) && nowDate.before(end)) {
                return "0";
            }else if(nowDate.after(end)){
                return "1";
            }else {
                return "-1";
            }
        } catch (ParseException e) {
            log.error("CouponsController checkDate format datime error");
            e.printStackTrace();
            return "";
        }

    }

    private synchronized String getDate() {
       SimpleDateFormat df = new SimpleDateFormat("yyyyMMddHHmmss");//设置日期格式
       return df.format(new Date());
    }




}
