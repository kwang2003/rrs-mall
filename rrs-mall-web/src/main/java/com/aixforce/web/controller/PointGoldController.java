package com.aixforce.web.controller;

import com.aixforce.item.service.ItemService;
import com.aixforce.user.base.BaseUser;
import com.aixforce.user.base.UserUtil;
import com.aixforce.web.dto.JsonValue;
import com.aixforce.web.misc.MessageSources;
import com.aixforce.web.utils.RSAToBCDCoder;
import com.alibaba.dubbo.common.json.JSON;
import com.alibaba.dubbo.common.json.ParseException;
import com.google.common.base.Objects;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by zhum01 on 2014/11/6.
 */
@Slf4j
@Controller
@RequestMapping("/api/pointGold")
public class PointGoldController {

    @Autowired
    private ItemService itemService;

    @Autowired
    private MessageSources messageSources;

    @Value("#{app.pointGoldUrl}")
    private String pointGoldUrl;

//    #获取用户点击金币接口url
    @Value("#{app.showUserGoldUrl}")
    private String showUserGoldUrl;

    @Getter
    @Setter
    private String showGoldKey = "client_rrs";

    @RequestMapping(value = "/showPoints", method = RequestMethod.GET)
    @ResponseBody
    public Map<String, Object> showPoints(){
        Map<String, Object> map = new HashMap<String, Object>();
        //查询查看商品信息的是否是登陆用户
        BaseUser baseUser = UserUtil.getCurrentUser();
//        ?api=userpointstotal&query={"id":"12000099"}&sign=c74391dcbbb3566309b649bce5c4e6ef
        if(baseUser!=null){
            String messages = "";
            RSAToBCDCoder rsaToBCDCoder = new RSAToBCDCoder();
            //http://haierrrsdev.oceanus-data.com/api/v1/points?api=userpointstotal&query={%22id%22:%2212000099%22}&sign=c74391dcbbb3566309b649bce5c4e6ef
//            根据地址获取积分加密数据
            StringBuffer queryUrl = new StringBuffer(showUserGoldUrl);
            StringBuffer queryParam = new StringBuffer("api=userpointstotal");

            queryParam.append("&query=").append("{").append("\"id\":").append("\"").append(baseUser.getId()).append("\"").append("}");
            StringBuffer keyParam = new StringBuffer(queryParam.toString());//.append(strkey.toString()).toString();
            if(getShowGoldKey()!=null && !getShowGoldKey().equals("")){
                keyParam.append("&key=").append(getShowGoldKey());
            }
            String sign = rsaToBCDCoder.MD5(keyParam.toString());
            queryParam.append("&sign=").append(sign.toLowerCase());
            queryUrl.append(queryParam);

            String returnValue = httpurl(queryUrl.toString());
            try {
                JsonValue json  = JSON.parse(returnValue,JsonValue.class);
                if(Objects.equal(json.getStatus(),200L)){//返回成功数据
                    messages = json.getData();
                    String mingValue = rsaToBCDCoder.decryptByPrivateKey(messages);//解密处理
                    json  = JSON.parse(mingValue,JsonValue.class);
                    messages = json.getPoints();
                }else{
//                    messages = json.getMessage();
                    messages = "0";
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }
            map.put("messages",messages);
        }else{
            map.put("messages",0);
        }
        return map;
    }


    private String httpurl(String queryUrl){
        String result = "";
        try {
            URL url = new URL(queryUrl);
            HttpURLConnection connection = (HttpURLConnection) url
                    .openConnection();
            connection.setDoOutput(true);
            connection.setDoInput(true);
            connection.setRequestMethod("GET");
            connection.setUseCaches(false);
            // URLConnection.setInstanceFollowRedirects是成员函数，仅作用于当前函数
            connection.setInstanceFollowRedirects(false);
            connection.setRequestProperty("Content-Type",
                    "application/x-www-form-urlencoded");
            connection.connect();
            // 发送执行请求

            // 接收返回请求
            BufferedReader reader = new BufferedReader(new InputStreamReader(
                    connection.getInputStream(), "utf8"));
            String line = "";
            StringBuffer buffer = new StringBuffer();
            while ((line = reader.readLine()) != null) {
                buffer.append(line);
            }
            result = buffer.toString();
            connection.disconnect();
        } catch (Exception e) {
            e.printStackTrace();
        }finally{

        }
        return result;
    }



    @RequestMapping(value = "/queryPointGoldUrl", method = RequestMethod.GET)
    @ResponseBody
    public Map<String, Object> queryPointGoldUrl(@RequestParam("itemId") Long itemId){
        Map<String, Object> map = new HashMap<String, Object>();
        //查询查看商品信息的是否是登陆用户
        BaseUser baseUser = UserUtil.getCurrentUser();
        if(baseUser!=null){
            StringBuffer pointUrl = new StringBuffer(pointGoldUrl);
            StringBuffer sb = new StringBuffer();

            sb.append("{");
            sb.append("\"uid\":").append("\"").append(baseUser.getId()).append("\"");
            sb.append(",");
            sb.append("\"pid\":").append("\"").append(itemId).append("\"");
            sb.append("}");
            RSAToBCDCoder rsaToBCDCoder = new RSAToBCDCoder();
            String entryCode = rsaToBCDCoder.enctyptBypublishKey(sb.toString());
            String md5Code = rsaToBCDCoder.MD5(sb.toString());
            pointUrl.append("md5=").append(md5Code);
            pointUrl.append("&");
            pointUrl.append("data=").append(entryCode);
            map.put("isLoginUser",1);
            map.put("pointUrl",pointUrl);
            map.put("pointGoldUrl",pointGoldUrl);
        }else{
            map.put("isLoginUser",0);
        }
        return map;
    }

}
