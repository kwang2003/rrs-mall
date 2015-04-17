package com.aixforce.sms.inner;

import org.junit.Test;

/**
 * Author:  <a href="mailto:jlchen.cn@gmail.com">jlchen</a>
 * Date: 2013-05-24
 */
public class XMLMapperTest {
    @Test
    public void testFromXML() throws Exception {
        String xml = "<?xml version=\"1.0\" encoding=\"gbk\"?>\n" +
                "<response>\n" +
                "<code>00</code>\n" +
                "<message>\n" +
                "\t<desmobile>13900000000</desmobile>\n" +
                "\t<msgid>200811041234253654785</msgid>\n" +
                "</message>\n" +
                "<message>\n" +
                "\t<desmobile>13400000000</desmobile>\n" +
                "\t<msgid>200811041234253654786</msgid>\n" +
                "</message>\n" +
                "<message>\n" +
                "\t<desmobile>13500000000</desmobile>\n" +
                "\t<msgid>200811041234253654787</msgid>\n" +
                "</message>\n" +
                "<message>\n" +
                "\t<desmobile>13600000000</desmobile>\n" +
                "\t<msgid>200811041234253654788</msgid>\n" +
                "</message>\n" +
                "</response>\n";
        System.out.println(XMLMapper.fromXML(xml));
    }
}
