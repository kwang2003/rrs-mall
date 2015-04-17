package com.aixforce.admin.web.jobs;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.aixforce.sms.SmsService;
import com.aixforce.trade.service.OrderQueryService;

@Component
public class PresaleOrderSmsJobs {
	@Autowired
    private SmsService smsService;
	@Autowired
    private OrderQueryService orderQueryService;
	/**
     * run every 30s
     * 判断预售是否已结束,预售结束以后发短信通知
     */
    @Scheduled(cron = "0 */15 * * * *")
    public void smsContent(){
    	List<Map<String, Object>> moblieList=orderQueryService.getMoblieList();
    	List<String> list=new ArrayList<String>();
    	for (int i = 0; i < moblieList.size(); i++) {
    		list.add(moblieList.get(i).get("itemId").toString());
    	}
    	if(moblieList.size()>0){
    	Map<String, Object> map=new HashMap<String, Object>();
    	map.put("itemIdList", list);
    	orderQueryService.updateSmsFloag(map);
    	for (int i = 0; i < moblieList.size(); i++) {
    		sendSms(moblieList.get(i).get("moblie").toString(),"亲爱的【"+moblieList.get(i).get("name").toString()+"】，请及时支付您【"+moblieList.get(i).get("orderDate").toString()+"】预购商品【"+moblieList.get(i).get("itemName").toString()+"】订单尾款，以免货被抢空。");
		}
    	}
    	
    	
    }
    private void sendSms(String moblie,String content){    	
    	smsService.sendSingle("000000", moblie, content);
    }

}
