package com.rrs.third.common;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
* Created by zhaop01 on 2014/9/2.
*/
public class CommonUtils {

    static SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddhhmmss");


    // 获取XML字符串中的节点内容
    public static List<?> getXmlValue(String text,String pathExpress) throws DocumentException {
        Document doc = DocumentHelper.parseText(text);
        Element e = doc.getRootElement();
        List<?> nodeList = e.selectNodes(pathExpress);
        return nodeList;
    }
    public static String getTime(){
       return  sdf.format(new Date());
    }

    /**
     * 海尔接口系统参数
     * @param env
     * @return
     */
    public static ApiParams getHaierApiParams(String env){
        ApiParams params = new ApiParams();
        if("test".equals(env)){ // 测试环境
            params.setSource("haier");
            params.setCoAppName("haier");
            params.setSecretKey("1234");
            params.setUrl("http://test.haier.com/ids/service?idsServiceType=httpssoservice&serviceName=findUserBySSOID");
            params.setSecurityType("itzTyAusn6b4");
        }else{
            params.setSource("haier");
            params.setCoAppName("haier");
            params.setSecretKey("1234");
            params.setUrl("http://www.haier.com/ids/service?idsServiceType=httpssoservice&serviceName=findUserBySSOID");
            params.setSecurityType("bMy?gQGhjJrj");
        }
        return params;
    }

    /**
     * E海尔接口系统参数
     * @param env
     * @return
     */
    public static ApiParams getEhaierApiParams(String env){
        ApiParams params = new ApiParams();
        if("test".equals(env)){ // 测试环境
            params.setSource("ehaier");
            params.setCoAppName("haier");
            params.setSecretKey("123456");
            params.setUrl("http://www.testehaier.com/api/sso.php");
        }else{
            params.setSource("ehaier");
            params.setCoAppName("haier");
            params.setSecretKey("ehaier&2ab*(_");
            params.setUrl("http://www.ehaier.com/api/sso.php");
        }
        return params;
    }
}
