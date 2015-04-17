package com.aixforce.haier;

import com.aixforce.sms.SmsService;
import com.aixforce.sms.haier.HaierSmsServiceImpl;
import com.aixforce.sms.haier.SmsResponse;
import com.aixforce.sms.haier.XmlTranslator;
import com.google.common.base.Charsets;
import com.google.common.collect.Lists;
import com.google.common.io.Files;
import com.google.common.io.LineProcessor;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.List;


public class HaierSmsServiceImplTest {


    @Test
    public void testXmlTrans() throws Exception {
//        SmsService spy = PowerMockito.spy(new HaierSmsServiceImpl("13524677753","13524677753","test message"));

        String path = ClassLoader.getSystemResource("haier-response-ok.xml").getPath();

        String xml = Files.readLines(new File(path), Charsets.UTF_8, new LineProcessor<String>() {
            StringBuilder sb = new StringBuilder();

            @Override
            public boolean processLine(String line) throws IOException {
                sb.append(line);
                return true;
            }

            @Override
            public String getResult() {
                return sb.toString();
            }
        });
        System.out.println(xml);


        SmsResponse spyResponse = XmlTranslator.fromXML(xml, SmsResponse.class);
        String code = spyResponse.getBody().getCode();
        System.out.println("response code is : " + code);

    }

    @Test
    public void testSendSingle() {
        SmsService service = new HaierSmsServiceImpl("goodaysh", "good!!!", "http://221.179.180.137:9836/HttpApi_Simple/submitMessage");
        service.sendSingle("000000", "13524677753", "短信网关测试120");
    }

    @Test
    public void testSendGroup() {
        SmsService service = new HaierSmsServiceImpl("goodaysh", "good!!!", "http://221.179.180.137:9836/HttpApi_Simple/submitMessage");
        List<String> toList = Lists.newArrayList("18758363200", "13524677753");

        service.sendGroup("000000", toList, "短信网关群发测试");
    }

    @Test
    public void testLongMsgSend() {
        SmsService service = new HaierSmsServiceImpl("goodaysh", "good!!!", "http://221.179.180.137:9836/HttpApi_Simple/submitMessage");
        service.sendSingle("000000", "13524677753", "在此次网间结算政策调整后，普通用户关心的是，在电信、联通用户拨打移动手机的结算费用下降后，" +
                "手机通话资费标准是否也会跟着降？而中国移动的语音、短信等业务的资费会不会增长？\n" +
                "2008年，工信部曾对网间结算进行过一次调整，但从上次的先例来看，几家运营商调整资费的力度并不大，" +
                "也就是说对普通用户的影响并不大。至于此次能否有所调整，还需要等待运营商出台各自的资费政策，" +
                "但有业内人士预计，短期对用户的影响不大。\n" +
                "对各家运营商有什么影响？\n" +
                "对于中国联通和中国电信而言，此次网间结算的调整，由于降低了对中国移动的结算费用，" +
                "这将使其净利润实现大规模增长，并可以补充资金实力，而这或许有助于这两家运营商在4G网络方面的建设。\n" +
                "中国电信在公告中表示，以2012年全年的移动网间实际话务量、短信及彩信使用量测算，" +
                "中国电信估计上述网间互联结算标准的调整将令公司2012年度结算收入减少约人民币5.6亿元，" +
                "但结算支出减少约人民币31.4亿元，最终仍然是中国电信会获得利润。");
    }
}
