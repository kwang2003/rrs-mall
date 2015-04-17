package com.aixforce;

import com.github.kevinsawicki.http.HttpRequest;
import com.google.common.collect.Maps;
import org.junit.Test;

import java.util.Map;

/**
 * 快递100
 * Author: haolin
 * On: 9/25/14
 */

public class Express100Test {
    /**
     * 参数大揭秘:
     *
        #### 请求参数 ####

        id	        String	是	身份授权key，请 快递查询接口 进行申请（大小写敏感）
        com	        String	是	要查询的快递公司代码，不支持中文，对应的公司代码见
                                《API URL 所支持的快递公司及参数说明》和《支持的国际类快递及参数说明》。
                                如果找不到您所需的公司，请发邮件至 kuaidi@kingdee.com 咨询（大小写不敏感）
        nu	        String	是	要查询的快递单号，请勿带特殊符号，不支持中文（大小写不敏感）
        valicode	String	是	已弃用字段，无意义，请忽略。
        show	    String	是	返回类型：
                                    0：返回json字符串，
                                    1：返回xml对象，
                                    2：返回html对象，
                                    3：返回text文本。
                                    如果不填，默认返回json字符串。
        muti	    String	是	 返回信息数量：
                                    1:返回多行完整的信息，
                                    0:只返回一行信息。
                                    不填默认返回多行。
        order	    String	是	 排序：
                                    desc：按时间由新到旧排列，
                                    asc：按时间由旧到新排列。
                                    不填默认返回倒序（大小写不敏感）

        #### 返回参数 ####

        com	物流公司编号
        nu	物流单号
        time	每条跟踪信息的时间
        context	每条跟综信息的描述
        state	快递单当前的状态 ：　
            0：在途，即货物处于运输过程中；
            1：揽件，货物已由快递公司揽收并且产生了第一条跟踪信息；
            2：疑难，货物寄送过程出了问题；
            3：签收，收件人已签收；
            4：退签，即货物由于用户拒签、超区等原因退回，而且发件人已经签收；
            5：派件，即快递正在进行同城派件；
            6：退回，货物正处于退回发件人的途中；
                该状态还在不断完善中，若您有更多的参数需求，欢迎发邮件至 kuaidi@kingdee.com 提出。
        status	查询结果状态：
            0：物流单暂无结果，
            1：查询成功，
            2：接口出现异常，

       {
            "message":"ok",
            "status":"1",
            "state":"3",
            "data":
                [
                    {"time":"2012-07-07 13:35:14","context":"客户已签收"},
                    {"time":"2012-07-07 09:10:10","context":"离开 [北京石景山营业厅] 派送中，递送员[温]，电话[]"},
                    {"time":"2012-07-06 19:46:38","context":"到达 [北京石景山营业厅]"},
                    {"time":"2012-07-06 15:22:32","context":"离开 [北京石景山营业厅] 派送中，递送员[温]，电话[]"},
                    {"time":"2012-07-06 15:05:00","context":"到达 [北京石景山营业厅]"},
                    {"time":"2012-07-06 13:37:52","context":"离开 [北京_同城中转站] 发往 [北京石景山营业厅]"},
                    {"time":"2012-07-06 12:54:41","context":"到达 [北京_同城中转站]"},
                    {"time":"2012-07-06 11:11:03","context":"离开 [北京运转中心驻站班组] 发往 [北京_同城中转站]"},
                    {"time":"2012-07-06 10:43:21","context":"到达 [北京运转中心驻站班组]"},
                    {"time":"2012-07-05 21:18:53","context":"离开 [福建_厦门支公司] 发往 [北京运转中心_航空]"},
                    {"time":"2012-07-05 20:07:27","context":"已取件，到达 [福建_厦门支公司]"}
                ]
        }
     */
    public void testByApi(){
        String express100Url = "http://api.kuaidi100.com/api";
        String express100Key = "6ae90c587ee5876a";
        String code = "shentong";
        String no = "888373504855";
        Map<String, String> params = Maps.newHashMapWithExpectedSize(4);
        params.put("id", express100Key);
        params.put("com", code);
        params.put("nu", no);
        params.put("order", "asc");
        params.put("show", "0");
        HttpRequest request = HttpRequest.get(express100Url, params, false);
        System.err.println(request.body());
    }

    @Test
    public void testByHtmlApi(){
        String express100Url = "http://www.kuaidi100.com/applyurl";
        String express100Key = "6ae90c587ee5876a";
        String code = "shentong";
        String no = "888373504855";
        Map<String, String> params = Maps.newHashMapWithExpectedSize(4);
        params.put("key", express100Key);
        params.put("com", code);
        params.put("nu", no);
        HttpRequest request = HttpRequest.get(express100Url, params, false);
        System.err.println(request.body());
    }

    @Test
    public void testByChaxun(){
        String express100Url = "http://www.kuaidi100.com/chaxun";
        String code = "ems";
        String no = "1004040389508";
        Map<String, String> params = Maps.newHashMapWithExpectedSize(4);
        params.put("com", code);
        params.put("nu", no);
        HttpRequest request = HttpRequest.get(express100Url, params, false);
        System.err.println(request.body());
    }
}