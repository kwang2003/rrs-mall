package com.aixforce.haier;


import com.aixforce.sms.haier.*;
import com.google.common.collect.Lists;
import junit.framework.TestCase;
import org.junit.Test;

public class XmlTranslatorTest extends TestCase {


    @Test
    public void testToXml() {
        SmsRequestMessage message = new SmsRequestMessage("12311111111", "Message", "15");
        SmsRequestBody body = new SmsRequestBody("", Lists.newArrayList(message, message));
        SmsRequest request = new SmsRequest("1234", "4567", body);
        String xml = XmlTranslator.toXML(request);
        System.out.println(xml);
        request = XmlTranslator.fromXML(xml);
        System.out.println(request.getBody().getMessageList().get(0).getContent());

    }

    @Test
    public void testFromXml() {
        SmsResponseMessage message = new SmsResponseMessage("12311111111", "W20090222163305220262");
        SmsResponseBody body = new SmsResponseBody("0", Lists.newArrayList(message, message, message));
        SmsResponse response = new SmsResponse("", "", "", "", body);
        String xml = XmlTranslator.toXML(response);
        System.out.println(xml);
        response = XmlTranslator.fromXML(xml);
        System.out.println(response.getBody().getCode());


    }

    @Test
    public void testFromXmlByText() {
        String xml =
                "<CoreSMS>\n" +
                        "    <OperID></OperID>\n" +
                        "    <OperPass></OperPass>\n" +
                        "    <Action></Action>\n" +
                        "    <Category></Category>\n" +
                        "    <Body>\n" +
                        "        <Code>0</Code>\n" +
                        "        <Message>\n" +
                        "            <DesMobile>12311111111</DesMobile>\n" +
                        "            <SMSID>W20090222163305220262</SMSID>\n" +
                        "        </Message>\n" +
                        "        <Message>\n" +
                        "            <DesMobile>12311111111</DesMobile>\n" +
                        "            <SMSID>W20090222163305220262</SMSID>\n" +
                        "        </Message>\n" +
                        "        <Message>\n" +
                        "            <DesMobile>12311111111</DesMobile>\n" +
                        "            <SMSID>W20090222163305220262</SMSID>\n" +
                        "        </Message>\n" +
                        "    </Body>\n" +
                        "</CoreSMS>";
        System.out.print(xml);
        SmsResponse response = XmlTranslator.fromXML(xml);
        System.out.println(response.getBody().getCode());


    }


}
