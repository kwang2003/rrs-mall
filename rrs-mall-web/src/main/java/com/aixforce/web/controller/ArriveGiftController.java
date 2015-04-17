package com.aixforce.web.controller;

import com.aixforce.common.model.Response;
import com.aixforce.exception.JsonResponseException;
import com.aixforce.sms.SmsService;
import com.aixforce.user.base.BaseUser;
import com.aixforce.user.base.UserUtil;
import com.aixforce.web.controller.api.CaptchaGenerator;
import com.aixforce.web.misc.MessageSources;
import com.rrs.arrivegift.dto.ArriveSmsInfoDto;
import com.rrs.arrivegift.model.Experince;
import com.rrs.arrivegift.model.ReserveSmsConfig;
import com.rrs.arrivegift.model.ReserveSmsInfos;
import com.rrs.arrivegift.model.ShopGiftConfig;
import com.rrs.arrivegift.service.ExperinceService;
import com.rrs.arrivegift.service.ReserveSmsConfigService;
import com.rrs.arrivegift.service.ReserveSmsInfosService;
import com.rrs.arrivegift.service.ShopGiftConfigService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import static com.google.common.base.Objects.equal;

/**
 * Created by zhum01 on 2014/10/15.
 */
@Slf4j
@Controller
@RequestMapping("/api")
public class ArriveGiftController {

    @Autowired
    private SmsService smsService;

    @Autowired
    private CaptchaGenerator captchaGenerator;

    @Autowired
    private ReserveSmsInfosService reserveSmsInfosService;

    @Autowired
    private ShopGiftConfigService shopGiftConfigService;

    @Autowired
    private ExperinceService experinceService;

    @Autowired
	private MessageSources messageSources;

    @Autowired
    private ReserveSmsConfigService reserveSmsConfigService;

    /**
     * checkMall 检查 mall是否开启到店有礼组件
     * **/
    @RequestMapping(value = "/checkMall",method = RequestMethod.GET)
    @ResponseBody
    public String checkMall(@RequestParam("shopId") Long shopId){
        String resultValue = "0";//不符合当前配置 不允许选择

        return resultValue;
    }

     /**
     * 判断预约时间是否符合当前店铺设置的时候
     * **/
    @RequestMapping(value = "/checkAriveTime",method = RequestMethod.GET)
    @ResponseBody
    public String checkAriveTime(@RequestParam("sendDate") String sendDate,@RequestParam("shopId") Long shopId,@RequestParam("shopType") Long shopType){
        String resultValue = "0";//不符合当前配置 不允许选择

        resultValue = checkSendDate(shopType,shopId,sendDate);


//        String weekday = "";
//        if(equal(shopType,1L)){
//            Response<ShopGiftConfig> shopGiftConfigResponse =  shopGiftConfigService.findShopGift(shopId);
//            if(shopGiftConfigResponse.isSuccess()){ //获取店铺的配置信息
//                ShopGiftConfig shopGiftConfig = shopGiftConfigResponse.getResult();
//                   weekday = shopGiftConfig.getWeekday();
//            }
//        }else if(equal(shopType,2L)){
//            Response<Experince> experConfigResponse =  experinceService.queryExperinceByMap(shopId);
//            if(experConfigResponse.isSuccess()){ //获取店铺的配置信息
//                Experince experince = experConfigResponse.getResult();
//                //判断当前是否在配置之内的日期
//                weekday = experince.getWeekday();
//            }
//        }
//        int dayOfWeek = 0;
//        Date formatDate = null;
//        formatDate =  formatStrToDate(sendDate,"yyyy-MM-dd");
//        Calendar calendar = Calendar.getInstance();
//        calendar.setTime(formatDate);
//        dayOfWeek = getChineseWeek(calendar);
//        //判断当前是否在配置之内的日期
//        if(weekday!=null && !weekday.equals("")){
//            if(weekday.contains(String.valueOf(dayOfWeek))){
//                resultValue = "1";
//            }
//        }
        return resultValue;
    }

    /**
     * 判断输入验证码是否正确
     * **/
    @RequestMapping(value = "/checkVcode",method = RequestMethod.GET)
    @ResponseBody
    public String checkVcode(@RequestParam("vCode") String vCode,HttpSession session){
        String resultValue = "0";//不符合当前配置 不允许选择
        if (vCode.equalsIgnoreCase(captchaGenerator
                .getGeneratedKey(session))) {
            resultValue = "1";
        }
        return resultValue;
    }

    public static int getChineseWeek(Calendar date) {
//        final String dayNames[] = { "星期日", "星期一", "星期二", "星期三", "星期四", "星期五",
//                "星期六" };
        int dayOfWeek = date.get(Calendar.DAY_OF_WEEK);
        if (equal(dayOfWeek, 1)) {
            dayOfWeek = 7;
        }else{
            dayOfWeek = dayOfWeek - 1;
        }
        return dayOfWeek;

    }

