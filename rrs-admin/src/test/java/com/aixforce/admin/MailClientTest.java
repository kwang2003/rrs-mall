/*
 * Copyright (c) 2012 杭州端点网络科技有限公司
 */

package com.aixforce.admin;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * Author:  <a href="mailto:jlchen.cn@gmail.com">jlchen</a>
 * Date: 2012-08-22
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:mail-context.xml"})
public class MailClientTest {

    @Autowired
    private MailClient mailClient;

    @Test
    @Ignore
    public void testSend() throws Exception {
        mailClient.send("test", "<h1>hello world</h1>", "admin@aixforce.com", "aixforcetest@163.com");
    }
}
