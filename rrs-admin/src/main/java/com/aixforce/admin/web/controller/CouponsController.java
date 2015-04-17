package com.aixforce.admin.web.controller;

import com.aixforce.category.service.CategoryService;
import com.aixforce.common.model.Response;
import com.rrs.coupons.service.CouponsManageService;
import com.rrs.coupons.service.CouponsRrsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by rock.yuan on 4/23/14.
 */
@Controller
@RequestMapping("/api/admin/couponsmanage")
public class CouponsController {
	private final static Logger log = LoggerFactory
			.getLogger(CouponsController.class);
	@Autowired
	private CouponsRrsService couponsRrsService;

	@Autowired
	private CategoryService categoryService;

    @Autowired
    private CouponsManageService couponsManageService;


    @RequestMapping(value = "/searchCoupons", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Response<String> searchCoupons(@RequestParam("shopAcc") String shopAcc,@RequestParam("channel") String channel,@RequestParam("status") String statuss,@RequestParam("shopName") String shopName,@RequestParam("pageCount") int pageCount){
        int count = couponsRrsService.adminCount();
        if(pageCount*25>=count){
            pageCount = pageCount -1;
        }
        Response<String> result = new Response<String>();
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:MM:ss");
        int tempPage = 0;
        if(pageCount!=0){
            tempPage = pageCount*25;
        }
        Map<String,Object> map2 = new HashMap<String,Object>();
        map2.put("shopAcc",shopAcc);
        map2.put("channelCou",channel);
        map2.put("statusCou",statuss);
        map2.put("shopName",shopName);
        map2.put("page",tempPage);
        List<Map> list = couponsManageService.searchAll(map2);
        StringBuilder stb = new StringBuilder();
        if(list!=null&&list.size()!=0){
            for(Map map :list){
                int term = Integer.parseInt(map.get("term").toString())/100;
                int amount = Integer.parseInt(map.get("amount").toString())/100;
                String status =Integer.parseInt(map.get("status").toString()) == 0 ? "未生效" :Integer.parseInt(map.get("status").toString()) == 1 ? "暂停" : Integer.parseInt(map.get("status").toString())== 2 ? "生效" :Integer.parseInt(map.get("status").toString()) == 3 ? "失效" : Integer.parseInt(map.get("status").toString()) == 4 ? "撤销":"获取状态失败";
                String tempType="1".equals(map.get("channelId").toString())?"家电":"2".equals(map.get("channelId").toString())?"家具":"3".equals(map.get("channelId").toString())?"家装":"4".equals(map.get("channelId").toString())?"家饰":"5".equals(map.get("channelId").toString())?"净水":"6".equals(map.get("channelId").toString())?"RRS广场":"获取频道错误";
                stb.append("<tr><td>"+tempType+"</td><td>"+map.get("user_name").toString()+"</td><td>"+map.get("name").toString()+"</td>");
                stb.append("<td>"+map.get("cpName").toString()+"</td><td>满"+term+"元减"+amount+"元</td>");
                stb.append("<td>"+df.format(map.get("created_at"))+"</td><td>"+map.get("sendNum")+"/"+map.get("couponReceive")+"/"+map.get("couponReceive")+"</td>");
                stb.append("<td>"+status+"</td>");
                if(Integer.parseInt(map.get("status").toString())!=4) {
                    stb.append("<td><input type=\"button\" class=\"btn\" onclick=\"chexiao(" + map.get("id") + ")\" value=\"撤销\"></td>");
                }
                stb.append("</tr>");
            }
            stb.append("<input type=\"hidden\" value=\""+pageCount+"\" id=\"pageCount\" />");


        }else{

            stb.append("<input type=\"hidden\" value=\""+pageCount+"\" id=\"pageCount\" />");
        }
        result.setResult(stb.toString());
        return result;
    }

    @RequestMapping(value = "/findAllCoupons", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public Response<String> queryCouponsBy(@RequestParam("pageCount") int pageCount){
        int count = couponsRrsService.adminCount();
        if(pageCount*25>=count){
            pageCount = pageCount -1;
        }
        List<Map> list  = couponsManageService.findAll(pageCount);
        Response<String> result = new Response<String>();
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:MM:ss");
        StringBuilder stb = new StringBuilder();
        if(list!=null&&list.size()!=0){
            for(Map map :list){
                int term = Integer.parseInt(map.get("term").toString())/100;
                int amount = Integer.parseInt(map.get("amount").toString())/100;
                String status =Integer.parseInt(map.get("status").toString()) == 0 ? "未生效" :Integer.parseInt(map.get("status").toString()) == 1 ? "暂停" : Integer.parseInt(map.get("status").toString())== 2 ? "生效" :Integer.parseInt(map.get("status").toString()) == 3 ? "失效" : Integer.parseInt(map.get("status").toString()) == 4 ? "撤销":"获取状态失败";
                String tempType="1".equals(map.get("channelId").toString())?"家电":"2".equals(map.get("channelId").toString())?"家具":"3".equals(map.get("channelId").toString())?"家装":"4".equals(map.get("channelId").toString())?"家饰":"5".equals(map.get("channelId").toString())?"净水":"6".equals(map.get("channelId").toString())?"RRS广场":"获取频道错误";
                stb.append("<tr><td>"+tempType+"</td><td>"+map.get("user_name").toString()+"</td><td>"+map.get("name").toString()+"</td>");
                stb.append("<td>"+map.get("cpName").toString()+"</td><td>满"+term+"元减"+amount+"元</td>");
                stb.append("<td>"+df.format(map.get("created_at"))+"</td><td>"+map.get("sendNum")+"/"+map.get("couponReceive")+"/"+map.get("couponReceive")+"</td>");
                stb.append("<td>"+status+"</td>");
                if(Integer.parseInt(map.get("status").toString())!=4) {
                    stb.append("<td><input type=\"button\" class=\"btn\" onclick=\"chexiao(" + map.get("id") + ")\" value=\"撤销\"></td>");
               }
                stb.append("</tr>");
            }
            stb.append("<input type=\"hidden\" value=\""+pageCount+"\" id=\"pageCount\" />");


        }else{

            stb.append("<input type=\"hidden\" value=\""+pageCount+"\" id=\"pageCount\" />");
        }
        result.setResult(stb.toString());
        return result;
    }
    @RequestMapping(value = "/chexiaoCounpons", method = RequestMethod.POST)
    @ResponseBody
    public Response<String> chexiaoCounpons(@RequestParam("couponsId") long couponsId){
            couponsManageService.chexiaoCoupons(couponsId);
        Response<String> result = new Response<String>();
        result.setResult("200");
        return result;
    }

	@RequestMapping(value = "/addCoupon", method = RequestMethod.POST)
	@ResponseBody
	public void addCoupon(HttpServletRequest request) {
		log.info("tian jia addCoupon");
		Map<String, Object> paramMap = new HashMap<String, Object>();
		paramMap.put(
				"channelId",
				request.getParameter("channelId") == null ? "0" : request
						.getParameter("channelId"));
		paramMap.put("cpName", request.getParameter("cpName") == null ? ""
				: request.getParameter("cpName"));
		paramMap.put(
				"startTime",
				request.getParameter("startTime") == null ? "" : request
						.getParameter("startTime") + " 00:00:00");
		paramMap.put("endTime", request.getParameter("endTime") == null ? ""
				: request.getParameter("endTime") + " 23:59:59");
		if (checkDate(
				request.getParameter("startTime") == null ? ""
						: request.getParameter("startTime") + " 00:00:00",
				request.getParameter("endTime") == null ? "" : request
						.getParameter("endTime") + " 23:59:59")) {
			// 优惠券生效
			paramMap.put("status", "2");
		} else {
			// 优惠券未生效
			paramMap.put("status", "0");
		}
		paramMap.put("area", request.getParameter("area") == null ? "1"
				: request.getParameter("area"));
		paramMap.put(
				"IdStr",
				request.getParameter("IdStr") == null ? "" : request
						.getParameter("IdStr").substring(0,
								request.getParameter("IdStr").length() - 1));
		paramMap.put("nameStr", request.getParameter("nameStr") == null ? ""
				: request.getParameter("nameStr"));
		paramMap.put("term", request.getParameter("term") == null ? "0"
				: new BigDecimal(
						request.getParameter("term")).multiply(
						new BigDecimal(100)).toString());
		paramMap.put(
				"amount",
				request.getParameter("amount") == null ? "0" : new BigDecimal(
						request.getParameter("amount")).multiply(
						new BigDecimal(100)).toString());
		paramMap.put("userType", request.getParameter("userType") == null ? "1"
				: request.getParameter("userType"));
		paramMap.put("useLimit", request.getParameter("useLimit") == null ? "0"
				: request.getParameter("useLimit"));
		paramMap.put("sendNum", request.getParameter("sendNum") == null ? "0"
				: request.getParameter("sendNum"));
		paramMap.put("sendType", request.getParameter("sendType") == null ? "2"
				: request.getParameter("sendType"));
		paramMap.put(
				"sendStartTime",
				request.getParameter("sendStartTime") == null ? "" : request
						.getParameter("sendStartTime"));
		paramMap.put(
				"sendEndTime",
				request.getParameter("sendEndTime") == null ? "" : request
						.getParameter("sendEndTime"));
		paramMap.put(
				"sendOrigin",
				request.getParameter("sendOrigin") == null ? "" : request
						.getParameter("sendOrigin"));
		paramMap.put(
				"costsBear",
				request.getParameter("costsBear_str") == null ? "1" : request
						.getParameter("costsBear_str"));
		paramMap.put("memo", request.getParameter("memo") == null ? ""
				: request.getParameter("memo"));
        paramMap.put("couponsType",1L);
		couponsRrsService.addCoupon(paramMap);
	}

	@RequestMapping(value = "/findCategory", method = RequestMethod.POST)
	@ResponseBody
	public List<Map<String, Object>> findCategory(
			@RequestParam("categoryId") Integer categoryId) {
		log.info("findCategory start");
		// categoryService.assignCategoryService(1);

		List<Map<String, Object>> map = couponsRrsService
				.findCategory(categoryId);
		return map;

	}

	@RequestMapping(value = "/updateCoupon", method = RequestMethod.POST)
	@ResponseBody
	public void updateCoupon(HttpServletRequest request) {
		log.info("updateCoupon start");
		Map<String, Object> paramMap = new HashMap<String, Object>();
		paramMap.put(
				"id",
				request.getParameter("id") == null ? "0" : request
						.getParameter("id"));
		paramMap.put(
				"channelId",
				request.getParameter("channelId") == null ? "0" : request
						.getParameter("channelId"));
		paramMap.put("cpName", request.getParameter("cpName") == null ? ""
				: request.getParameter("cpName"));
		paramMap.put(
				"startTime",
				request.getParameter("startTime") == null ? "" : request
						.getParameter("startTime") + " 00:00:00");
		paramMap.put("endTime", request.getParameter("endTime") == null ? ""
				: request.getParameter("endTime") + " 23:59:59");

		if (checkDate(
				request.getParameter("startTime") == null ? ""
						: request.getParameter("startTime") + " 00:00:00",
				request.getParameter("endTime") == null ? "" : request
						.getParameter("endTime") + " 23:59:59")) {
			// 优惠券生效
			paramMap.put("status", "2");
		} else {
			// 优惠券未生效
			paramMap.put("status", "0");
		}

		paramMap.put("area", request.getParameter("area") == null ? "1"
				: request.getParameter("area"));
		paramMap.put(
				"IdStr",
				request.getParameter("IdStr") == null ? "" : request
						.getParameter("IdStr").substring(0,
								request.getParameter("IdStr").length() - 1));
		paramMap.put("nameStr", request.getParameter("nameStr") == null ? ""
				: request.getParameter("nameStr"));
		paramMap.put("term", request.getParameter("term") == null ? "0"
				: new BigDecimal(
						request.getParameter("term")).multiply(
						new BigDecimal(100)).toString());
		paramMap.put(
				"amount",
				request.getParameter("amount") == null ? "0" : new BigDecimal(
						request.getParameter("amount")).multiply(
						new BigDecimal(100)).toString());
		paramMap.put("userType", request.getParameter("userType") == null ? "1"
				: request.getParameter("userType"));
		paramMap.put("useLimit", request.getParameter("useLimit") == null ? "0"
				: request.getParameter("useLimit"));
		paramMap.put("sendNum", request.getParameter("sendNum") == null ? "0"
				: request.getParameter("sendNum"));
		paramMap.put("sendType", request.getParameter("sendType") == null ? "2"
				: request.getParameter("sendType"));
		paramMap.put(
				"sendStartTime",
				request.getParameter("sendStartTime") == null ? "" : request
						.getParameter("sendStartTime"));
		paramMap.put(
				"sendEndTime",
				request.getParameter("sendEndTime") == null ? "" : request
						.getParameter("sendEndTime"));
		paramMap.put(
				"sendOrigin",
				request.getParameter("sendOrigin") == null ? "" : request
						.getParameter("sendOrigin"));
		paramMap.put(
				"costsBear",
				request.getParameter("costsBear_str") == null ? "1" : request
						.getParameter("costsBear_str"));
		paramMap.put("memo", request.getParameter("memo") == null ? ""
				: request.getParameter("memo"));
		couponsRrsService.updateCoupon(paramMap);

	}

	@RequestMapping(value = "/updateCouponStatus", method = RequestMethod.POST)
	@ResponseBody
	public void updateCouponStatus(@RequestParam("id") Integer id,
			@RequestParam("status") Integer status) {
		log.info("updateCouponStatus start");
		Map<String, Object> paramMap = new HashMap<String, Object>();
		paramMap.put("status", status);
		paramMap.put("id", id);
		couponsRrsService.updateCouponStatus(paramMap);
	}

	/**
	 * 判断当前日期是否在有效期内
	 * 
	 * @param startTime
	 * @param endTime
	 * @return
	 */
	private static boolean checkDate(String startTime, String endTime) {
		log.info("CouponsController checkDate format datime start");
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		try {
			Date start = sdf.parse(startTime);
			Date end = sdf.parse(endTime);
			Date nowDate = new Date();
			if (nowDate.after(start) && nowDate.before(end)) {
				return true;
			} else {
				return false;
			}
		} catch (ParseException e) {
			log.error("CouponsController checkDate format datime error");
			e.printStackTrace();
			return false;
		}

	}

	// public static void main(String[] args) {
	// System.err.println(checkDate("2014-09-02 12:12:22","2014-09-02 14:12:23"));
	// }
}