    public String checkSendDate(Long shopType,Long shopId,String sendDate){
        String resultValue = "0";
        String weekday = "";
        if(equal(shopType,1L)){
            Response<ShopGiftConfig> shopGiftConfigResponse =  shopGiftConfigService.findShopGift(shopId);
            if(shopGiftConfigResponse.isSuccess()){ //获取店铺 的配置信息
                ShopGiftConfig shopGiftConfig = shopGiftConfigResponse.getResult();
                weekday = shopGiftConfig.getWeekday();
            }
        }else if(equal(shopType,2L)){
            Response<Experince> experConfigResponse =  experinceService.queryExperinceByMap(shopId);
            if(experConfigResponse.isSuccess()){ //获取店铺的配置信息
                Experince experince = experConfigResponse.getResult();
                //判断当前是否在配置之内的日期
                weekday = experince.getWeekday();
            }
        }
        int dayOfWeek = 0;
        Date formatDate = null;
        formatDate =  formatStrToDate(sendDate,"yyyy-MM-dd");
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(formatDate);
        dayOfWeek = getChineseWeek(calendar);
        //判断当前是否在配置之内的日期
        if(weekday!=null && !weekday.equals("")){
            if(weekday.contains(String.valueOf(dayOfWeek))){
                resultValue = "1";
            }
        }
        return resultValue;
    }

    /**
     * 发送消息
     * **/
    @RequestMapping(value = "/arriveSms", method = RequestMethod.POST)
    @ResponseBody
    public String sendMessage(HttpSession session,ArriveSmsInfoDto arriveSmsInfoDto) {
        String resultValue = "0";




        //校验 验证“手机号+用户名+店铺”，每天只能发送一次
//        arriveSmsInfoDto.getSendTele()   arriveSmsInfoDto.getShopId()
        Response<ReserveSmsInfos> reserveSmsInfosResponse = null;
        BaseUser baseUser = UserUtil.getCurrentUser();
        if(equal(arriveSmsInfoDto.getType(),1L)){
            reserveSmsInfosResponse = reserveSmsInfosService.checkSmsInfosBy(arriveSmsInfoDto.getSendTele(),baseUser,arriveSmsInfoDto.getShopId(),arriveSmsInfoDto.getType());
        }else if(equal(arriveSmsInfoDto.getType(),2L)){
            reserveSmsInfosResponse = reserveSmsInfosService.checkSmsInfosBy(arriveSmsInfoDto.getSendTele(),baseUser,arriveSmsInfoDto.getShopId(),arriveSmsInfoDto.getType());
        }
        if(reserveSmsInfosResponse.getResult()!=null){
            return resultValue;
        }

        resultValue = checkSendDate(arriveSmsInfoDto.getType(),arriveSmsInfoDto.getShopId(),arriveSmsInfoDto.getSendDate());
        if(resultValue.equals("0")){
            return resultValue;
        }


        Response<ReserveSmsConfig> reserveSmsConfigResponse =  reserveSmsConfigService.querySmsConfigInfo(arriveSmsInfoDto.getType(), arriveSmsInfoDto.getShopId());
        StringBuffer sb = new StringBuffer("预约成功！");// 组织短信内容
        sb.append(arriveSmsInfoDto.getShopName()).append(",").append(arriveSmsInfoDto.getAddress()).append(",").append(arriveSmsInfoDto.getPhoneNo()).append(",").append(arriveSmsInfoDto.getSendDate()).append(arriveSmsInfoDto.getSendTime());
        if(reserveSmsConfigResponse.isSuccess()){
            if(reserveSmsConfigResponse.getResult()!=null){
                sb.append(",").append(reserveSmsConfigResponse.getResult().getSmsInfo());
            }
        }


        ReserveSmsInfos reserveSmsInfos = new ReserveSmsInfos();
        // 获取当前登陆用户的手机和姓名
        if (baseUser != null) {
            reserveSmsInfos.setUserId(baseUser.getId());// 登陆用户的用户Id
        }
        reserveSmsInfos.setShopId(arriveSmsInfoDto.getShopId());
        reserveSmsInfos.setShopName(arriveSmsInfoDto.getShopName());
        reserveSmsInfos.setAddress(arriveSmsInfoDto.getAddress());

        reserveSmsInfos.setReserveType(arriveSmsInfoDto.getReserveType());//1上午  2下午
        reserveSmsInfos.setUserName(arriveSmsInfoDto.getSendName());
        reserveSmsInfos.setPhoneNo(arriveSmsInfoDto.getSendTele());
        reserveSmsInfos.setReserveDate(arriveSmsInfoDto.getSendDate());
        reserveSmsInfos.setReserveTime(arriveSmsInfoDto.getSendTime());
        reserveSmsInfos.setConfigId(arriveSmsInfoDto.getShopConfigId());
        reserveSmsInfos.setSmsInfo(sb.toString());
        reserveSmsInfos.setType(arriveSmsInfoDto.getType());// 1 商家店铺 2 体验馆MALL
        reserveSmsInfos.setUserType(2L);// 个人用户预约 1 商家 2 个人
        reserveSmsInfos.setState(0L);// 启用状态 0 启用 1 停用

        if(equal(arriveSmsInfoDto.getType(),1L)){
            Response<ShopGiftConfig> shopGiftConfigResponse =  shopGiftConfigService.findShopGift(arriveSmsInfoDto.getShopId());
            if(shopGiftConfigResponse.isSuccess()){ //获取店铺的配置信息
                ShopGiftConfig shopGiftConfig = shopGiftConfigResponse.getResult();
                Date startDate = new Date();
                Date endDate = new Date();
                StringBuffer startTime = new StringBuffer();
                StringBuffer endTime = new StringBuffer();
                if(equal(arriveSmsInfoDto.getReserveType(),1)){//上午
                    startTime.append(arriveSmsInfoDto.getSendDate()).append(" ").append(formatSToS(shopGiftConfig.getAmstart()));
                    endTime.append(arriveSmsInfoDto.getSendDate()).append(" ").append(formatSToS(shopGiftConfig.getAmend()));
                }else{//下午
                    startTime.append(arriveSmsInfoDto.getSendDate()).append(" ").append(formatSToS(shopGiftConfig.getPmstart()));
                    endTime.append(arriveSmsInfoDto.getSendDate()).append(" ").append(formatSToS(shopGiftConfig.getPmend()));
                }
                startDate =  formatStrToDate(startTime.toString(),"yyyy-MM-dd HH:mm");
                reserveSmsInfos.setReserveStart(startDate);

                endDate =  formatStrToDate(endTime.toString(),"yyyy-MM-dd HH:mm");
                reserveSmsInfos.setReserveEnd(endDate);
            }
        }

        if(equal(arriveSmsInfoDto.getType(),2L)){
            Response<Experince> experConfigResponse =  experinceService.queryExperinceByMap(arriveSmsInfoDto.getShopId());
            if(experConfigResponse.isSuccess()){ //获取店铺的配置信息
                Experince experince = experConfigResponse.getResult();
                Date startDate = new Date();
                Date endDate = new Date();
                StringBuffer startTime = new StringBuffer();
                StringBuffer endTime = new StringBuffer();
                if(equal(arriveSmsInfoDto.getReserveType(),1)){//上午
                    startTime.append(arriveSmsInfoDto.getSendDate()).append(" ").append(formatSToS(experince.getAmStart()));
                    endTime.append(arriveSmsInfoDto.getSendDate()).append(" ").append(formatSToS(experince.getAmEnd()));
                }else{//下午
                    startTime.append(arriveSmsInfoDto.getSendDate()).append(" ").append(formatSToS(experince.getPmStart()));
                    endTime.append(arriveSmsInfoDto.getSendDate()).append(" ").append(formatSToS(experince.getPmEnd()));
                }
                startDate =  formatStrToDate(startTime.toString(),"yyyy-MM-dd HH:mm");
                reserveSmsInfos.setReserveStart(startDate);

                endDate =  formatStrToDate(endTime.toString(),"yyyy-MM-dd HH:mm");
                reserveSmsInfos.setReserveEnd(endDate);
            }
        }

        reserveSmsInfosService.create(reserveSmsInfos);


        // 	短信格式：预约成功！体验店名称+地址+联系电话+预约时间+预约内容（见预约短信需求）
        //arriveSmsInfoDto.getSendTele()
//        sb.toString()
        sendMessage(arriveSmsInfoDto.getSendTele(),sb.toString());

        return "1";
    }

    public void sendMessage(String tel,String message){
            smsService.sendSingle("000000", tel,message);
    }


    //时间自定义格式化处理
    public String formatSToS(String apmTime){
        if(apmTime!=null && !apmTime.equals("")){
            return apmTime.substring(0,2).concat(":").concat(apmTime.substring(2));
        }else{
            return "";
        }
    }


    //时间格式化 方法
    public Date formatStrToDate(String strDate,String foarmat){
        SimpleDateFormat sdf = new SimpleDateFormat(foarmat);//小写的mm表示的是分钟
        Date formatDate = null;
        try {
            formatDate = sdf.parse(strDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return formatDate;
    }

    @RequestMapping(value = "/captcha", method = RequestMethod.GET)
    @ResponseBody
    public void captcha(HttpServletRequest request,HttpServletResponse response) {
        byte[] data = captchaGenerator.captcha(request.getSession());
        String Vtext = captchaGenerator.getGeneratedText(request.getSession());
        BufferedImage images = captchaGenerator.serialize(Vtext);
        ServletOutputStream sos = null;
        try {
            sos = response.getOutputStream();
            captchaGenerator.images(sos,Vtext);
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            if (sos != null)
                try {
                    sos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
        }
    }
    
    
    /**
     * 商家中心  商家管理预约短信是否到店
     * @param id  预约id
     * @return
     */
    @RequestMapping(value = "/seller/gotinShop", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public String updateSmsInfo(@RequestParam("id") Long id) {
		Response<Boolean> result = reserveSmsInfosService
				.updateReserveSmsInfos(id);

		if (!result.isSuccess()) {
			log.error(		
					"failed to update reserveSmsinfo infos(id={}), error code:{}",
					result.getError());		
			throw new JsonResponseException(500, messageSources.get(result
					.getError()));
		}
		return "ok";
	}

}
